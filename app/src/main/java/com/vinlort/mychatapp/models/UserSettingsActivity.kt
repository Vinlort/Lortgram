package com.vinlort.mychatapp.models

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.vinlort.mychatapp.R
import com.vinlort.mychatapp.RegisterActivity
import com.vinlort.mychatapp.databinding.ActivityUserSettingsBinding
import com.vinlort.mychatapp.messages.LatestMessagesActivity
import java.util.*


class UserSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserSettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Створення адаптера та прив'язка до ListView
        val adapter = MenuItemAdapter(this)
        adapter.add("Change Username")
        adapter.add("Change Password")
        adapter.add("Logout")
        val listView = findViewById<ListView>(R.id.settings_list)
        listView.adapter = adapter

        // Обробка натискань на підпункти меню
        listView.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = adapter.getItem(position)
            when (selectedItem) {
                "Change Username" -> {
                    // Обробка вибору "Change Username"
                    val dialogBuilder = AlertDialog.Builder(this)
                    val editText = EditText(this)
                    editText.hint = "Enter new username"
                    dialogBuilder.setView(editText)
                    dialogBuilder.setTitle("Change Username")
                    dialogBuilder.setPositiveButton("Save") { _, _ ->
                        // Обробник події для кнопки Save в AlertDialog

                        val newUsername = editText.text.toString()

                        val uid = FirebaseAuth.getInstance().uid
                        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
                        ref.child("username").setValue(newUsername)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Username updated", Toast.LENGTH_SHORT).show()
                                fetchUserData()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error updating username", Toast.LENGTH_SHORT).show()
                            }

                    }
                    dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                        // Обробник події для кнопки Cancel в AlertDialog
                        dialog.cancel()
                    }
                    val dialog = dialogBuilder.create()
                    dialog.show()
                }
                "Change Password" -> {
                    // Обробка вибору "Change Password"
                    val dialogBuilder = AlertDialog.Builder(this)
                    val inflater = layoutInflater
                    val dialogView = inflater.inflate(R.layout.dialog_change_password, null)
                    val oldPasswordEditText = dialogView.findViewById<EditText>(R.id.current_password)
                    val newPasswordEditText = dialogView.findViewById<EditText>(R.id.new_password)
                    dialogBuilder.setView(dialogView)
                    dialogBuilder.setPositiveButton("Save") { _, _ ->
                        // Обробник події для кнопки Save в AlertDialog
                        val oldPassword = oldPasswordEditText.text.toString()
                        val newPassword = newPasswordEditText.text.toString()
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
                            user.reauthenticate(credential)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        user.updatePassword(newPassword)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(this, "Password update failed", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    } else {
                                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                    dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                        // Обробник події для кнопки Cancel в AlertDialog
                        dialog.cancel()
                    }
                    val dialog = dialogBuilder.create()
                    dialog.show()
                }
                "Logout" -> {
                    // Обробка вибору "Logout"
                    Toast.makeText(this, "Logout selected", Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, RegisterActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
        }


        val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri = data?.data
                if (selectedImageUri != null) {
                    updateUserProfileImage(selectedImageUri)
                }
            }
        }

        val profilePhoto = binding.userImage
        profilePhoto.setOnClickListener {
            // Обробка натискання на фото
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            activityResultLauncher.launch(intent)
        }
        fetchUserData()

    }

    private fun fetchUserData() {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val userRef = FirebaseDatabase.getInstance().getReference("/users/$uid")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentUser = snapshot.getValue(User::class.java)
                LatestMessagesActivity.currentUser = currentUser
                Log.d("LatestMessages", "Current user ${LatestMessagesActivity.currentUser?.profileImageUrl}")
                val uri = LatestMessagesActivity.currentUser?.profileImageUrl
                val targetImageView = binding.userImage
                Picasso.get().load(uri).into(targetImageView)

                val name = currentUser?.username
                Log.d("TAG", "User name: $name")
                binding.usernameText.text = name
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("TAG", "Failed to read user data from Firebase database: ${error.message}")
            }
        })
    }

    private fun updateUserProfileImage(newImageUri: Uri) {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val filename = UUID.randomUUID().toString()
        val storageRef = FirebaseStorage.getInstance().getReference("/images/$filename")

        storageRef.putFile(newImageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val userUpdates = HashMap<String, Any>()
                    userUpdates["profileImageUrl"] = uri.toString()

                    ref.updateChildren(userUpdates)
                        .addOnSuccessListener {
                            Log.d("UpdateProfileActivity", "Successfully updated user profile image")
                            Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show()
                            fetchUserData()
                        }
                        .addOnFailureListener {
                            Log.d("UpdateProfileActivity", "Failed to update user profile image: ${it.message}")
                            Toast.makeText(this, "Failed to update profile image: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Log.d("UpdateProfileActivity", "Failed to upload new image to storage: ${it.message}")
                Toast.makeText(this, "Failed to update profile image: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Розмітка для підпункту меню
    class MenuItemAdapter(context: Context) : ArrayAdapter<String>(context, R.layout.menu_item, R.id.menu_item_text) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            val menuItemText = view.findViewById<TextView>(R.id.menu_item_text)
            menuItemText.text = getItem(position)
            return view
        }
    }
}