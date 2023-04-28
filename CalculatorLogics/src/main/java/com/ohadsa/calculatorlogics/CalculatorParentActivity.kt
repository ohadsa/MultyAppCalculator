package com.ohadsa.calculatorlogics

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.Group
import com.ohadsa.calculatorlogics.calculator.Calculator
import com.ohadsa.calculatorlogics.databinding.ActivityComplexBinding
import com.ohadsa.calculatorlogics.databinding.ActivitySimpleBinding
import com.ohadsa.calculatorlogics.extenssions.displayFormatting
import com.ohadsa.calculatorlogics.models.CalculatorBrain
import com.ohadsa.calculatorlogics.models.ScientificCalculator


abstract class CalculatorParentActivity : AppCompatActivity() {

    abstract fun getCalculatorType(): CalculatorType
    private var _expressionsBinding: ActivityComplexBinding? = null
    private val expressionsBinding: ActivityComplexBinding
        get() = _expressionsBinding!!

    private var _simpleBinding: ActivitySimpleBinding? = null
    private val simpleBinding: ActivitySimpleBinding
        get() = _simpleBinding!!

    private lateinit var extCalculator: Calculator
    private lateinit var simpleCalculator: CalculatorBrain
    private lateinit var operations: List<String>
    private lateinit var digitsAndDot: List<String>
    private var edText: String
        get() = findViewById<TextView>(R.id.edit_text).text.toString()
        set(value) {
            findViewById<TextView>(R.id.edit_text).text = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (getCalculatorType()) {
            CalculatorType.Expressions -> {
                extCalculator = Calculator()
                _expressionsBinding = ActivityComplexBinding.inflate(layoutInflater)
                setContentView(expressionsBinding.root)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                val buttons: List<Button> =
                    (findViewById<Group>(R.id.all_group).referencedIds.map(this::findViewById))
                buttons.forEach { btn -> btn.setOnClickListener{buttonsRouter(btn)} }
                val controllers: List<Button> =
                    (findViewById<Group>(R.id.controllers_group).referencedIds.map(this::findViewById))
                controllers.forEach { it.setOnClickListener(this::controllerRouter) }
                expressionsBinding.buttonEquals.setOnClickListener {
                    try {
                        if (edText != "") {
                            if (extCalculator.hasMem()) expressionsBinding.buttonAc.text =
                                ControllerButtons.C.value
                            expressionsBinding.display.editResult.text =
                                extCalculator.calculate(edText.trim()).displayFormatting()
                        }
                    } catch (e: Exception) {
                        expressionsBinding.display.editText.setTextColor(Color.rgb(180, 0, 0))
                    }
                }
            }
            CalculatorType.Simple -> {
                simpleCalculator = ScientificCalculator()
                _simpleBinding = ActivitySimpleBinding.inflate(layoutInflater)
                setContentView(simpleBinding.root)
                operations = simpleBinding.opGroup.referencedIds.map { "${findViewById<Button>(it).text}" }
                digitsAndDot = simpleBinding.digitGroup.referencedIds.map { "${findViewById<Button>(it).text}" }
                val buttons: List<Button> = (simpleBinding.flowBtn.referencedIds.map(this::findViewById))
                buttons.forEach { btn -> btn.setOnClickListener{buttonsRouter(btn)} }
            }
        }

    }

    enum class CalculatorType {
        Expressions, Simple
    }


    private fun controllerRouter(view: View) {
        if (getCalculatorType() == CalculatorType.Simple) return
        when ("${(view as Button).text}") {
            ControllerButtons.AC.value -> {
                edText = ""
                extCalculator.deleteMem()
                expressionsBinding.display.editResult.text = ""
            }
            ControllerButtons.C.value -> {
                edText = ""
                expressionsBinding.buttonAc.text = ControllerButtons.AC.value
            }
            ControllerButtons.UNDO.value -> {
                val tmp = extCalculator.unDo()
                edText = tmp.first
                expressionsBinding.display.editResult.text = tmp.second
            }
            ControllerButtons.REDO.value -> {
                val tmp = extCalculator.reDo()
                edText = tmp.first
                expressionsBinding.display.editResult.text = tmp.second
            }
            ControllerButtons.ANS.value -> edText += extCalculator.currentValue.second
            ControllerButtons.DELETE.value -> {
                edText = if (edText.isNotEmpty()) edText.substring(0, edText.length - 1) else ""
                expressionsBinding.display.editText.setTextColor(Color.parseColor("#C8C5BF"))
            }
        }
        if (edText.isNotEmpty()) expressionsBinding.buttonAc.text = ControllerButtons.C.value
    }

