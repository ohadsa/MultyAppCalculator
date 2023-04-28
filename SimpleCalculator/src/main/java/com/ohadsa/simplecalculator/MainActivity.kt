package com.ohadsa.simplecalculator

import com.ohadsa.calculatorlogics.CalculatorParentActivity

class MainActivity : CalculatorParentActivity() {

    override fun getCalculatorType(): CalculatorType {
        return CalculatorType.Simple
    }
    
}

