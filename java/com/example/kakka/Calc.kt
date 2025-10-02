package com.example.kakka

import java.util.Stack
import kotlin.math.*


class Calc {
    private var currExpression: String = "0"

    private val BINARY_OPERATORS = listOf("+", "-", "x", "/", "^")
    private val ALL_TRIG_FUNCTIONS = listOf("sin", "cos", "tan", "asin", "acos", "atan")
    private val OTHER_FUNCTIONS = listOf("log", "ln", "sqrt")

    fun onDigit(digit: String) {
        if (currExpression == "Error") {
            currExpression = "0"
        }

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

                if (lastChar == "(") {
                    if (operator != "-") {
                        return
                    }
                }

                if (isLastCharOperator) {
                    if (operator == "-" && lastChar != "-") {

                    } else {
                        return
                    }
                }

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


                currExpression += operator
            }

        }
    }


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
            try {
                val allTokens = tokenize(currExpression)

                if (allTokens.isEmpty() || allTokens.last().toDoubleOrNull() == null) {
                    throw IllegalArgumentException("Operand missing or not a single number.")
                }

                val operandString = allTokens.last()

                val newPostfixNotation = when (internalFunction) {
                    "!" -> "$operandString!"
                    "%" -> "$operandString%"
                    "1/x" -> "1/($operandString)"
                    else -> ""
                }

                val newExpression = currExpression.dropLast(operandString.length) + newPostfixNotation

                currExpression = newExpression

            } catch (e: Exception) {
                currExpression = "Error"
            }

        } else {
            val lastChar = currExpression.lastOrNull()?.toString()

            if (currExpression != "0" && lastChar !in BINARY_OPERATORS ) {
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




    private fun evaluateExpression(expression: String): Double {
        val tokens = tokenize(expression)
        val rpnTokens = shuntingYard(tokens)
        return evaluateRPN(rpnTokens)
    }


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
                char == '-' && (i == 0 || expression[i - 1].toString() in (BINARY_OPERATORS + listOf("("))) -> {

                    val nextCharIndex = i + 1
                    var isNextCharFunction = false

                    for (func in ALL_TRIG_FUNCTIONS + OTHER_FUNCTIONS) {
                        if (expression.length > nextCharIndex && expression.substring(nextCharIndex).startsWith(func)) {
                            isNextCharFunction = true
                            break
                        }
                    }

                    val isNextCharParen = nextCharIndex < expression.length && expression[nextCharIndex] == '('

                    if (isNextCharFunction || isNextCharParen) {
                        tokens.add("-1")
                        tokens.add("x")
                        i++
                    } else {
                        var numberBuffer = "-"
                        i++
                        while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                            numberBuffer += expression[i]
                            i++
                        }
                        tokens.add(numberBuffer)
                    }
                }
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


                        if (isFunction(stackPeek)) {
                            outputQueue.add(operatorStack.pop())
                            continue
                        }

                        if (stackPeek == "(") break

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


    private fun factorial(n: Double): Double {
        if (n < 0 || n != n.toInt().toDouble()) return Double.NaN

        val nInt = n.toInt()

        if (nInt > 170) return Double.POSITIVE_INFINITY

        if (nInt == 0) return 1.0

        var result = 1.0
        for (i in 1..nInt) {
            result *= i
        }
        return result
    }


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