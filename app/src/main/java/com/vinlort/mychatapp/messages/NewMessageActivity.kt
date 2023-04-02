package com.vinlort.mychatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.vinlort.mychatapp.databinding.ActivityNewMessageBinding
import com.vinlort.mychatapp.messages.ChatLogActivity
import com.vinlort.mychatapp.models.User
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

private lateinit var binding: ActivityNewMessageBinding
class NewMessageActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Select User"

        fetchUsers()
    }

    companion object{
        val USER_KEY = "USER_KEY"
    }
    private fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupieAdapter()

                p0.children.forEach{
                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null){
                        adapter.add(UserItem(user))
                    }
                }
                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem
                    val intent = Intent(view.context,ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)
                    finish()
                }
                binding.recyclerViewNewMessage.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}

class UserItem(val user: User): Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        if(user.uid == FirebaseAuth.getInstance().uid){
            viewHolder.itemView.findViewById<TextView>(R.id.username_textview_new_message).text = "My Notes"
            val drawableResId = R.drawable.mynotes
            Picasso.get().load(drawableResId).into(viewHolder.itemView.findViewById<ImageView>(R.id.imageview_new_message))
        } else {
            viewHolder.itemView.findViewById<TextView>(R.id.username_textview_new_message).text = user.username
            Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.imageview_new_message))
        }
    }
    override fun getLayout():Int {
        return R.layout.user_row_new_message
    }

}