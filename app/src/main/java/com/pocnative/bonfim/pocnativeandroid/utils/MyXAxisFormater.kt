package com.pocnative.bonfim.pocnativeandroid.utils

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

class MyXAxisFormater(var values: Array<String>) : ValueFormatter() {
    override fun getFormattedValue(index: Float, axis: AxisBase?): String {
        return values[index.toInt()]
    }

}

