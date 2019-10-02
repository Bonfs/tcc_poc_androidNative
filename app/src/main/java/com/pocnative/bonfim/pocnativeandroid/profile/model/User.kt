package com.pocnative.bonfim.pocnativeandroid.profile.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class User() {
    var weight: Long = 0
    var height: Long = 0
    var age: Long = 0
    lateinit var gender: Gender

    constructor(weight: Long, height: Long, age: Long, gender: Gender) : this() {
        this.weight = weight
        this.height = height
        this.age = age
        this.gender = gender
    }
    // TODO: more accurate BMI calculator
    @Exclude
    fun getBMI(): Long = (weight/height) * height
}

enum class Gender(val gender: String){
    MALE("Male"),
    FEMALE("Female")
}