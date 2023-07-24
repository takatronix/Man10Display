package red.man10.display

import red.man10.display.CommandType.*
import java.io.File
import java.util.concurrent.Executors
import kotlin.math.roundToLong
import kotlin.random.Random

const val MACRO_SLEEP_TIME = 1L
enum class CommandType {
    LABEL,
    GOTO,
    SET,
    PRINT,
    WAIT,
    IF,
    LOOP,
    ENDLOOP,
    MACRO,
    EXIT,
    RANDOM,
    CLEAR,
    IMAGE,
    STRETCH_IMAGE,
}

data class MacroCommand(
    val type: CommandType,
    val params: List<String>
)

class MacroEngine {
    private val symbolTable = mutableMapOf<String, Any>()
    private val executorService = Executors.newSingleThreadExecutor()
    private val labelIndices = mutableMapOf<String, Int>()
    private var commands = listOf<MacroCommand>()
    private var currentLineIndex = 0
    private var shouldStop = false
    private var callback: ((MacroCommand, Int) -> Unit)? = null
    // プロパティとしての変数代入をサポートする
    private var variableName: String? = null
    private var isPaused = false

    // region 制御コマンド
    // 終了フラグを設定するメソッド
    fun stop() {
        shouldStop = true
    }

    // 終了フラグのチェック
    fun shouldStop(): Boolean {
        return shouldStop
    }
    // マクロを一時停止するメソッド
    fun pause() {
        isPaused = true
    }

    // マクロを再開するメソッド
    fun resume() {
        isPaused = false
    }
    // マクロの実行をリスタートする関数
    fun restart() {
        currentLineIndex = 0
        shouldStop = false
        isPaused = false
    }
    fun run(macroName: String, callback: (MacroCommand, Int) -> Unit) {
        val filePath = getMacroFilePath(macroName) ?: return
        val commands = parseMacroFile(filePath)
        execute(commands, callback)
    }

    // プロパティとしての変数代入をサポートする
    fun setVariable(variableName: String) {
        this.variableName = variableName
    }
    private fun execute(commands: List<MacroCommand>, callback: (MacroCommand, Int) -> Unit) {
        this.commands = commands
        this.callback = callback
        currentLineIndex = 0
        shouldStop = false
        var shouldEndLoop = false
        var loopDepth = 0

        collectLabels(commands)

        while (currentLineIndex < commands.size && !this.shouldStop && !shouldEndLoop) {
            val command = commands[currentLineIndex]

            while (isPaused) {
                if (shouldStop) {
                    return
                }
                Thread.sleep(MACRO_SLEEP_TIME)
            }
            info("[MACRO][$currentLineIndex] $command")
            when (command.type) {
                GOTO -> {
                    val label = command.params[0]
                    currentLineIndex = labelIndices[label] ?: throw IllegalArgumentException("Label not found: $label")
                }
                SET -> {
                    val variableName = command.params[0]
                    val expressionParts = command.params.drop(1)
                    val value = evaluateExpression(expressionParts.joinToString(" "))
                    symbolTable[variableName] = value
                }
                PRINT -> {
                    val expression = command.params.joinToString(" ")
                    if (expression.startsWith("\"") && expression.endsWith("\"")) {
                        // パラメータが文字列リテラルの場合は、そのまま出力します。
                        info(expression.substring(1, expression.length - 1))
                    } else {
                        // それ以外の場合は、expressionを評価します。
                        val value = evaluateExpression(expression)
                        info(value.toString())
                    }
                }
                WAIT -> {
                    val sleepTimeInSeconds = evaluateExpression(command.params[0]) as Double
                    if (!waitSeconds(sleepTimeInSeconds)) {
                        throw InterruptedException("Macro execution was stopped during a wait command.")
                    }
                }
                IF -> {
                    val conditionExpression = command.params[0]
                    val result = evaluateExpression(conditionExpression)
                    if (result is Boolean && result) {
                        val label = command.params[2]
                        currentLineIndex = labelIndices[label] ?: throw IllegalArgumentException("Label not found: $label")
                    } else {
                        currentLineIndex++
                    }
                }
                LOOP -> {
                    // ループが始まったことを記録し、ループ深さを増やす
                    loopDepth++

                    val loopCount = evaluateExpression(command.params[0]) as Int
                    val loopCommands = commands.subList(currentLineIndex + 1, currentLineIndex + 1 + loopCount)
                    executeLoop(loopCount, loopCommands, callback)

                    // ループの終了後、次のコマンドに進む
                    currentLineIndex += loopCommands.size + 1
                }
                // ENDLOOPコマンドの処理
                ENDLOOP -> {
                    loopDepth--
                    if (loopDepth == 0) {
                        shouldEndLoop = true // ループを終了するフラグを立てる
                        currentLineIndex++
                    }
                }
                MACRO -> { // マクロを呼び出すコマンドの処理
                    val filePath = command.params[0]
                    val nestedCommands = parseMacroCommands(filePath)
                    execute(nestedCommands, callback)
                    currentLineIndex++
                }
                EXIT -> {
                    stop() // マクロの実行を即座に終了
                }
                else -> {
                    // Handle other command types if needed
                    callback(command, currentLineIndex)
                    currentLineIndex++
                }
            }

            // Move to the next line if the command is not GOTO or IF
            if (command.type != GOTO && command.type != IF ) {
                currentLineIndex++
            }
        }
    }

