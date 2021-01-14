package com.example.whatsappclone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.utils.formatAsTime
import kotlinx.android.synthetic.main.list_item_chat_recv_msg.view.*
import kotlinx.android.synthetic.main.list_item_date_header.view.*

class ChatAdapter(private val list: MutableList<ChatEvent> , private val mCurrentUid: String): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflate = { layout: Int ->
            LayoutInflater.from(parent.context).inflate(layout , parent , false)
        }
        return when(viewType) {
            TEXT_MESSAGE_RECEIVED -> {
                MessageViewHolder(inflate(R.layout.list_item_chat_recv_msg))
            }
            TEXT_MESSAGE_SENT ->  {
                MessageViewHolder(inflate(R.layout.list_item_chat_sent_msg))
            }
            DATE_HEADER -> {
                DateViewHolder(inflate(R.layout.list_item_date_header))
            }
           else -> MessageViewHolder(inflate(R.layout.list_item_chat_recv_msg))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val item = list[position]) {
            is DataHeader -> {
                holder.itemView.textView.text = item.date
            }
            is Message -> {
                holder.itemView.apply {
                    content.text = item.msg
                    time.text = item.sentAt.formatAsTime()
                }
            }
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {  // This function tell us that at which position which view we have to show.
        return when (val event = list[position]) {
            is Message -> {
                if(event.senderId == mCurrentUid) {
                    TEXT_MESSAGE_SENT
                } else {
                    TEXT_MESSAGE_RECEIVED
                }
            }
            is DataHeader -> DATE_HEADER
            else -> UNSUPPORTED
        }
    }

    class DateViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class MessageViewHolder(view: View): RecyclerView.ViewHolder(view)

    companion object { // static variables
        private const val UNSUPPORTED = -1
        private const val TEXT_MESSAGE_RECEIVED = 0
        private const val TEXT_MESSAGE_SENT = 1
        private const val DATE_HEADER = 2
    }
}