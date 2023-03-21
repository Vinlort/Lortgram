package com.vinlort.mychatapp.views

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

class LatestMessageRow(val chatMessage: ChatMessage): Item<GroupieViewHolder>(){
    var chatPartnerUser: User? = null
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_latestmessage_latest_message_row).text = chatMessage.text
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
    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
}