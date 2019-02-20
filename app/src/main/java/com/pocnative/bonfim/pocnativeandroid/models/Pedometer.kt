package com.pocnative.bonfim.pocnativeandroid.models

data class Pedometer(val steps: Int) {
    var distance: Int? = null
    var calories: Int? = null
}