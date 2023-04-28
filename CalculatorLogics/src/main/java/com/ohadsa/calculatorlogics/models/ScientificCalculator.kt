package com.ohadsa.calculatorlogics.models

import kotlin.math.*

class ScientificCalculator : CalculatorBrain(allOps())

private fun allOps(): MutableMap<String, MathOperation> {
    val operations = mutableMapOf<String, MathOperation>()
    //const
    operations["AC"] = MathOperation.ConstC { 0.0 }
    operations["e"] = MathOperation.ConstC { E }
    operations["π"] = MathOperation.ConstC { PI }
    //unary
    operations["-/+"] = MathOperation.Unary { x -> -1 * x }
    operations["√"] = MathOperation.Unary { x -> sqrt(x) }
    operations["%"] = MathOperation.Unary { x -> x / 100 }
    operations["x²"] = MathOperation.Unary { x -> x * x }
    operations["x³"] = MathOperation.Unary { x -> x * x * x }
    operations["x⁻¹"] = MathOperation.Unary { x -> 1 / x }
    operations["sin"] = MathOperation.Unary { x -> sin(x) }
    operations["tan"] = MathOperation.Unary { x -> tan(x) }
    operations["cos"] = MathOperation.Unary { x -> cos(x) }
    operations["sin⁻¹"] = MathOperation.Unary { x -> asin(x) }
    operations["cos⁻¹"] = MathOperation.Unary { x -> acos(x) }
    operations["tan⁻¹"] = MathOperation.Unary { x -> atan(x) }
    operations["ln"] = MathOperation.Unary { x -> ln(x) }
    operations["eˣ"] = MathOperation.Unary { x -> E.pow(x) }
    //binary
    operations["+"] = MathOperation.Binary { x, y -> x + y }
    operations["-"] = MathOperation.Binary { x, y -> x - y }
    operations["x"] = MathOperation.Binary { x, y -> x * y }
    operations["÷"] = MathOperation.Binary { x, y -> x / y }
    operations["xʸ"] = MathOperation.Binary { x, y -> x.pow(y) }
    return operations
}