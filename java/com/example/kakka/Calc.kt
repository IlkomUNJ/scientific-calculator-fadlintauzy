package com.example.kakka

import java.util.Stack
import kotlin.math.*


class Calc {
    private var currExpression: String = "0"

    // --- Konstanta untuk Keterbacaan ---
    private val BINARY_OPERATORS = listOf("+", "-", "x", "/", "^")
    private val ALL_TRIG_FUNCTIONS = listOf("sin", "cos", "tan", "asin", "acos", "atan")
    private val OTHER_FUNCTIONS = listOf("log", "ln", "sqrt")

    fun onDigit(digit: String) {
        if (currExpression == "Error") {
            currExpression = "0"
        }

        // Perkalian Implisit: Angka setelah kurung tutup
        if (currExpression.endsWith(')')) {
            currExpression += "x"
        }

        if (currExpression == "0" && digit != ".") {
            currExpression = digit
        }
        else if (digit == "." && currExpression.endsWith('.')) {
            return
        }
        else {
            currExpression += digit
        }
    }

    /**
     * Handles inputs for mathematical constants, like 'e'.
     */
    fun onConstant(constant: String) {
        if (currExpression == "Error") {
            currExpression = "0"
        }

        if (currExpression != "0" && !isOperator(currExpression.last().toString()) && currExpression.last() != '(') {
            currExpression += "x"
        }

        currExpression = when(constant) {
            "e" -> currExpression.replace("e", E.toString())
            else -> currExpression
        }
        if (currExpression == "0") {
            currExpression = E.toString()
        }
    }

    fun onOperator(operator: String) {
        if (currExpression == "Error") {
            currExpression = "0"
        }

        val lastChar = currExpression.lastOrNull()?.toString()
        val isLastCharOperator = lastChar in BINARY_OPERATORS

        when (operator) {
            in BINARY_OPERATORS -> {

                // *** PENCEGAHAN BARU: Jika karakter terakhir adalah '(', hanya izinkan '-' ***
                if (lastChar == "(") {
                    if (operator != "-") {
                        return // Abaikan semua operator biner selain '-'
                    }
                }

                // KONDISI MUTLAK: MENCEGAH OPERATOR BERURUTAN (TIDAK ADA PENGGANTIAN)
                if (isLastCharOperator) {
                    if (operator == "-" && lastChar != "-") {
                        // Lanjutkan ke KASUS UNARY MINUS
                    } else {
                        return
                    }
                }

                // --- KASUS 1: Unary Minus (-) ---
                if (operator == "-") {
                    if (currExpression == "0") {
                        currExpression = "-"
                        return
                    }

                    if (isLastCharOperator || lastChar == "(") {
                        if (lastChar == "-") {
                            return
                        }
                        currExpression += operator
                        return
                    }
                }


                // --- KASUS 2: TAMBAHKAN OPERATOR BINER BARU ---
                currExpression += operator
            }

            // BLOK KURUNG DIHAPUS, karena input kurung hanya datang dari onScientificFunction
        }
    }

    /**
     * Applies a scientific function.
     */
    fun onScientificFunction(function: String) {
        if (currExpression == "Error") {
            currExpression = "0"
        }

        val internalFunction = when (function) {
            "x!" -> "!"
            "1/x" -> "1/x"
            "%" -> "%"
            else -> function
        }

        if (internalFunction == "!" || internalFunction == "%" || internalFunction == "1/x") {
            // PERBAIKAN POSTFIX: Ganti operand terakhir dengan notasi fungsi, TUNDA EVALUASI
            try {
                // 1. Dapatkan semua token
                val allTokens = tokenize(currExpression)

                // 2. Cek apakah token terakhir adalah angka tunggal
                if (allTokens.isEmpty() || allTokens.last().toDoubleOrNull() == null) {
                    throw IllegalArgumentException("Operand missing or not a single number.")
                }

                val operandString = allTokens.last()

                // 3. Tentukan notasi postfix baru
                val newPostfixNotation = when (internalFunction) {
                    "!" -> "$operandString!"
                    "%" -> "$operandString%"
                    "1/x" -> "1/($operandString)"
                    else -> ""
                }

                // 4. Ganti operand terakhir di currExpression dengan notasi baru
                val newExpression = currExpression.dropLast(operandString.length) + newPostfixNotation

                currExpression = newExpression

            } catch (e: Exception) {
                currExpression = "Error"
            }
        } else {
            val lastChar = currExpression.lastOrNull()?.toString()

            // Perkalian implisit
            if (currExpression != "0" && lastChar !in BINARY_OPERATORS && lastChar != "(") {
                currExpression += "x"
            }

            if (currExpression == "0") {
                currExpression = "$function("
            } else {
                currExpression += "$function("
            }
        }
    }

