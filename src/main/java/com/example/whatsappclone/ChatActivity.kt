package com.example.whatsappclone

import android.content.Context
import android.content.Intent
import com.example.whatsappclone.Inbox
import com.example.whatsappclone.Message
import com.example.whatsappclone.R
import com.example.whatsappclone.User
import kotlinx.android.synthetic.main.list_item.view.*



import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsappclone.utils.isSameDayAs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.android.synthetic.main.activity_chat.*


const val UID = "uid"
const val NAME = "name"
const val IMAGE = "photo"

class ChatActivity : AppCompatActivity() {

    private val friendId: String by lazy {
        intent.getStringExtra(UID).toString()
    }
    private val name: String by lazy {
        intent.getStringExtra(NAME).toString()
    }
    private val image: String by lazy {
        intent.getStringExtra(IMAGE).toString()
    }
    private val mCurrentUid: String by lazy {
        FirebaseAuth.getInstance().uid!!
    }
    private val db: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }
    lateinit var currentUser: User
    private val message = mutableListOf<ChatEvent>()
    lateinit var chatAdapter: ChatAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        setContentView(R.layout.activity_chat)

        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
            .addOnSuccessListener {
                currentUser = it.toObject(User::class.java)!!
            }
        chatAdapter = ChatAdapter(message ,  mCurrentUid)
        msgRv.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }
        nameTv.text = name
        Picasso.get().load(image).into(userImgView)
        listenToMessage()

        sendBtn.setOnClickListener {
            msgEdtv.text?.let {
                if(it.isNotEmpty()) {
                    sendMessage(it.toString())
                    it.clear()
                }
            }
        }

        updateReadCount()
    }

    private fun updateReadCount() {
        getInbox(mCurrentUid , friendId).child("count").setValue(0)
    }

    private fun listenToMessage() {
        getMessages(friendId)
            .orderByKey()
            .addChildEventListener(object: ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val msg = snapshot.getValue(Message::class.java)!!
                    addMessage(msg)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    TODO("Not yet implemented")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun addMessage(msg: Message) {
        val eventBefore = message.lastOrNull()
        if((eventBefore != null && !eventBefore.sentAt.isSameDayAs(msg.sentAt)) || eventBefore == null) {
            message.add(
                DataHeader(msg.sentAt , this)
            )
        }
        message.add(msg)
        chatAdapter.notifyItemInserted(message.size - 1)
        msgRv.scrollToPosition(message.size - 1)
    }

    private fun sendMessage(msg : String) {
        val id = getMessages(friendId).push().key  // in order to generate a Unique Key
        checkNotNull(id) {" Cannot be null"}
        val msgMap =  Message(msg , mCurrentUid ,  id)
        getMessages(friendId).child(id).setValue(msgMap).addOnSuccessListener {
            Log.i("CHATS" , "completed")
        }.addOnFailureListener {
            Log.i("CHATS" , it.localizedMessage)
        }
        updateLastMessage(msgMap)

    }

    private fun updateLastMessage(message: Message) {
        val inboxMap = Inbox(
            message.msg ,
            friendId,
            name,
            image,
            count = 0
        )
        
        getInbox(mCurrentUid , friendId).setValue(inboxMap).addOnSuccessListener { 
            getInbox(friendId , mCurrentUid).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(Inbox::class.java)

                    inboxMap.apply {
                        from = message.senderId
                        name = currentUser.name
                        image = currentUser.thumbImage
                        count = 1
                    }
                    value?.let {
                        if(it.from == message.senderId) {
                            inboxMap.count = value.count + 1
                        }
                    }
                    getInbox(friendId , mCurrentUid).setValue(inboxMap)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }
    private fun markAsRead() {  // To re assign the count as zero when user read the message.
        getInbox(friendId , mCurrentUid).child("count").setValue(0)
    }

    private fun getMessages(friendId: String) = db.reference.child("messages/${getId(friendId)}")
    private fun getInbox(toUser: String , fromUser: String) = db.reference.child("chats/$toUser/$fromUser")


    private fun getId(friendId: String): String { // Id for the messages.
        return if (friendId > mCurrentUid) {
            mCurrentUid + friendId
        } else {
            friendId + mCurrentUid
        }
    }

    companion object {

        fun createChatActivity(context: Context, id: String, name: String, image: String): Intent {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(UID, id)
            intent.putExtra(NAME, name)
            intent.putExtra(IMAGE ,  image)

            return intent
        }
    }


}