    private fun buttonsRouter(button: Button) {
        when (getCalculatorType()){
            CalculatorType.Expressions -> {
                val op = "${button.text}"
                val pair = fromOpViewsToOpExpression(op)
                edText += pair.first
                expressionsBinding.display.editText.setTextColor(Color.parseColor("#C8C5BF"))
                if (edText.isNotEmpty()) expressionsBinding.buttonAc.text = ControllerButtons.C.value
            }
            CalculatorType.Simple -> {
                val result: String?
                val op = "${button.text}"
                result = when (op) {
                    in digitsAndDot -> simpleCalculator.digitClicked(op, simpleBinding.editResult.text.toString())
                    in operations -> simpleCalculator.operationClicked(op, simpleBinding.editResult.text.toString())
                    else -> throw RuntimeException("Input error")
                }
                simpleBinding.editResult.text = result
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _expressionsBinding = null
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        when(getCalculatorType()){
            CalculatorType.Expressions -> {
                extCalculator = (
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                            savedInstanceState.getParcelable("calculator", Calculator::class.java)
                        else
                            savedInstanceState.getParcelable("calculator")
                        ) ?: Calculator()
                edText = savedInstanceState.getString("editor") ?: ""
                expressionsBinding.display.editResult.text = savedInstanceState.getString("display") ?: ""
                expressionsBinding.buttonAc.text = savedInstanceState.getString("ac_State") ?: "AC"
            }
            CalculatorType.Simple ->{
                simpleCalculator.state = savedInstanceState
                simpleBinding.editResult.text  = savedInstanceState.getString("display", simpleBinding.editResult.text.toString()) ?: "0"
            }
        } }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        when(getCalculatorType()){
            CalculatorType.Expressions -> {
                outState.putParcelable("calculator", extCalculator)
                outState.putString("editor", edText)
                outState.putString("display", expressionsBinding.display.editResult.text.toString())
                outState.putString("ac_State", expressionsBinding.buttonAc.text.toString())

            }
            CalculatorType.Simple -> {
                outState.putAll(simpleCalculator.state)
                outState.putString("display",  simpleBinding.editResult.text.toString())
            }
        }
}

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (getCalculatorType() == CalculatorType.Simple) return
            // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            Log.d("motionActivity", "landscape")
            findViewById<Flow>(R.id.flow_btn).setMaxElementsWrap(8)
            findViewById<MotionLayout>(R.id.motion_base).transitionToEnd()
        }
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d("motionActivity", "portrait")
            findViewById<Flow>(R.id.flow_btn).setMaxElementsWrap(4)
            findViewById<MotionLayout>(R.id.motion_base).transitionToStart()
        }
    }
}


enum class ControllerButtons(val value: String) {
    AC("AC"), C("C"), UNDO("◁"), REDO("▷"), DELETE("⌦"), ANS("ans");
}

fun fromOpViewsToOpExpression(value: String): Pair<String, String> {
    val map = mutableMapOf<String, Pair<String, String>>()


    map["0"] = Pair("0", ".")
    map["1"] = Pair("1", ".")
    map["2"] = Pair("2", ".")
    map["3"] = Pair("3", ".")
    map["4"] = Pair("4", ".")
    map["5"] = Pair("5", ".")
    map["6"] = Pair("6", ".")
    map["7"] = Pair("7", ".")
    map["8"] = Pair("8", ".")
    map["9"] = Pair("9", ".")
    map["."] = Pair(".", ".")
    map["e"] = Pair("e", "e")
    map["eˣ"] = Pair("e^", "e^")
    map["2ˣ"] = Pair("2^", "2^")
    map["π"] = Pair("π", "π")
    map["√"] = Pair("√", "√")
    map["∛"] = Pair("∛", "∛")
    map["%"] = Pair("%", "%")
    map["x²"] = Pair("²", "²")
    map["x³"] = Pair("³", "³")
    map["x⁻¹"] = Pair("⁻", "⁻")
    map["sin"] = Pair("sin", "sin")
    map["tan"] = Pair("tan", "tan")
    map["cos"] = Pair("cos", "cos")
    map["ln"] = Pair("ln", "ln")
    map["log"] = Pair("log", "log")
    map["!"] = Pair("!", "!")
    map["-/+"] = Pair("˗", "˗")
    map["+"] = Pair("+", "+")
    map["-"] = Pair("-", "-")
    map["x"] = Pair("x", "x")
    map["÷"] = Pair("÷", "÷")
    map["xʸ"] = Pair("^", "^")
    map[")"] = Pair(")", ")")
    map["("] = Pair("(", "(")

    return map[value] ?: Pair("", "")

}