    fun onEquals() {
        if (currExpression == "Error" || currExpression == "0") return

        try {
            var expressionToEvaluate = currExpression

            // Tambahkan kurung tutup yang hilang
            val openParenCount = expressionToEvaluate.count { it == '(' }
            val closeParenCount = expressionToEvaluate.count { it == ')' }

            if (openParenCount > closeParenCount) {
                val missingCloses = openParenCount - closeParenCount
                repeat(missingCloses) {
                    expressionToEvaluate += ")"
                }
            }

            val result = evaluateExpression(expressionToEvaluate)
            currExpression = formatNumber(result)
        } catch (e: Exception) {
            currExpression = "Error"
        }
    }

    fun onClear() {
        currExpression = "0"
    }

    fun onDelete() {
        if (currExpression.length > 1) {
            currExpression = currExpression.dropLast(1)
        } else {
            currExpression = "0"
        }
    }

    fun getDisplay(): String {
        return currExpression
    }

    private fun isOperator(token: String): Boolean {
        return token in BINARY_OPERATORS || token == "!" || token == "%" || token == "1/x"
    }

    private fun isFunction(token: String): Boolean {
        return token in ALL_TRIG_FUNCTIONS || token in OTHER_FUNCTIONS
    }

    private fun isExpressionCurrentNumber(): Boolean {
        return currExpression.toDoubleOrNull() != null
    }

    // --- Implementasi Kalkulator ---

    private fun evaluateExpression(expression: String): Double {
        val tokens = tokenize(expression)
        val rpnTokens = shuntingYard(tokens)
        return evaluateRPN(rpnTokens)
    }