    private fun executeLoop(loopCount: Int, loopCommands: List<MacroCommand>, callback: (MacroCommand, Int) -> Unit) {
        repeat(loopCount) {
            // LOOP 内のコマンドを実行
            execute(loopCommands, callback)
        }
    }
    // endregion


    private fun collectLabels(commands: List<MacroCommand>) {
        for (index in commands.indices) {
            val command = commands[index]
            if (command.type == CommandType.LABEL) {
                val label = command.params[0]
                labelIndices[label] = index
            }
        }
    }

    private fun findIndexOfLabel(commands: List<MacroCommand>, label: String): Int {
        val index = commands.indexOfFirst { it.type == LABEL && it.params[0] == label }
        if (index == -1) {
            throw IllegalArgumentException("Label not found: $label")
        }
        return index
    }


    private fun executeCommand(command: MacroCommand) {
        when (command.type) {
            SET -> {
                // プロパティとしての変数代入をサポート
                variableName?.let { varName ->
                    val expression = command.params[0]
                    symbolTable[varName] = evaluateExpression(expression)
                    this.variableName = null // 変数代入が終わったらプロパティをリセット
                }
            }
            PRINT -> {
                val expression = command.params[0]
                val result = evaluateExpression(expression)
                println(result.toString())
            }
            WAIT -> {
                val sleepTimeInSeconds = evaluateExpression(command.params[0]) as Double
                if (!waitSeconds(sleepTimeInSeconds)) {
                    // Stop the execution if the waiting was interrupted.
                    throw InterruptedException("Macro execution was stopped during a wait command.")
                }
            }
            IF -> {
                val conditionExpression = command.params[0]
                val result = evaluateExpression(conditionExpression)
                if (result is Boolean && result) {
                    val label = command.params[2]
                    currentLineIndex = findIndexOfLabel(commands, label)
                } else {
                    currentLineIndex++ // 条件が偽の場合は次の行に進む
                }
            }
            else -> {} // Do nothing for GOTO and LABEL, as these are handled in the run method
        }
    }

    private fun waitSeconds(seconds: Double): Boolean {
        val sleepMillis = (seconds * 1000).roundToLong()
        val future = executorService.submit {
            try {
                Thread.sleep(sleepMillis)
            } catch (e: InterruptedException) {
                // 終了指示が来た場合にはInterruptedExceptionが投げられる
            }
        }
        while (!future.isDone) {
            if (shouldStop()) {
                future.cancel(true)
                return false
            }
            Thread.sleep(MACRO_SLEEP_TIME)
        }
        return true
    }

