package com.ohadsa.calculatorlogics.models


import android.os.Bundle

abstract class CalculatorBrain(private val operations: MutableMap<String, MathOperation>) {
    // mathematics operations in the calculator
    // props ->
    private var memoryCalculator = CalcMemory()// struct-> display, pending: Double, operand: String
    private var prevPress = PreviousPressType.NUM //know the last taped to ignore exec multiple op
    private var existPendingOperation = false //can be only binary operation
    private var isEditingMode = false // field that show when to start write new number

    init {
        this.operations["="] = MathOperation.Equals
    }
    //calculate props from memoryCalculator - can be useful for formatting and alike
    private var pendingValue: Double
        get() = this.memoryCalculator.pendingValue
        set(value) {
            this.memoryCalculator.pendingValue = value
        }

    private var displayedValue: Double
        get() = this.memoryCalculator.displayedValue
        set(value) {
            this.memoryCalculator.displayedValue = value
        }

    private var operand: String
        get() = this.memoryCalculator.operand
        set(value) {
            this.memoryCalculator.operand = value
        }

    // for switching between portrait and landscape, build and extract bundle from get/set
    var state: Bundle
        get() {
            val bundle = Bundle()
            bundle.putDouble("displayedValue", displayedValue)
            bundle.putDouble("pendingValue", pendingValue)
            bundle.putString("operand", operand)
            bundle.putInt("prevPress", prevPress.ordinal)
            bundle.putBoolean("existPendingOperation", existPendingOperation)
            bundle.putBoolean("isEditingMode", isEditingMode)
            return bundle
        }
        set(value) {
            displayedValue = value.getDouble("displayedValue")
            pendingValue = value.getDouble("pendingValue")
            operand = value.getString("operand") ?: "="
            prevPress = PreviousPressType.values()[value.getInt("prevPress")]
            existPendingOperation = value.getBoolean("existPendingOperation")
            isEditingMode = value.getBoolean("isEditingMode")
        }


    /*
    the only public methods digitClicked, operationClicked
    to update the memory and execute the operations . returns state -> the display
    can be 2 ways to tap in calculator -
        digit or dot clicked - to creating number
        operation clicked - to manipulate numbers
     */
    fun digitClicked(digit: String, _display: String): String {/*handling zero and dot here*/
        prevPress = PreviousPressType.NUM
        if (digit == ".") return if (!_display.contains('.')) "$_display." else _display
        val display = if (isEditingMode) _display + digit else digit
        isEditingMode = true
        return display
    }

    fun operationClicked(newOperation: String, _display: String): String {
        val op = operations[newOperation] ?: throw RuntimeException("Operator $operand not exist")
        var display = _display.trim()
        if (existPendingOperation) display = performPendingOperation(display)
        when (op) {
            is MathOperation.Equals -> equalsOpMemoryHandler(display)
            is MathOperation.Binary -> binaryOpMemoryHandler(display, newOperation)
            is MathOperation.ConstC -> constOpMemoryHandler(display, newOperation)
            is MathOperation.Unary -> unaryOpMemoryHandler(display, newOperation)
        }
        isEditingMode = false
        return "$displayedValue"
    }

    /*
    private methods that helps  operationClicked() to update memory state
    in every operations that enter( bin , un , const, equals) memory change different
    */
    private fun unaryOpMemoryHandler(_display: String, newOperation: String) {
        operand = newOperation
        displayedValue = performOperation(_display)
        pendingValue = displayedValue
        prevPress = PreviousPressType.OP
    }

    private fun constOpMemoryHandler(_display: String, newOperation: String) {
        if (existPendingOperation) {
            val tmpOp = operand
            operand = newOperation
            displayedValue = performOperation(_display)
            operand = tmpOp
        } else {
            operand = newOperation
            displayedValue = performOperation(_display)
        }
        prevPress = PreviousPressType.CONST
    }

    private fun binaryOpMemoryHandler(_display: String, newOperation: String) {
        operand = newOperation
        pendingValue = _display.toDouble()
        displayedValue = pendingValue
        existPendingOperation = true
        prevPress = PreviousPressType.OP
    }

    private fun equalsOpMemoryHandler(_display: String) {
        pendingValue = _display.toDouble()
        displayedValue = pendingValue
        prevPress = PreviousPressType.EQUALS
    }

    /*
    perform op and pending op
     */
    private fun performPendingOperation(_display: String): String {
        if (prevPress != PreviousPressType.OP) {
            displayedValue = performOperation(_display)
            pendingValue = _display.toDouble()
            existPendingOperation = false
            return "$displayedValue"
        }
        return _display
    }

    private fun performOperation(display: String): Double {
        val op = operations[operand] ?: throw RuntimeException("Operator $operand not exist")
        val result: Double = when (op) {
            is MathOperation.ConstC -> op.op.constFunc()
            is MathOperation.Unary -> op.op.unFunc(display.toDouble())
            is MathOperation.Binary -> op.op.binFunc(pendingValue, display.toDouble())
            is MathOperation.Equals -> display.toDouble()
        }
        return result
    }
}


enum class PreviousPressType { OP, EQUALS, NUM, CONST }

data class CalcMemory(
    //initial default memory , put performPending inside this class
    var displayedValue: Double = 0.0,
    var pendingValue: Double = 0.0,
    var operand: String = "=",
)

sealed class MathOperation {
    class Binary(val op: BinFunc) : MathOperation()
    class Unary(val op: UnaryFunc) : MathOperation()
    class ConstC(val op: ConstFunc) : MathOperation()
    object Equals : MathOperation()

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








