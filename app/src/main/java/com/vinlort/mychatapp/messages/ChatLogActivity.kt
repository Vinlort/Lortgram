package com.vinlort.mychatapp.messages
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.vinlort.mychatapp.NewMessageActivity
import com.vinlort.mychatapp.R
import com.vinlort.mychatapp.databinding.ActivityChatLogBinding
import com.vinlort.mychatapp.models.User
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item


class ChatLogActivity : AppCompatActivity() {

    companion object{
        val TAG ="ChatLog"
    }

    private lateinit var binding: ActivityChatLogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NewMessageActivity.USER_KEY, User::class.java)
        } else {
            intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        }
        supportActionBar?.title = user?.username
        setupDummyData()

        binding.sendButtonChatLog.setOnClickListener {
            Log.d(TAG, "Attempt to send message...")
            performSendMessage()
        }
    }

    class ChatMessage(val id: String, val text: String, val fromId: String, val toId: String, val timestamp: Long)
    private fun performSendMessage(){
        //send message to firebase db

        val textForMassage = binding.edittextChatLog.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NewMessageActivity.USER_KEY, User::class.java)
        } else {
            intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        }
        val toId = user?.uid
        if (fromId == null) return
        if (toId == null) return
        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()

        val chatMessage = ChatMessage(reference.key!!, textForMassage, fromId, toId, System.currentTimeMillis()/1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved message: ${reference.key}")
            }
    }
    private fun setupDummyData(){
        val adapter = GroupieAdapter()

        adapter.add(ChatFromItem("from message tatatattatattatattata"))
        adapter.add(ChatFromItem("from message tatatattatattatattata"))
        adapter.add(ChatToItem("to message dododoodoodododoooo"))
        adapter.add(ChatFromItem("from message tatatattatattatattata"))
        adapter.add(ChatToItem("to message dododoodoodododoooo"))
        adapter.add(ChatToItem("to message dododoodoodododoooo"))

        binding.recyclerviewChatLog.adapter = adapter
    }
}

class ChatFromItem(val text: String): Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_from_row).text = text
        //Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.imageview_new_message))
    }
    override fun getLayout():Int {
        return R.layout.chat_from_row
    }

}

class ChatToItem(val text: String): Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_to_row).text = text
        //Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.imageview_new_message))
    }
    override fun getLayout():Int {
        return R.layout.chat_to_row
    }

}