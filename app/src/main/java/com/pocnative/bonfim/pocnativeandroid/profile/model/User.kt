package com.pocnative.bonfim.pocnativeandroid.profile.model

class User(var weight: Long, var height: Int, var age: Int, var gender: Gender){

    // TODO: more accurate BMI calculator
    fun getBMI(): Long = (weight/height) * height
}

enum class Gender(val gender: String){
    MALE("Male"),
    FEMALE("Female")
}