    // ランダムな整数を生成する関数
    private fun random(min: Int, max: Int): Int {
        return Random.nextInt(min, max + 1)
    }
    private fun evaluateExpression(expression: String): Any {
        // 数値型の四則演算を評価する
        fun evaluateArithmeticExpression(expression: String): Double {
            val parts = expression.split(" ")
            if (parts.size != 3) {
                throw IllegalArgumentException("Invalid arithmetic expression: $expression")
            }

            // 入力が数値でない場合は変数として評価
            val num1 = evaluateExpression(parts[0]) as? Double ?: throw IllegalArgumentException("Invalid number or variable: ${parts[0]}")
            val operator = parts[1]
            val num2 = evaluateExpression(parts[2]) as? Double ?: throw IllegalArgumentException("Invalid number or variable: ${parts[2]}")

            return when (operator) {
                "+" -> num1 + num2
                "-" -> num1 - num2
                "*" -> num1 * num2
                "/" -> num1 / num2
                else -> throw IllegalArgumentException("Invalid operator: $operator")
            }
        }

        // 変数を評価する
        fun evaluateVariable(variableName: String): Any {
            return symbolTable[variableName] ?: throw IllegalArgumentException("Variable $variableName not found.")
        }

        // 数値を評価する
        fun evaluateNumber(number: String): Double {
            return number.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $number")
        }

        if (expression.startsWith("\"") && expression.endsWith("\"")) {
            // 文字列の式の評価: 引用符で囲まれた部分を返します。
            return expression.substring(1, expression.length - 1)
        } else if (expression == "true" || expression == "false") {
            // Boolean型の式の評価
            return expression.toBoolean()
        } else if (expression.startsWith("$")) {
            // 変数の評価
            val variableName = expression.removePrefix("$")
            return evaluateVariable(variableName)
        } else if (expression.toUpperCase() == "RANDOM") {
            // RANDOMの場合はランダムな整数を生成して返す
            return (0..100).random() // 0から100までのランダムな整数を返す
        } else if (expression.contains(" ") && (expression.contains("+") || expression.contains("-") || expression.contains("*") || expression.contains("/"))) {
            // 数値型の式の評価: 既存のロジックを使用
            return evaluateArithmeticExpression(expression)
        } else {
            // 単純な数値の評価
            return evaluateNumber(expression)
        }
    }


    // region parser
    // テキストファイルをList<red.man10.display.MacroCommand>に変換する関数
    fun parseMacroFile(filePath: String): List<MacroCommand> {
        val commands = mutableListOf<MacroCommand>()

        info("Parsing macro file: $filePath")
        File(filePath).forEachLine { line ->
            // 行を解析してMacroCommandに変換
            val command = parseCommand(line)
            if (command != null) {
                commands.add(command)
                // ログ出力
                info(command.toString())
            }
        }

        return commands
    }

    // 行を解析してMacroCommandに変換する関数
    private fun parseCommand(line: String): MacroCommand? {
        val trimmedLine = line.trim()
        if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
            // 空行またはコメント行の場合はnullを返す
            return null
        }

        // ラベルの書き方をチェック
        val labelRegex = Regex("^(\\w+):$")
        val labelMatch = labelRegex.find(trimmedLine)
        if (labelMatch != null) {
            val label = labelMatch.groupValues[1]
            return MacroCommand(CommandType.LABEL, listOf(label))
        }

        val parts = line.trim().split(" ")
        if (parts.isEmpty()) {
            return null
        }

        val type = when (parts[0].toUpperCase()) {
            "LABEL" -> LABEL
            "GOTO" -> GOTO
            "SET" -> SET
            "PRINT" -> PRINT
            "WAIT" -> WAIT
            "LOOP" -> LOOP
            "ENDLOOP" -> ENDLOOP
            "MACRO" -> MACRO
            "IF" -> IF
            "CLEAR" -> CLEAR
            "IMAGE" -> IMAGE
            "EXIT" -> EXIT
            "STRETCH_IMAGE" -> STRETCH_IMAGE
            "RANDOM" -> RANDOM
            else -> return null // 不明なコマンドは無視する
        }

        val params = parts.drop(1)

        return MacroCommand(type, params)
    }

    // マクロファイルをパースしてList<MacroCommand>に変換する関数
    private fun parseMacroCommands(filePath: String): List<MacroCommand> {
        val commands = mutableListOf<MacroCommand>()

        File(filePath).forEachLine { line ->
            // 行を解析してMacroCommandに変換
            val command = parseCommand(line)
            if (command != null) {
                commands.add(command)
            }
        }

        return commands
    }

    private fun getMacroFilePath(macroName: String): String? {
        val userdata = File(Main.plugin.dataFolder, File.separator + DEFAULT_MACRO_FOLDER)
        if (!userdata.exists()) {
            userdata.mkdir()
        }
        val macroFile = File(userdata, File.separator + macroName + ".txt")
        if (!macroFile.exists()) {
            return null
        }

        return macroFile.absolutePath
    }
    fun parse(macroName:String):List<MacroCommand>{
        val filePath = getMacroFilePath(macroName) ?: return emptyList()
        return parseMacroFile(filePath)
    }

    // endregion
}