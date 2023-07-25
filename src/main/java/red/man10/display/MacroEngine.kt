package red.man10.display

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import red.man10.display.CommandType.*
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.roundToLong
import kotlin.random.Random
import kotlinx.coroutines.*

const val MACRO_SLEEP_TIME = 1L

enum class CommandType {
    LABEL,
    GOTO,
    SET,
    PRINT,
    WAIT,
    IF,
    ELSE,
    ENDIF,
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
    private val labelIndices = mutableMapOf<String, Int>()
    private var commands = listOf<MacroCommand>()
    private var currentLineIndex = 0
    private var shouldStop = false
    private var callback: ((MacroCommand, Int) -> Unit)? = null

    private data class Loop(val startLine: Int, var counter: Int)
    private data class IfBlock(var condition: Boolean, val startLine: Int)
    private val loopStack = Stack<Loop>()
    private val ifStack = Stack<IfBlock>()

    // region 制御コマンド

    fun skip() {
        currentLineIndex++
    }

    // マクロの実行をリスタートする関数
    fun restart() {
        currentLineIndex = 0
        shouldStop = false
    }

    private var currentJob: Job? = null  // Current job
    fun stop() {
        info("Stopping macro execution...")
        shouldStop = true
        currentJob?.cancel()
    }

    fun isRunning(): Boolean {
        return currentJob?.isActive == true
    }
    @OptIn(DelicateCoroutinesApi::class)
    fun runMacroAsync(macroName: String, callback: (MacroCommand, Int) -> Unit) {

        if (isRunning()) {
            stop()
        }
        // 現在のジョブが完了するまで待つ
        runBlocking {
            currentJob?.join()
        }
        // GlobalScopeを使用して、新しいトップレベルのコルーチンを起動します。
        // このコルーチンはメインスレッドから切り離され、バックグラウンドで実行されます。
        currentJob = GlobalScope.launch {
            run(macroName, callback)
        }
    }
    private fun run(macroName: String, callback: (MacroCommand, Int) -> Unit) {
        val filePath = getMacroFilePath(macroName) ?: return
        val commands = parseMacroFile(filePath)
        execute(commands, callback)
    }

