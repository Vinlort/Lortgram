package com.vinlort.mychatapp.registerlogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.vinlort.mychatapp.databinding.ActivityLoginBinding


class LoginActivity:AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.loginButton.setOnClickListener {
            val email = binding.editTextTextEmailAddress2.text.toString()
            val password = binding.editTextTextPassword2.text.toString()

            Log.d("RegisterActivity", "Email is " + email)
            Log.d("RegisterActivity", "Password is $password")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                //.addOnCompleteListener {  }
                //.addOnFailureListener {  }
        }
        binding.backTextView.setOnClickListener {
            finish()
        }
    }
}