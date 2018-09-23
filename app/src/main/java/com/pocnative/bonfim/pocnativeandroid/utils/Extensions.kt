package com.pocnative.bonfim.pocnativeandroid.utils

import android.app.Activity
import android.content.Intent
import android.widget.Toast

fun Activity.showToast(text: String, duration: Int = Toast.LENGTH_SHORT){
    Toast.makeText(applicationContext, text, duration).show()
}

fun Activity.toActivity(c: Class<*>, intent: Intent = Intent(this.baseContext, c)){
    this.startActivity(intent)
}