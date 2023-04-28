package com.ohadsa.multyappcalculator

import com.ohadsa.calculatorlogics.CalculatorParentActivity

class MainActivity : CalculatorParentActivity() {

    override fun getCalculatorType(): CalculatorType {
        return CalculatorType.Expressions
    }

}

