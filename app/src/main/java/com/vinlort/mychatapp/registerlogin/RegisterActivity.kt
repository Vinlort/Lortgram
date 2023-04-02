package com.vinlort.mychatapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.vinlort.mychatapp.databinding.ActivityRegisterBinding
import com.vinlort.mychatapp.messages.LatestMessagesActivity
import com.vinlort.mychatapp.models.User
import com.vinlort.mychatapp.registerlogin.LoginActivity
import java.util.UUID


class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private var selectedPhotoUri: Uri? = null
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
                binding.selectPhotoRegisterImg.setImageBitmap(bitmap)
                binding.selectPhotoRegister.alpha = 0f
            }
        }

        binding.selectPhotoRegister.setOnClickListener {
            Log.d("RegisterActivity", "Try to show photo selector")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            activityResultLauncher.launch(intent)
        }
    }

    private fun performRegister(){
        val email = binding.editTextTextEmailAddress.text.toString()
        val password = binding.editTextTextPassword.text.toString()
        val userName = binding.editTextTextPersonName.text.toString()

        when{
            email.isEmpty() ->{
                Toast.makeText(this, "Please enter your email!", Toast.LENGTH_SHORT).show()
                return
            }
            password.isEmpty() ->{
                Toast.makeText(this, "Please enter your password!", Toast.LENGTH_SHORT).show()
                return
            }
            userName == "" ->{
                Toast.makeText(this, "Please enter your username!", Toast.LENGTH_SHORT).show()
                return
            }
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
    private fun  uploadImageToFirebaseStorage(){

        if (selectedPhotoUri == null) {
            val defaultImageRef = FirebaseStorage.getInstance().getReference("/default/Sample_User_Icon.png")
            defaultImageRef.downloadUrl.addOnSuccessListener { uri ->
                saveUserToFirebaseDatabase(uri.toString())
            }.addOnFailureListener {
            }
        } else {
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
                }
        }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val userName = binding.editTextTextPersonName.text.toString()

        val user = User(uid, userName ,profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Saved the user to Firebase database")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

    }
}