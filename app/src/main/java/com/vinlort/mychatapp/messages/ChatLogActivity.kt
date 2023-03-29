package com.vinlort.mychatapp.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.vinlort.mychatapp.NewMessageActivity
import com.vinlort.mychatapp.R
import com.vinlort.mychatapp.databinding.ActivityChatLogBinding
import com.vinlort.mychatapp.models.ChatMessage
import com.vinlort.mychatapp.models.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item


class ChatLogActivity : AppCompatActivity() {

    companion object {
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()
    var toUser: User? = null

    private lateinit var binding: ActivityChatLogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerviewChatLog.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        supportActionBar?.title = toUser?.username
        //setupDummyData()
        listenForMessages()

        binding.sendButtonChatLog.setOnClickListener {
            Log.d(TAG, "Attempt to send message...")
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser
                        adapter.add(ChatToItem(chatMessage.text, currentUser!!))
                    } else {
                        //val currentUser = LatestMessagesActivity.currentUser
                        adapter.add(ChatFromItem(chatMessage.text, toUser!!))
                    }
                }
                binding.recyclerviewChatLog.scrollToPosition(adapter.itemCount -1)
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    private fun performSendMessage() {
        //send message to firebase db

        val textForMassage = binding.edittextChatLog.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        val toId = user?.uid
        if (fromId == null) return
        if (toId == null) return

        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()


        val chatMessage = ChatMessage(
            reference.key!!,
            textForMassage,
            fromId,
            toId,
            System.currentTimeMillis() / 1000
        )
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved message: ${reference.key}")
                binding.edittextChatLog.text.clear()
                binding.recyclerviewChatLog.scrollToPosition(adapter.itemCount - 1)
            }

        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
    }
}

class ChatFromItem(val text: String, val user: User) : Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_from_row).text = text

        val uri = user.profileImageUrl
        val targetImageView =
            viewHolder.itemView.findViewById<ImageView>(R.id.imageview_chat_from_row)
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

}

class ChatToItem(val text: String, val user: User) : Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_to_row).text = text

        //load user image
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageview_chat_to_row)
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}
class Chat1ToItem(val text: String, val currentUser: User) : Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_to_row).text = text

        //load user image
        val uid = currentUser.uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    val uri = user.profileImageUrl
                    val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageview_chat_to_row)
                    Picasso.get().load(uri).into(targetImageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatToItem", "Failed to retrieve user data from Firebase: ${error.message}")
            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}
