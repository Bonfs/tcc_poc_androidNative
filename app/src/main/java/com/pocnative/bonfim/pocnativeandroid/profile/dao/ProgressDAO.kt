package com.pocnative.bonfim.pocnativeandroid.profile.dao

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.pocnative.bonfim.pocnativeandroid.profile.model.User

fun createUserProfile(user: User, onComplete: () -> Unit){
    val mAuth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()
    val userProfileReference = database.getReference("debug/users/profile/${mAuth.currentUser?.uid}")


    // TODO mudar database profile
    database.getReference("debug/users").child(mAuth.currentUser?.uid!!).child("infos").setValue(user).addOnCompleteListener { onComplete() }
    //userProfileReference.setValue(user)
            //.addOnCompleteListener { onComplete() }
}