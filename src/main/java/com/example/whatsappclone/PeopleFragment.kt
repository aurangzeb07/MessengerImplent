package com.example.whatsappclone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.DrawableCompat.inflate

import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_chat.*
import java.lang.Exception

private const val DELETED_VIEW_TYPE = 1
private const val NORMAL_VIEW_TYPE = 2

class PeopleFragment : Fragment() {

    lateinit var mAdapter: FirestorePagingAdapter<User, RecyclerView.ViewHolder>    // using 2 view in a single recycler view adapter.
    private lateinit var viewManager: RecyclerView.LayoutManager
    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    val database by lazy {
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("name", Query.Direction.ASCENDING)
    }
    override fun onCreateView  (
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewManager = LinearLayoutManager(requireContext())
        // Inflate the layout for this fragment.
        setupAdapter()
        return inflater.inflate(R.layout.fragment_chat , container , false)
    }

    private fun setupAdapter() {
        val config = PagedList.Config.Builder()
            .setPrefetchDistance(2)
            .setPageSize(10)
            .setEnablePlaceholders(false)
            .build()

        val options = FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(database , config , User::class.java)
            .build()
        
        mAdapter = object : FirestorePagingAdapter<User , RecyclerView.ViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val inflater = layoutInflater
                return when(viewType) {
                    NORMAL_VIEW_TYPE -> UserViewHolder(layoutInflater.inflate(R.layout.list_item , parent , false))
                    else -> EmptyViewHolder(layoutInflater.inflate(R.layout.empty_view , parent , false))
                }
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, user: User) {
                if (holder is UserViewHolder)
                 if (auth.uid == user.uid){
                     currentList?.snapshot()?.removeAt(position)
                     notifyItemRemoved(position)
//                    holder.bind(user = model) { name: String , photo: String , id: String ->
//                        val intent = Intent(requireContext() , ChatActivity::class.java)
//                        intent.putExtra(UID, id)
//                        intent.putExtra(NAME, name)
//                        intent.putExtra(IMAGE, photo)
//                        startActivity(intent)

                    }
                else holder.bind(user) {name: String , photo: String, id: String ->
                     startActivity(
                             ChatActivity.createChatActivity(
                                     requireContext(),
                                     id,
                                     name,
                                     photo
                             )
                     )

                }
            }

            override fun onLoadingStateChanged(state: LoadingState) {
               //super.onLoadingStateChanged(state)
                when(state) {
                    LoadingState.ERROR -> {
                        Toast.makeText(
                                requireContext(),
                                "Error Occurred!",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                    LoadingState.LOADING_INITIAL -> { }
                    LoadingState.LOADING_MORE -> { }
                    LoadingState.LOADED -> { }
                    LoadingState.FINISHED -> { }

                }
            }

            override fun onError(e: Exception) {
                super.onError(e)
                Log.e("MainActivity" , e.message.toString())
            }

            override fun getItemViewType(position: Int): Int {
                val item = getItem(position)?.toObject(User::class.java)
                return if(auth.uid == item!!.uid) {
                    DELETED_VIEW_TYPE
                } else {
                    NORMAL_VIEW_TYPE
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = mAdapter

        }
    }
}

/*
 * let we have 1000 users.
 * 1 users has a data of 10 Kb
 * Therefore 1000 users have to fetch online data of approx 10000 Kb which is to large ( Hence it shows us the Server timeout error).
 * We use Pagination to avoid this. (It is basically a concept of fetching the data from server in pages (whenever the data is required)).
 */
