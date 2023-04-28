package com.ohadsa.calculatorlogics.extenssions

import com.ohadsa.calculatorlogics.calculator.operations

fun String.isLetterOrNumber(): Boolean {
    return isLetter() || isNumber()
}

fun String.isLetter(): Boolean {
    if (operations[this] != null) return false
    this.forEach { if (!it.isLetter()) return false }
    return true
}

fun String.isDot(): Boolean {
    return "." == this
}

fun String.isNumber(): Boolean {
    try {
        this.toDouble()
        return true
    } catch (e: Exception) {
        return false
    }
}

fun String.isOperation(): Boolean {
    return operations[this] != null
}

fun String.displayFormatting(): String {
    return this
}


//²
fun String.toEquationList(): List<String> { //from 1+3*3 to list[1,+,3,*,3]
    val equation = mutableListOf<String>()
    this.forEach { equation.add("$it") }
    var index = 0
    while (index < equation.size - 1) {
        if (equation[index].isOperation()) {
        } else {
            if (equation[index].isLetter() && (equation[index + 1].isLetter() )) {
                equation[index + 1] = equation[index] + equation[index + 1]
                equation.removeAt(index--)
            } else if (equation[index].isNumber() && (equation[index + 1].isNumber() || equation[index + 1].isDot())) {
                equation[index + 1] = equation[index] + equation[index + 1]
                equation.removeAt(index--)

            }
        }
        index++
    }

    return equation
}

