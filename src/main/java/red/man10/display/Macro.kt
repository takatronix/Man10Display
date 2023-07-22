package red.man10.display

import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

const val DEFAULT_PROGRAM_WAIT_TIME = 0.0
const val DEFAULT_MACRO_FOLDER = "macro"
class MacroData(var command: String = "", var fileName: String = "", var params: String = ""){

    override fun toString(): String {
        return "$command \"$fileName\" \"$params\""
    }
    fun load(str:String){

        val text = str.trimStart()
        // コメントは無視
        if(text.startsWith("#") || text.startsWith("//"))
            return
        // 空行は無視
        if(text.isEmpty())
            return

        val cmd = text.split(" ")
        if(cmd.isNotEmpty()){
            command = cmd[0]
        }
        if(cmd.size >= 2) {
            if(command == "image")
                fileName = cmd[1]
            if(command == "stretch")
                fileName = cmd[1]

            // 組み込み関数は引数含める
            when (command) {
                "wait" -> command = text
                "goto" -> command = text
                "set" -> command = text
                "if" -> command = text
                "log" -> command = text
            }
            fileName = fileName .replace("\"","")
        }
    }
}
class Macro {
    private var data = mutableListOf<MacroData>()
    var currentMacroName = ""

    init{
        createProgramDirectory()
    }

    companion object{
        val commands = arrayListOf("run","stop","list")
        val macroList:ArrayList<String>
            get() {
                val list = getMacroList()
                return list.toTypedArray().toCollection(ArrayList())
            }
        private fun getMacroList(): List<String> {
            val folder = File(Main.plugin.dataFolder, File.separator + DEFAULT_MACRO_FOLDER)
            val files = folder.listFiles()
            val list = mutableListOf<String>()
            for (f in files!!) {
                if (f.isFile) {
                    var filename = f.name
                    //      隠しファイルは無視
                    if (filename.substring(0, 1).equals(".", ignoreCase = true)) {
                        continue
                    }
                    val point = filename.lastIndexOf(".")
                    if (point != -1) {
                        filename = filename.substring(0, point)
                    }
                    list.add(filename)
                }
            }
            return list
        }
    }

    fun load(macroName: String): Boolean {
        val userdata = File(Main.plugin.dataFolder, File.separator + DEFAULT_MACRO_FOLDER)
        val f = File(userdata, File.separator + macroName + ".txt")
        if (!f.exists()) {
            return false
        }
        currentMacroName = macroName
        val lines = f.readLines()
        data.clear()
        lines.forEach {
            val d = MacroData()
            d.load(it)
            data.add(d)
        }
        return true
    }

    private fun createProgramDirectory(){
        val userdata = File(Main.plugin.dataFolder, File.separator + DEFAULT_MACRO_FOLDER)
        if(!userdata.exists()){
            userdata.mkdir()
        }
    }

    // region Macro Program
    // 実行スレッドを制御するエグゼキュータ
    private var executor = Executors.newSingleThreadScheduledExecutor()

    // 実行中のタスクを制御するフューチャ
    private var future: ScheduledFuture<*>? = null

    // マクロの終了を制御するフラグ
    private var shouldStop = false

    // ラベルとその位置を保存するマップ
    private var labels = mutableMapOf<String, Int>()

    // 文字列変数を保存するマップ
    private var stringVariables = mutableMapOf<String, String>()

    // 数値変数を保存するマップ
    private var numericVariables = mutableMapOf<String, Number>()

    // 実行中のマクロの現在のインデックス
    private var currentIndex = 0

    // マクロが実行中かどうかを返す関数
    fun isRunning() = future?.isDone?.not() ?: false

    // マクロを実行する関数
    fun execute(callback: (MacroData, Int) -> Unit) {
        // すでに実行中ならばまずそれを停止する
        if (isRunning()) {
            stop()
        }

        // ラベルと変数をリセットする
        labels.clear()
        stringVariables.clear()
        numericVariables.clear()
        currentIndex = 0
        shouldStop = false

        // 最初のパス：ラベルを収集する
        for ((index, macroData) in data.withIndex()) {
            if (macroData.command.endsWith(":")) {
                val label = macroData.command.substring(0, macroData.command.length - 1)
                labels[label] = index
            }
        }

        future = executor.schedule({
            while (currentIndex < data.size && !shouldStop) {
                val macroData = data[currentIndex]
               info("Executing: $currentMacroName($currentIndex) ${macroData.command} ")



                when {
                    macroData.command.startsWith("goto ") -> goto(macroData)
                    macroData.command.startsWith("set ") -> set(macroData)
                    macroData.command.startsWith("wait ") -> wait(macroData)
                    macroData.command.startsWith("if ") -> ifStatement(macroData)
                    macroData.command.startsWith("log ") -> log(macroData)
                    else -> callback(macroData, currentIndex)
                }

                // 次の行に進む
                currentIndex++
            }
            info("Macro execution finished ")
        }, 0, TimeUnit.SECONDS)  // すぐに開始する
    }
    private fun processCommand(command: String, macroData: MacroData) {
        when {
            command.startsWith("goto ") -> goto(MacroData(command, macroData.fileName, macroData.params))
            command.startsWith("set ") -> set(MacroData(command, macroData.fileName, macroData.params))
            command.startsWith("wait ") -> wait(MacroData(command, macroData.fileName, macroData.params))
            command.startsWith("if ") -> ifStatement(MacroData(command, macroData.fileName, macroData.params))
            command.startsWith("log ") -> log(MacroData(command, macroData.fileName, macroData.params))
            // 他のコマンドも同様に追加
            else -> throw IllegalArgumentException("Invalid command: $command")
        }
    }
    // "goto" コマンドを処理する関数
    private fun goto(macroData: MacroData) {
        val label = macroData.command.substring("goto ".length)
        val labelIndex = labels[label]
        if (labelIndex != null) {
            currentIndex = labelIndex
            info("Jumping to label $label ($currentIndex)")
        } else {
            error("Label $label not found")
        }
    }

