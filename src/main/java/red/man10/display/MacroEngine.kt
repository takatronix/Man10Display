package red.man10.display

import red.man10.display.CommandType.*
import java.io.File
import java.util.concurrent.Executors
import kotlin.math.roundToLong

enum class CommandType {
    LABEL,
    GOTO,
    SET,
    PRINT,
    WAIT,
    IF,
    LOOP,
    CLEAR,
    IMAGE,
    STRETCH_IMAGE,
}

data class MacroCommand(
    val type: CommandType,
    val params: List<String>
)

const val MACRO_SLEEP_TIME = 1L

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

    // マクロを一時停止するメソッド
    fun pause() {
        isPaused = true
    }

    // マクロを再開するメソッド
    fun resume() {
        isPaused = false
    }


    fun execute(commands: List<MacroCommand>, callback: (MacroCommand, Int) -> Unit) {
        this.commands = commands
        this.callback = callback
        currentLineIndex = 0
        shouldStop = false
        collectLabels(commands)

        while (currentLineIndex < commands.size && !this.shouldStop) {
            val command = commands[currentLineIndex]

            while (isPaused) {
                if (shouldStop) {
                    return
                }
                Thread.sleep(MACRO_SLEEP_TIME)
            }
            when (command.type) {
                GOTO -> {
                    val label = command.params[0]
                    currentLineIndex = labelIndices[label] ?: throw IllegalArgumentException("Label not found: $label")
                }
                SET -> {
                    val variableName = command.params[0].removePrefix("$")
                    val expression = command.params[1]
                    symbolTable[variableName] = evaluateExpression(expression)
                }
                PRINT -> {
                    val expression = command.params[0]
                    val result = evaluateExpression(expression)
                    println(result.toString())
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
                    // LOOP コマンドの処理
                    val loopCount = evaluateExpression(command.params[0]) as Int
                    val loopCommands = commands.subList(currentLineIndex + 1, currentLineIndex + 1 + loopCount)
                    // 指定された回数だけループする
                    repeat(loopCount) {
                        // LOOP 内のコマンドを実行
                        execute(loopCommands, callback)
                    }
                    // ループの終了後、次のコマンドに進む
                    currentLineIndex += loopCommands.size + 1
                }

                else -> {
                    // Handle other command types if needed
                    callback(command, currentLineIndex)
                    currentLineIndex++
                }
            }

            // Move to the next line if the command is not GOTO or IF
            if (command.type != CommandType.GOTO && command.type != CommandType.IF) {
                currentLineIndex++
            }
        }
    }

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

    // 終了フラグを設定するメソッド
    fun stop() {
        shouldStop = true
    }

    // 終了フラグのチェック
    fun shouldStop(): Boolean {
        return shouldStop
    }
    // プロパティとしての変数代入をサポートする
    fun setVariable(variableName: String) {
        this.variableName = variableName
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

    private fun evaluateExpression(expression: String): Any {
        if (expression.startsWith("\"") && expression.endsWith("\"")) {
            // 文字列の式の評価: 引用符で囲まれた部分を返します。
            return expression.substring(1, expression.length - 1)
        } else if (expression == "true" || expression == "false") {
            // Boolean型の式の評価
            return expression.toBoolean()
        } else {
            // 数値型の式の評価: 既存のロジックを使用
            val evaluator = object {
                fun evaluate(variableName: String): Double {
                    val value = symbolTable[variableName]
                    if (value is Number) {
                        return value.toDouble()
                    }
                    throw IllegalArgumentException("Variable $variableName is not a number")
                }
            }
            return evaluator.evaluate(expression)
        }
    }
    // region parser
    // テキストファイルをList<red.man10.display.MacroCommand>に変換する関数
    fun parseMacroFile(filePath: String): List<MacroCommand> {
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

    // 行を解析してMacroCommandに変換する関数
    private fun parseCommand(line: String): MacroCommand? {
        val trimmedLine = line.trim()
        if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
            // 空行またはコメント行の場合はnullを返す
            return null
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
            "IF" -> IF
            "CLEAR" -> CLEAR
            "IMAGE" -> IMAGE
            "STRETCH_IMAGE" -> STRETCH_IMAGE
            else -> return null // 不明なコマンドは無視する
        }

        val params = parts.drop(1)

        return MacroCommand(type, params)
    }
    // endregion
}