package com.vinlort.mychatapp.views

import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.vinlort.mychatapp.R
import com.vinlort.mychatapp.models.ChatMessage
import com.vinlort.mychatapp.models.User
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class LatestMessageRow(val chatMessage: ChatMessage): Item<GroupieViewHolder>(){
    var chatPartnerUser: User? = null
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val strKeyAES = Base64.decode(chatMessage.keyAES, Base64.DEFAULT)
        val strIV = Base64.decode(chatMessage.iv, Base64.DEFAULT)
        val decrMessage = decrypt(chatMessage.text,strKeyAES,strIV)
        viewHolder.itemView.findViewById<TextView>(R.id.textview_latestmessage_latest_message_row).text = decrMessage
        val chatPartnerId: String
        if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
            chatPartnerId = chatMessage.toId
        }else{
            chatPartnerId = chatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                chatPartnerUser = p0.getValue(User::class.java)
                viewHolder.itemView.findViewById<TextView>(R.id.textview_username_latest_message_row).text = chatPartnerUser?.username
                val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageview_latest_message_row)
                Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }
    fun decrypt(textToDecrypt: String, keyString: ByteArray, ivString: ByteArray): String {
        val key = SecretKeySpec(keyString, "AES")
        val iv = IvParameterSpec(ivString)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, iv)
        val decryptedBytes = cipher.doFinal(Base64.decode(textToDecrypt, Base64.NO_WRAP))
        return String(decryptedBytes, Charsets.UTF_8)
    }
    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
}