    private fun substituteVariables(expression: String, variables: Map<String, Number>): String {
        var substitutedExpression = expression
        for ((varName, varValue) in variables) {
            substitutedExpression = substitutedExpression.replace("{$varName}", varValue.toString())
        }
        return substitutedExpression
    }
    private fun set(macroData: MacroData) {
        val (varName, value) = macroData.command.substring("set ".length).split(" ", limit = 2)

        if (value.startsWith("{") && value.endsWith("}")) {
            val expression = value.substring(1, value.length - 1)
            val substitutedExpression = substituteVariables(expression, numericVariables)
            val evaluatedValue = evaluateExpression(substitutedExpression, numericVariables)
            numericVariables[varName] = evaluatedValue
        } else if (value.toDoubleOrNull() != null) {
            // valueが数値なので直接設定します
            if (value.contains(".")) {
                numericVariables[varName] = value.toDouble()
            } else {
                numericVariables[varName] = value.toInt()
            }
        } else {
            // valueが文字列なので直接設定します
            stringVariables[varName] = value
        }
    }
    private fun add(macroData: MacroData) {
        val (varName, value) = macroData.command.substring("add ".length).split(" ", limit = 2)
        val addValue = value.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $value")
        val originalValue = numericVariables[varName]?.toDouble() ?: throw IllegalArgumentException("Undefined variable: $varName")
        numericVariables[varName] = originalValue + addValue
    }
    private fun evaluateExpression(expression: String, variables: Map<String, Number>): Double {

        val (varName, operation, number) = expression.split(" ")
        val varValue = variables[varName] ?: 0.0
        val numberValue = number.toDoubleOrNull() ?: 0.0

        return when (operation) {
            "+" -> varValue.toDouble() + numberValue.toDouble()
            "-" -> varValue.toDouble() - numberValue.toDouble()
            "*" -> varValue.toDouble() * numberValue.toDouble()
            "/" -> varValue.toDouble() / numberValue.toDouble()
            else -> throw IllegalArgumentException("不明な演算子: $operation")
        }
    }

    // "if" コマンドを処理する関数
    private fun ifStatement(macroData: MacroData) {
        val (condition, thenCommand) = macroData.command.substring("if ".length).split(" then ", limit = 2)
        val (left, operator, right) = condition.split(" ", limit = 3)

        val leftVariableName = if (left.startsWith("{") && left.endsWith("}")) left.substring(1, left.length - 1) else left
        val rightVariableName = if (right.startsWith("{") && right.endsWith("}")) right.substring(1, right.length - 1) else right

        val leftValue = getValue(leftVariableName)
        val rightValue = getValue(rightVariableName)

        val result = when (operator) {
            "==" -> leftValue == rightValue
            "!=" -> leftValue != rightValue
            "<" -> leftValue.toDouble() < rightValue.toDouble()
            "<=" -> leftValue.toDouble() <= rightValue.toDouble()
            ">" -> leftValue.toDouble() > rightValue.toDouble()
            ">=" -> leftValue.toDouble() >= rightValue.toDouble()
            else -> throw IllegalArgumentException("Invalid operator: $operator")
        }

        if (result) {
            processCommand(thenCommand, macroData)
        }
    }

    // "log" コマンドを処理する関数
    private fun log(macroData: MacroData) {
        var message = macroData.command.substring("log ".length)
        for ((varName, value) in stringVariables) {
            message = message.replace("\${$varName}", value)
        }
        for ((varName, value) in numericVariables) {
            if (value is Int) {
                message = message.replace("\${$varName}", value.toString())
            } else if (value is Double) {
                message = message.replace("\${$varName}", value.toString())
            }
        }
        //\u3000""があれば、左右の""を削除する
        if(message.startsWith("\"") && message.endsWith("\"")){
            message = message.substring(1,message.length-1)
        }
        info(message)
    }
    private fun wait(macroData: MacroData) {
        val duration = getValue(macroData.command.substring("wait ".length))
        Thread.sleep((duration * 1000).toLong())  // durationは秒単位と仮定
    }
    // マクロの実行を停止する関数
    fun stop() {
        shouldStop = true
        future?.cancel(false)
        future = null
    }

    // マクロの実行を強制的に停止する関数
    fun shutdown() {
        stop()
        executor.shutdownNow()  // 実行中のタスクを強制的に停止する
        executor = Executors.newSingleThreadScheduledExecutor()  // 新しいタスクのための新しいエグゼキュータを作成する
    }

    private fun getValue(value: String): Double {
        // 変数名が {...} 形式で与えられた場合、 {...} を取り除く
        val variableName = if (value.startsWith("{") && value.endsWith("}")) {
            value.substring(1, value.length - 1)
        } else {
            value
        }

        // 数値として解釈できる場合はそのまま数値として返す
        val doubleValue = value.toDoubleOrNull()
        if (doubleValue != null) {
            return doubleValue
        }

        // 変数として解釈できる場合はその値を返す
        val variableValue = numericVariables[value]?.toDouble()
        if (variableValue != null) {
            return variableValue
        }

        // それ以外の場合はエラー
        throw IllegalArgumentException("Invalid value: $value")
    }
}
    // endregion
