package com.vinlort.mychatapp.messages

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso
import com.vinlort.mychatapp.NewMessageActivity
import com.vinlort.mychatapp.R
import com.vinlort.mychatapp.databinding.ActivityChatLogBinding
import com.vinlort.mychatapp.models.ChatMessage
import com.vinlort.mychatapp.models.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

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

        if (toUser?.uid == FirebaseAuth.getInstance().uid){
            supportActionBar?.title = "My Notes"
        }
        else{
            supportActionBar?.title = toUser?.username
        }
        //setupDummyData()
        listenForMessages()

        binding.sendButtonChatLog.setOnClickListener {
            Log.d(TAG, "Attempt to send message...")
            performSendMessage()
        }
    }

    private val addedMessageIds = mutableSetOf<String>()

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if (chatMessage != null && chatMessage.id !in addedMessageIds) {
                    Log.d(TAG, chatMessage.text)
                    val strKeyAES = Base64.decode(chatMessage.keyAES, Base64.DEFAULT)
                    val strIV = Base64.decode(chatMessage.iv, Base64.DEFAULT)
                    val decrMessage = decrypt(chatMessage.text,strKeyAES,strIV)
                    val timeStr = setTime(chatMessage.timestamp)

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser
                        val state: Boolean = chatMessage.toId == FirebaseAuth.getInstance().uid
                        adapter.add(ChatToItem(decrMessage, currentUser!!, timeStr, state))
                    } else {
                        adapter.add(ChatFromItem(decrMessage, toUser!!, timeStr))
                    }
                    addedMessageIds.add(chatMessage.id)
                    binding.recyclerviewChatLog.scrollToPosition(adapter.itemCount -1)
                }
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

    private fun encrypt(textToEncrypt: String, keyString: ByteArray, ivString: ByteArray): String {
        val key = SecretKeySpec(keyString, "AES")
        val iv = IvParameterSpec(ivString)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        val encryptedBytes = cipher.doFinal(textToEncrypt.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }

    fun decrypt(textToDecrypt: String, keyString: ByteArray, ivString: ByteArray): String {
        val key = SecretKeySpec(keyString, "AES")
        val iv = IvParameterSpec(ivString)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, iv)
        val decryptedBytes = cipher.doFinal(Base64.decode(textToDecrypt, Base64.NO_WRAP))
        return String(decryptedBytes, Charsets.UTF_8)
    }

    private fun performSendMessage() {
        //send message to firebase db

        val textForMessage = binding.edittextChatLog.text.toString()
        if (textForMessage == "") return
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        val toId = user?.uid
        if (fromId == null) return
        if (toId == null) return

        val random = SecureRandom()
        val keyAES = ByteArray(16)
        val iv = ByteArray(16)
        random.nextBytes(keyAES)
        random.nextBytes(iv)
        val encMessage = encrypt(textForMessage,keyAES,iv)

        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val strKeyAES = Base64.encodeToString(keyAES, Base64.DEFAULT)
        val strIV = Base64.encodeToString(iv, Base64.DEFAULT)

        val chatMessage = ChatMessage(
            reference.key!!,
            encMessage,
            fromId,
            toId,
            System.currentTimeMillis() / 1000,
            strKeyAES,
            strIV
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

    fun setTime(unixTime: Long): String{
        val date = Date(unixTime * 1000L)
        val calendar: Calendar = Calendar.getInstance()
        calendar.setTime(date)
        val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
        val minute: Int = calendar.get(Calendar.MINUTE)
        val time: String = "${hour.toString()}:${minute.toString()}"
        return time
    }
}

class ChatFromItem(val text: String, val user: User, val timeStr: String) : Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_from_row).text = text
        viewHolder.itemView.findViewById<TextView>(R.id.textview_time_from_row).text = timeStr

        if (user.uid == FirebaseAuth.getInstance().uid){
            val targetImageView =
                viewHolder.itemView.findViewById<ImageView>(R.id.imageview_chat_from_row)
            val drawableResId = R.drawable.mynotes
            Picasso.get().load(drawableResId).into(targetImageView)
        } else {
            val uri = user.profileImageUrl
            val targetImageView =
                viewHolder.itemView.findViewById<ImageView>(R.id.imageview_chat_from_row)
            Picasso.get().load(uri).into(targetImageView)
        }

    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

}

class ChatToItem(val text: String, val user: User, val timeStr: String, val state: Boolean) : Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_to_row).text = text
        viewHolder.itemView.findViewById<TextView>(R.id.textview_time_to_row).text = timeStr

        if (state){
            val targetImageView =
                viewHolder.itemView.findViewById<ImageView>(R.id.imageview_chat_to_row)
            val drawableResId = R.drawable.mynotes
            Picasso.get().load(drawableResId).into(targetImageView)

        } else {
            val uri = user.profileImageUrl
            val targetImageView =
                viewHolder.itemView.findViewById<ImageView>(R.id.imageview_chat_to_row)
            Picasso.get().load(uri).into(targetImageView)
        }
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}
