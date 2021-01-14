package com.example.whatsappclone

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.utils.formatAsListItem
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat.view.*
import kotlinx.android.synthetic.main.activity_otp.view.*
import kotlinx.android.synthetic.main.list_item.view.*

class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(item: Inbox , onClick: (name: String , photo: String , id: String) -> Unit) =
        with(itemView) {
            countTextView.isVisible = item.count > 0
            countTextView.text = item.count.toString()
            timeTextView.text = item.time.formatAsListItem(context)

            titleTextView.text = item.name
            subtitleTextView.text = item.msg
            Picasso.get()
                .load(item.image)
                .placeholder(R.drawable.ic_baseline_account_circle_24)
                .error(R.drawable.ic_baseline_account_circle_24)
                .into(userImgView)

            setOnClickListener {
                onClick.invoke(item.name , item.image , item.from)
            }
        }
}