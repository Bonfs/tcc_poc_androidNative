package com.pocnative.bonfim.pocnativeandroid.auth

import com.google.firebase.auth.FirebaseAuth

class AuthManager {

    companion object {
        //check if is logged
        @JvmStatic
        fun isLogged(): Boolean{
            val mAuth = FirebaseAuth.getInstance()
            return mAuth.currentUser != null
        }

        // create a anonymous user
        @JvmStatic
        fun createAnonymousUser(successCallback: () -> Unit, errorCallback: (() -> Unit)?){
            val mAuth = FirebaseAuth.getInstance()
            mAuth.signInAnonymously().addOnCompleteListener{
                if (it.isSuccessful){
                    successCallback()
                } else{
                    errorCallback?.invoke() ?: throw NullPointerException("no callback for anonymous login")
                }
            }
        }
    }
}