package com.pocnative.bonfim.pocnativeandroid.models

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable
import kotlin.collections.ArrayList

@IgnoreExtraProperties
data class PhysicalActivity(
        var steps: Long = 0,
        var duration: Long = 0,
        var date: String = "",
        var startPosition: Map<String, Double> = mapOf(),
        var locations: ArrayList<Map<String, Double>> = arrayListOf(),
        var started: Boolean = false,
        var finished: Boolean = false
): Serializable