package com.pocnative.bonfim.pocnativeandroid.utils

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(applicationContext, text, duration).show()
}

fun AppCompatActivity.toActivity(c: Class<*>, intent: Intent = Intent(this.baseContext, c)) {
    this.startActivity(intent)
}