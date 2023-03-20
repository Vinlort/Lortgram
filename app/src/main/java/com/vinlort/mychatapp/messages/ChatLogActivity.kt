package com.vinlort.mychatapp.messages
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.vinlort.mychatapp.R
import com.vinlort.mychatapp.databinding.ActivityChatLogBinding
import com.vinlort.mychatapp.models.User
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item


class ChatLogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatLogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Chat Log"
        val adapter = GroupieAdapter()

        adapter.add(ChatFromItem())
        adapter.add(ChatFromItem())
        adapter.add(ChatToItem())
        adapter.add(ChatFromItem())
        adapter.add(ChatToItem())
        adapter.add(ChatToItem())

        binding.recyclerviewChatLog.adapter = adapter
    }
}

class ChatFromItem: Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        //viewHolder.itemView.findViewById<TextView>(R.id.username_textview_new_message).text = user.username
        //Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.imageview_new_message))
    }
    override fun getLayout():Int {
        return R.layout.chat_from_row
    }

}

class ChatToItem: Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        //viewHolder.itemView.findViewById<TextView>(R.id.username_textview_new_message).text = user.username
        //Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.imageview_new_message))
    }
    override fun getLayout():Int {
        return R.layout.chat_to_row
    }

}