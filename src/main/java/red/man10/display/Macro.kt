package red.man10.display

import org.bukkit.command.CommandSender
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

const val DEFAULT_PROGRAM_DURATION = 0.0
const val DEFAULT_MACRO_FOLDER = "macro"
class MacroData(var command: String = "", var duration:Double = DEFAULT_PROGRAM_DURATION,var fileName:String = ""){

    override fun toString(): String {
        return "$command,$duration,\"$fileName\""
    }
    fun load(str:String){
        val list = str.split(",")
        command = list[0]
        if(command.isEmpty())
            return
        if(list.size == 1)
            return
        if(list[1].isNotEmpty())
           duration = list[1].toDouble()
        if(list.size == 2)
            return
        fileName = list[2].replace("\"","")
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
    private var numericVariables = mutableMapOf<String, Int>()

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
                    macroData.command.startsWith("wait ") -> {
                        val duration = macroData.command.substring("wait ".length).toDoubleOrNull() ?: 0.0
                        Thread.sleep((duration * 1000).toLong())
                    }
                    macroData.command.startsWith("if ") -> ifStatement(macroData, callback)
                    macroData.command.startsWith("log ") -> log(macroData)
                    else -> callback(macroData, currentIndex)
                }

                // 次の行に進む
                currentIndex++
            }
            info("Macro execution finished ")
        }, 0, TimeUnit.SECONDS)  // すぐに開始する
    }

    // "goto" コマンドを処理する関数
    private fun goto(macroData: MacroData) {
        val label = macroData.command.substring("goto ".length)
        val labelIndex = labels[label]
        if (labelIndex != null) {
            currentIndex = labelIndex
        } else {
            error("Label $label not found")
        }
    }

    // "set" コマンドを処理する関数
    private fun set(macroData: MacroData) {
        val (varName, value) = macroData.command.substring("set ".length).split(" ", limit = 2)
        if (value.toIntOrNull() != null) {
            numericVariables[varName] = value.toInt()
        } else {
            stringVariables[varName] = value
        }
    }

    // "if" コマンドを処理する関数
    private fun ifStatement(macroData: MacroData, callback: (MacroData, Int) -> Unit) {
        val (condition, thenCommand) = macroData.command.substring("if ".length).split(" then ", limit = 2)
        val (varName, operator, expectedValue) = condition.split(" ", limit = 3)
        val actualStringValue = stringVariables[varName]
        val actualNumericValue = numericVariables[varName]

        val conditionIsTrue = when (operator) {
            "==" -> (actualStringValue == expectedValue) || (actualNumericValue == expectedValue.toIntOrNull())
            "!=" -> (actualStringValue != expectedValue) || (actualNumericValue != expectedValue.toIntOrNull())
            "<", ">", "<=", ">=" -> {
                if (actualNumericValue != null && expectedValue.toIntOrNull() != null) {
                    when (operator) {
                        "<" -> actualNumericValue < expectedValue.toInt()
                        ">" -> actualNumericValue > expectedValue.toInt()
                        "<=" -> actualNumericValue <= expectedValue.toInt()
                        ">=" -> actualNumericValue >= expectedValue.toInt()
                        else -> false
                    }
                } else {
                    throw RuntimeException("Invalid comparison between non-integer values")
                }
            }
            else -> throw RuntimeException("Invalid operator $operator in if statement")
        }
        if (conditionIsTrue) {
            // 'then' の後のコマンドを実行する
            val thenCommandParts = thenCommand.split(" ", limit = 2)
            if (thenCommandParts.size == 2) {
                val duration = thenCommandParts[0].toDoubleOrNull() ?: 0.0
                val command = thenCommandParts[1]
                callback(MacroData(command, duration), currentIndex)
            } else {
                callback(MacroData(thenCommand, 0.0), currentIndex)
            }
        }
    }

    // "log" コマンドを処理する関数
    private fun log(macroData: MacroData) {
        var message = macroData.command.substring("log ".length)
        for ((varName, value) in stringVariables) {
            message = message.replace("\${$varName}", value)
        }
        for ((varName, value) in numericVariables) {
            message = message.replace("\${$varName}", value.toString())
        }
        info(message)
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
}
    // endregion