    /**
     * Splits the expression string into a list of numbers, operators, and parentheses.
     */
    private fun tokenize(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < expression.length) {
            val char = expression[i]
            when {
                char.isDigit() || char == '.' -> {
                    var numberBuffer = ""
                    while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                        numberBuffer += expression[i]
                        i++
                    }
                    tokens.add(numberBuffer)
                }
                char.isLetter() -> {
                    var functionBuffer = ""
                    while (i < expression.length && expression[i].isLetter()) {
                        functionBuffer += expression[i]
                        i++
                    }
                    tokens.add(functionBuffer)
                }
                // Handle Unary Minus: jika di awal, atau setelah operator biner/kurung buka
                char == '-' && (i == 0 || expression[i - 1].toString() in (BINARY_OPERATORS + listOf("("))) -> {

                    // PERBAIKAN: Menangani -fungsi atau -kurung sebagai -1 x
                    val nextCharIndex = i + 1
                    var isNextCharFunction = false

                    // Cek apakah diikuti oleh fungsi
                    for (func in ALL_TRIG_FUNCTIONS + OTHER_FUNCTIONS) {
                        if (expression.length > nextCharIndex && expression.substring(nextCharIndex).startsWith(func)) {
                            isNextCharFunction = true
                            break
                        }
                    }

                    // Cek apakah diikuti oleh kurung buka
                    val isNextCharParen = nextCharIndex < expression.length && expression[nextCharIndex] == '('

                    if (isNextCharFunction || isNextCharParen) {
                        // Ubah menjadi -1 x
                        tokens.add("-1")
                        tokens.add("x")
                        i++
                    } else {
                        // KASUS LAMA: Mengkonsumsi angka
                        var numberBuffer = "-"
                        i++
                        while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                            numberBuffer += expression[i]
                            i++
                        }
                        tokens.add(numberBuffer)
                    }
                }
                // *** PERBAIKAN: Mengisolasi 1/x, %, ! sebagai token operator single-character/string
                char == '!' -> {
                    tokens.add("!")
                    i++
                }
                expression.substring(i).startsWith("1/x") -> {
                    tokens.add("1/x")
                    i += 3
                }
                char == '%' -> {
                    tokens.add("%")
                    i++
                }
                char.toString() in (BINARY_OPERATORS + listOf("(", ")")) -> {
                    tokens.add(char.toString())
                    i++
                }
                else -> i++
            }
        }
        return tokens.filter { it.isNotBlank() }
    }

    /**
     * Converts an infix token list to a postfix (RPN) list using the shunting-yard algorithm.
     */
    private fun shuntingYard(tokens: List<String>): List<String> {
        val outputQueue = mutableListOf<String>()
        val operatorStack = Stack<String>()

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> {
                    outputQueue.add(token)
                }
                isFunction(token) -> {
                    operatorStack.push(token)
                }
                token == "(" -> {
                    operatorStack.push(token)
                }
                token == ")" -> {
                    while (operatorStack.isNotEmpty() && operatorStack.peek() != "(") {
                        outputQueue.add(operatorStack.pop())
                    }
                    if (operatorStack.isNotEmpty() && operatorStack.peek() == "(") {
                        operatorStack.pop()
                        if (operatorStack.isNotEmpty() && isFunction(operatorStack.peek())) {
                            outputQueue.add(operatorStack.pop())
                        }
                    } else {
                        throw IllegalArgumentException("Mismatched parentheses")
                    }
                }
                isOperator(token) -> {
                    while (operatorStack.isNotEmpty()) {
                        val stackPeek = operatorStack.peek()

                        // KONDISI 1: Jika melihat Fungsi, pop fungsi tersebut (Preseden tertinggi)
                        // Fungsi harus diproses ke output sebelum operator biner yang datang.
                        if (isFunction(stackPeek)) {
                            outputQueue.add(operatorStack.pop())
                            continue // Lanjutkan loop untuk memproses operator di bawahnya
                        }

                        // Kurung buka menghentikan pop
                        if (stackPeek == "(") break

                        // Lakukan pop operator biner jika presedennya lebih tinggi atau sama
                        if (isOperator(stackPeek) && getPrecedence(stackPeek) >= getPrecedence(token)) {
                            outputQueue.add(operatorStack.pop())
                        } else {
                            break
                        }
                    }
                    operatorStack.push(token)
                }
            }
        }
        while (operatorStack.isNotEmpty()) {
            if (operatorStack.peek() in listOf("(", ")")) throw IllegalArgumentException("Mismatched parentheses")
            outputQueue.add(operatorStack.pop())
        }
        return outputQueue
    }

    /**
     * Evaluates a postfix (RPN) expression.
     */
    private fun evaluateRPN(tokens: List<String>): Double {
        val stack = Stack<Double>()
        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> {
                    stack.push(token.toDouble())
                }
                isFunction(token) -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Invalid RPN expression: missing operand for $token")
                    val operand = stack.pop()
                    val result = performScientificCalculation(operand, token)
                    stack.push(result)
                }
                isOperator(token) -> {
                    if (token == "!" || token == "1/x" || token == "%") {
                        if (stack.isEmpty()) throw IllegalArgumentException("Invalid RPN expression: missing operand for $token")
                        val operand = stack.pop()
                        val result = performScientificCalculation(operand, token)
                        stack.push(result)
                    } else {
                        if (stack.size < 2) throw IllegalArgumentException("Invalid RPN expression: missing operand for $token")
                        val operand2 = stack.pop()
                        val operand1 = stack.pop()
                        val result = performCalculation(operand1, operand2, token)
                        stack.push(result)
                    }
                }
            }
        }
        if (stack.size != 1) throw IllegalArgumentException("Invalid RPN expression")
        return stack.pop()
    }

    /**
     * Defines the precedence of each operator.
     */
    private fun getPrecedence(op: String): Int {
        return when {
            isFunction(op) -> 4
            op == "!" || op == "1/x" || op == "%" -> 3
            op == "^" -> 3
            op == "x" || op == "/" -> 2
            op == "+" || op == "-" -> 1
            else -> 0
        }
    }

    private fun Double.degToRadians(): Double = this * PI / 180
    private fun Double.radiansToDeg(): Double = this * 180 / PI


    /**
     * Performs a standard binary calculation based on the given operator.
     */
    private fun performCalculation(operand1: Double, operand2: Double, operator: String): Double {
        return when (operator) {
            "+" -> operand1 + operand2
            "-" -> operand1 - operand2
            "x" -> operand1 * operand2
            "/" -> if (operand2 != 0.0) operand1 / operand2 else Double.NaN
            "^" -> operand1.pow(operand2)
            else -> Double.NaN
        }
    }

    /**
     * Performs a scientific calculation (unary operation).
     */
    private fun performScientificCalculation(operand: Double, operator: String): Double {
        return when (operator) {
            // Trigonometric
            "sin" -> sin(operand.degToRadians())
            "cos" -> cos(operand.degToRadians())
            "tan" -> tan(operand.degToRadians())
            "asin" -> asin(operand).radiansToDeg()
            "acos" -> acos(operand).radiansToDeg()
            "atan" -> atan(operand).radiansToDeg()
            // Logarithmic
            "log" -> log10(operand)
            "ln" -> ln(operand)
            // Other
            "sqrt" -> {
                if (operand < 0) Double.NaN else sqrt(operand)
            }
            "!" -> factorial(operand)
            "1/x" -> 1.0 / operand
            "%" -> operand / 100.0
            else -> Double.NaN
        }
    }

    /**
     * Helper function to calculate the factorial of a number.
     */
    private fun factorial(n: Double): Double {
        // PERBAIKAN: Batasi faktorial untuk mencegah overflow Double
        if (n < 0 || n != n.toInt().toDouble()) return Double.NaN

        val nInt = n.toInt()

        // Batas aman (170! adalah maksimum sebelum menjadi Infinity)
        if (nInt > 170) return Double.POSITIVE_INFINITY

        if (nInt == 0) return 1.0

        var result = 1.0
        for (i in 1..nInt) {
            result *= i
        }
        return result
    }

    /**
     * Formats a double to a string, removing the ".0" for whole numbers.
     */
    private fun formatNumber(number: Double?): String {
        if (number == null || number.isNaN() || number.isInfinite()) return "Error"

        // Tambahkan format untuk Infinity (yang datang dari faktorial > 170)
        if (number.isInfinite()) return "Error"

        return if (number % 1 == 0.0 && number.toLong() in Long.MIN_VALUE..Long.MAX_VALUE) {
            number.toLong().toString()
        } else {
            "%.10f".format(number).trimEnd('0').trimEnd('.')
        }
    }
}