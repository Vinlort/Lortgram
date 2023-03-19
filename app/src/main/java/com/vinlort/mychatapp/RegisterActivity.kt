package com.vinlort.mychatapp

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.vinlort.mychatapp.databinding.ActivityRegisterBinding
import java.util.UUID


class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    var selectedPhotoUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            performRegister()
        }
        binding.haveAccountTextView.setOnClickListener {
            Log.d("RegisterActivity", "Try to show login page")

            // launch login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


        var activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == RESULT_OK && result.data != null) {
                Log.d("RegisterActivity", "Photo was selected")
                selectedPhotoUri = result.data!!.data
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
                val bitmapDrawable = BitmapDrawable(bitmap)
                binding.selectPhotoRegister.setBackgroundDrawable(bitmapDrawable)
                binding.selectPhotoRegister.text = ""
            }
        }

        binding.selectPhotoRegister.setOnClickListener {
            Log.d("RegisterActivity", "Try to show photo selector")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            //startActivityForResult(intent, 0)
            activityResultLauncher.launch(intent)
        }

    }

    private fun performRegister(){
        val email = binding.editTextTextEmailAddress.text.toString()
        val password = binding.editTextTextPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter your email and pw!", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                // else if successful
                Log.d("RegisterActivity", "Created user with uid: ${it.result.user?.uid}")
                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener{
                Log.d("RegisterActivity", "Failed to create user: ${it.message}")
                Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
            }

    }
    private  fun  uploadImageToFirebaseStorage(){
        if (selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Succesfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d("RegisterActivity", "File location: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener{
                //logs
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, binding.editTextTextPersonName.text.toString(),profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Finally ve saved the user to Firebase database")
            }

    }
}

class User(val uid: String, val username: String, val profileImageUrl: String)