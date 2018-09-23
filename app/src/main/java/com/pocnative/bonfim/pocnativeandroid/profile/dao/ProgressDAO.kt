package com.pocnative.bonfim.pocnativeandroid.profile.dao

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.pocnative.bonfim.pocnativeandroid.profile.model.User

fun createUserProfile(user: User, onComplete: () -> Unit){
    val mAuth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()
    val userProfileReference = database.getReference("users/profile/${mAuth.currentUser?.uid}")


    database.getReference("users").child(mAuth.currentUser?.uid!!).child("progress").setValue(user).addOnCompleteListener { onComplete() }
    //userProfileReference.setValue(user)
            //.addOnCompleteListener { onComplete() }
}