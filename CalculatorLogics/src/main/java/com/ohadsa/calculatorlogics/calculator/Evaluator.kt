package com.ohadsa.calculatorlogics.calculator

import com.ohadsa.calculatorlogics.extenssions.isLetterOrNumber
import com.ohadsa.calculatorlogics.extenssions.isNumber
import java.util.*
import kotlin.math.*


interface Evaluator {

    fun calculateExpression(exp: List<String>): String = postFixCalculate(infixToPostfix(exp))
    fun infixToPostfix(expression: List<String>): List<String> {
        val result = Stack<String>()
        val stack = Stack<String>()
        expression.forEach {
            if (operations[it] is MathOperation.Unary.UnaryPrefix) stack.push(it)
            else if (operations[it] is MathOperation.Unary.UnaryPost) result.add(it)
            else if (it.isLetterOrNumber()) {
                result.add(it)
                if (!stack.isEmpty() && operations[stack.peek()] is MathOperation.Unary.UnaryPrefix) result.add(
                    stack.pop()
                )
            } else if (it == "(") stack.push(it)
            else if (it == ")") {
                while (!stack.isEmpty() && stack.peek() != "(") result.add(stack.pop())
                stack.pop()
                if (!stack.isEmpty() && operations[stack.peek()] is MathOperation.Unary.UnaryPrefix) result.add(
                    stack.pop()
                )
            } else {
                while (!stack.isEmpty() && rankOfOperation(it) <= rankOfOperation(stack.peek()))
                    result.add(stack.pop())
                stack.push(it)
            }
        }
        while (!stack.isEmpty()) {
            if (stack.peek() == "(") return listOf()
            result.add(stack.pop())
        }
        return result
    }

    fun postFixCalculate(exp: List<String>): String {
        val lst = mutableListOf<String>()
        exp.forEach {
            if (operations[it] is MathOperation.ConstC) {
                lst.add((operations[it] as MathOperation.ConstC).op.constFunc().toString())
            } else lst.add(it)
        }
        val operations = listOfOperations()
        var result: String
        val stack = Stack<String>()
        lst.forEach {
            if (it.isNumber()) {
                stack.push(it)
            } else {
                result = when (val op = operations[it]) {
                    is MathOperation.Binary -> "${
                        op.op.binFunc(
                            stack.pop().toDouble(),
                            stack.pop().toDouble()
                        )
                    }"
                    is MathOperation.Unary -> "${op.op.unFunc(stack.pop().toDouble())}"
                    else -> throw RuntimeException()
                }
                stack.push(result)
            }
        }
        return stack.pop()
    }


    fun rankOfOperation(operation: String): Int {
        val operations = listOfOperations()
        try {
            return (operations[operation] as MathOperation.Binary).rank
        } catch (e: Exception) {
        }
        return -1
    }

}
sealed class MathOperation {
    class Binary(val rank: Int, val op: BinFunc) : MathOperation()
    open class Unary(val rank: Int, val op: UnaryFunc) : MathOperation() {
        class UnaryPrefix(rank: Int, op: UnaryFunc) : Unary(rank, op)
        class UnaryPost(rank: Int, op: UnaryFunc) : Unary(rank, op)
    }

    class ConstC(val op: ConstFunc) : MathOperation()


    fun interface BinFunc {
        fun binFunc(firstNum: Double, lastNum: Double): Double
    }

    fun interface UnaryFunc {
        fun unFunc(firstNum: Double): Double
    }

    fun interface ConstFunc {
        fun constFunc(): Double
    }
}


var operations = listOfOperations()
fun listOfOperations(): MutableMap<String, MathOperation> {
    val operations = mutableMapOf<String, MathOperation>()
    //const
    operations["e"] = MathOperation.ConstC { E }
    operations["π"] = MathOperation.ConstC { PI }
    //unary
    operations["√"] = MathOperation.Unary.UnaryPrefix(4) { x -> sqrt(x) }
    operations["∛"] = MathOperation.Unary.UnaryPrefix(4) { x -> x.pow(1.0 / 3.0) }
    operations["%"] = MathOperation.Unary.UnaryPrefix(4) { x -> x / 100 }
    operations["²"] = MathOperation.Unary.UnaryPost(4) { x -> x * x }
    operations["³"] = MathOperation.Unary.UnaryPost(4) { x -> x * x * x }
    operations["⁻"] = MathOperation.Unary.UnaryPost(4) { x -> 1.0 / x }
    operations["sin"] = MathOperation.Unary.UnaryPrefix(4) { x -> sin(x) }
    operations["tan"] = MathOperation.Unary.UnaryPrefix(4) { x -> tan(x) }
    operations["cos"] = MathOperation.Unary.UnaryPrefix(4) { x -> cos(x) }
    operations["ln"] = MathOperation.Unary.UnaryPrefix(4) { x -> ln(x) }
    operations["log"] = MathOperation.Unary.UnaryPrefix(4) { x -> log(x, 10.0) }
    operations["!"] = MathOperation.Unary.UnaryPost(4) { x ->
        1.rangeTo(x.toInt()).reduce { sum, y -> sum * y }.toDouble()
    }
    operations["˗"] = MathOperation.Unary.UnaryPrefix(4) { x -> -x }

    //binary
    operations["+"] = MathOperation.Binary(1) { x, y -> x + y }
    operations["-"] = MathOperation.Binary(1) { x, y -> y - x }
    operations["x"] = MathOperation.Binary(2) { x, y -> x * y }
    operations["÷"] = MathOperation.Binary(2) { x, y -> y / x }
    operations["^"] = MathOperation.Binary(3) { x, y -> y.pow(x) }

    return operations
}