    private fun execute(commands: List<MacroCommand>, callback: (MacroCommand, Int) -> Unit) {
        this.commands = commands
        this.callback = callback
        currentLineIndex = 0
        collectLabels(commands)

        while (currentLineIndex < commands.size) {
            val command = commands[currentLineIndex]

            if(currentJob?.isActive == false){
                info("Macro execution was stopped.")
                break
            }

            //info("[MACRO][$currentLineIndex] $command")
            when (command.type) {
                GOTO -> {
                    val label = command.params[0]
                    currentLineIndex = labelIndices[label] ?: throw IllegalArgumentException("Label not found: $label")
                    continue
                }

                SET -> {
                    val variableName = command.params[0].removePrefix("$")
                    val expressionParts = command.params.drop(1)
                    val value = evaluateExpression(expressionParts.joinToString(" "))
                    symbolTable[variableName] = value
                    info("Set $variableName = $value")
                }

                PRINT -> {
                    val expression = command.params.joinToString(" ")
                    if (expression.startsWith("\"") && expression.endsWith("\"")) {
                        // パラメータが文字列リテラルの場合は、そのまま出力します。
                        val output = evaluateStringExpression(expression.substring(1, expression.length - 1))
                        info(output)
                    } else {
                        // それ以外の場合は、expressionを評価します。
                        val value = evaluateExpression(expression)
                        info(value.toString())
                    }
                }

                WAIT -> {
                    val sleepTimeInSeconds = evaluateExpression(command.params[0]) as Double
                    if (!waitSeconds(sleepTimeInSeconds)) {
                        info("Macro execution was stopped during a wait command.")
                   //     throw InterruptedException("Macro execution was stopped during a wait command.")
                    }
                }

                IF -> {
                    val condition = evaluateExpression(command.params[0]) as Boolean
                    if (!condition) {
                        // IF文の条件がfalseの場合、対応するELSEかENDIFまでスキップする
                        var depth = 0
                        while (currentLineIndex < commands.size - 1) {
                            currentLineIndex++
                            val nextCommand = commands[currentLineIndex]
                            if (nextCommand.type == IF) {
                                depth++
                            } else if (nextCommand.type == ELSE && depth == 0) {
                                // ELSEがある場合、ELSEまでスキップする
                                break
                            } else if (nextCommand.type == ENDIF && depth == 0) {
                                // IF文のENDIFに到達したら終了
                                break
                            } else if (nextCommand.type == ENDIF) {
                                depth--
                            }
                        }
                    }
                }

                ELSE -> {
                    // ELSEはスキップする
                    var depth = 0
                    while (currentLineIndex < commands.size - 1) {
                        currentLineIndex++
                        val nextCommand = commands[currentLineIndex]
                        if (nextCommand.type == IF) {
                            depth++
                        } else if (nextCommand.type == ELSE && depth == 0) {
                            break
                        } else if (nextCommand.type == ENDIF) {
                            if (depth == 0) {
                                // ELSE節があるにもかかわらずENDIFに達した場合はエラーとする
                                throw IllegalArgumentException("Unexpected ENDIF at line $currentLineIndex")
                            }
                            depth--
                        }
                    }
                }

                ENDIF -> {
                    // なにもしない
                }

                LOOP -> {
                    if (loopStack.isEmpty() || loopStack.peek().startLine != currentLineIndex - 1) {
                        val loopCount = if (command.params.isEmpty()) {
                            // パラメータが指定されていない場合、無限にループする
                            Int.MAX_VALUE
                        } else {
                            // パラメータが指定されている場合、その回数だけループする
                            (evaluateExpression(command.params[0]) as Double).toInt()
                        }
                        loopStack.push(Loop(currentLineIndex - 1, loopCount))
                    }
                }
                // ENDLOOPコマンドの処理
                ENDLOOP -> {
                    if (loopStack.isNotEmpty()) {
                        val currentLoop = loopStack.peek()
                        currentLoop.counter--
                        if (currentLoop.counter > 0) {
                            // まだループを続行する必要がある場合、ループの開始位置に戻る
                            currentLineIndex = currentLoop.startLine + 1
                            continue
                        } else {
                            // ループを終了する
                            loopStack.pop()
                        }
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

                LABEL -> {
                    info("Label: ${command.params[0]}")
                }

                else -> {
                    // Handle other command types if needed
                    callback(command, currentLineIndex)
                }
            }

            currentLineIndex++
        }

        // ループが終了した後、ENDIFが残っていないかチェック
        if (ifStack.isNotEmpty()) {
            throw IllegalArgumentException("Unclosed IF block. Missing ENDIF.")
        }
        info("Macro execution finished.")
    }

    // endregion
    // region 評価関数
    private fun evaluateStringExpression(expression: String): String {
        val regex = Regex("\\{(\\$\\w+)}")
        return regex.replace(expression) { matchResult ->
            val variableName = matchResult.groups[1]!!.value.removePrefix("$")
            val variableValue = evaluateVariable(variableName)
            variableValue.toString()
        }
    }
    // 変数を評価する
    private fun evaluateVariable(variableName: String): Any {
        return symbolTable[variableName] ?: throw IllegalArgumentException("Variable $variableName not found.")
    }

    // 数値を評価する
    private fun evaluateNumber(number: String): Double {
        return number.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $number")
    }
    // 数値型の四則演算を評価する
    private fun evaluateArithmeticExpression(expression: String): Double {
        val parts = expression.split(" ")
        if (parts.size != 3) {
            throw IllegalArgumentException("Invalid arithmetic expression: $expression")
        }

        // 入力が数値でない場合は変数として評価
        val num1 = evaluateExpression(parts[0]) as? Double
            ?: throw IllegalArgumentException("Invalid number or variable: ${parts[0]}")
        val operator = parts[1]
        val num2 = evaluateExpression(parts[2]) as? Double
            ?: throw IllegalArgumentException("Invalid number or variable: ${parts[2]}")

        return when (operator) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            "*" -> num1 * num2
            "/" -> num1 / num2
            else -> throw IllegalArgumentException("Invalid operator: $operator")
        }
    }
    // 比較式を評価する
    private fun evaluateComparisonExpression(expression: String): Boolean {
        val operators = listOf(">=", "<=", ">", "<", "==")
        val operator = operators.find { expression.contains(it) }
            ?: throw IllegalArgumentException("Invalid comparison expression: $expression")

        val parts = expression.split(operator, limit = 2)
        if (parts.size != 2) {
            throw IllegalArgumentException("Invalid comparison expression: $expression")
        }

        val num1 = evaluateExpression(parts[0].trim()) as? Double
            ?: throw IllegalArgumentException("Invalid number or variable: ${parts[0].trim()}")
        val num2 = evaluateExpression(parts[1].trim()) as? Double
            ?: throw IllegalArgumentException("Invalid number or variable: ${parts[1].trim()}")

        return when (operator) {
            ">=" -> num1 >= num2
            "<=" -> num1 <= num2
            ">" -> num1 > num2
            "<" -> num1 < num2
            "==" -> num1 == num2
            else -> throw IllegalArgumentException("Invalid comparison operator in expression: $expression")
        }
    }

    // 式を評価する
    private fun evaluateExpression(expression: String): Any {
        // 比較演算子が含まれる場合は、比較式の評価を行う
        val comparisonOperators = listOf(">", "<", ">=", "<=", "==")
        if (comparisonOperators.any { expression.contains(it) }) {
            return evaluateComparisonExpression(expression)
        }

        if (expression.startsWith("\"") && expression.endsWith("\"")) {
            // 文字列の式の評価: 引用符で囲まれた部分を返します。
            return expression.substring(1, expression.length - 1)
        } else if (expression.contains(" ")) {
            // 算術式の評価
            return evaluateArithmeticExpression(expression)
        } else if (expression == "true" || expression == "false") {
            // Boolean型の式の評価
            return expression.toBoolean()
        } else if (expression.startsWith("$")) {
            // 変数の評価
            val variableName = expression.removePrefix("$")
            return evaluateVariable(variableName)
        } else if (expression.uppercase(Locale.getDefault()) == "RANDOM") {
            // RANDOMの場合はランダムな整数を生成して返す
            return (0..100).random() // 0から100までのランダムな整数を返す
        } else if (expression.contains(" ") && (expression.contains("+") || expression.contains("-") || expression.contains(
                "*"
            ) || expression.contains("/"))
        ) {
            // 数値型の式の評価: 既存のロジックを使用
            return evaluateArithmeticExpression(expression)
        } else if (expression.startsWith("$")) {
            // 変数の評価
            val variableName = expression.removePrefix("$")
            return evaluateVariable(variableName)
        } else {
            // 単純な数値の評価
            return evaluateNumber(expression)
        }
    }

    // endregion

    private fun executeLoop(loopCount: Int, loopCommands: List<MacroCommand>, callback: (MacroCommand, Int) -> Unit) {
        repeat(loopCount) {
            // LOOP 内のコマンドを実行
            execute(loopCommands, callback)
        }
    }
    private fun collectLabels(commands: List<MacroCommand>) {
        for (index in commands.indices) {
            val command = commands[index]
            if (command.type == LABEL) {
                val label = command.params[0]
                labelIndices[label] = index
            }
        }
    }

    private fun waitSeconds(seconds: Double): Boolean {
        val sleepTime = (seconds * 1000).roundToLong()
        val startTime = System.currentTimeMillis()
        info("Waiting for $seconds seconds...")
        while (System.currentTimeMillis() - startTime < sleepTime) {
            if (shouldStop) {
                info("Macro execution was stopped during a wait command.")
                return false
            }
            Thread.sleep(MACRO_SLEEP_TIME)
        }
        info("Waited for $seconds seconds.")
        return true
    }

    // ランダムな整数を生成する関数
    private fun random(min: Int, max: Int): Int {
        return Random.nextInt(min, max + 1)
    }

    // region parser
    private fun parseMacroFile(filePath: String): List<MacroCommand> {
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
        info("Parsing line: $line")
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
            return MacroCommand(LABEL, listOf(label))
        }

        val parts = line.trim().split(" ", limit = 3)

        // $a = $b + $c のような式を検出
        if (parts.size == 3 && parts[1] == "=") {
            // 新しい構文を検出
            val variableName = parts[0]
            val expression = parts[2]
            return MacroCommand(SET, listOf(variableName, expression))
        }

        val type = when (parts[0].uppercase(Locale.getDefault())) {
            "LABEL" -> LABEL
            "GOTO" -> GOTO
            "SET" -> SET
            "PRINT" -> PRINT
            "WAIT" -> WAIT
            "LOOP" -> LOOP
            "ENDLOOP" -> ENDLOOP
            "MACRO" -> MACRO
            "IF" -> IF
            "ELSE" -> ELSE
            "ENDIF" -> ENDIF
            "CLEAR" -> CLEAR
            "IMAGE" -> IMAGE
            "EXIT" -> EXIT
            "STRETCH_IMAGE" -> STRETCH_IMAGE
            "RANDOM" -> RANDOM
            else -> return null // 不明なコマンドは無視する
        }
        val params = if (type == IF || type == ELSE) {
            // IF文やELSE文の場合、括弧を省略して式を評価する
            val expression = line.substringAfter(" ")
            listOf(expression)
        } else {
            parts.drop(1)
        }

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

    fun parse(macroName: String): List<MacroCommand> {
        val filePath = getMacroFilePath(macroName) ?: return emptyList()
        return parseMacroFile(filePath)
    }

    // endregion
}