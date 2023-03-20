package com.vinlort.mychatapp.messages

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.vinlort.mychatapp.NewMessageActivity
import com.vinlort.mychatapp.R
import com.vinlort.mychatapp.RegisterActivity
import com.vinlort.mychatapp.databinding.ActivityLatestMessagesBinding

class LatestMessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLatestMessagesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLatestMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        verifyUserIsLoggedIn()
    }

   private fun verifyUserIsLoggedIn(){

       val uid = FirebaseAuth.getInstance().uid
       if (uid == null) {
           val intent = Intent(this, RegisterActivity::class.java)
           intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
           startActivity(intent)
       }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId){
            R.id.menu_new_message ->{
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}