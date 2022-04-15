package com.example.webexandroid.search

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.message.LocalFile
import com.example.webexandroid.BaseActivity
import com.example.webexandroid.R
import com.example.webexandroid.WebexRepository
import com.example.webexandroid.messaging.composer.MessageComposerActivity
import com.example.webexandroid.messaging.spaces.ReplyMessageModel
import com.example.webexandroid.messaging.spaces.SpaceMessageModel
import com.example.webexandroid.utils.Constants
import com.example.webexandroid.utils.showDialogWithMessage
import com.ciscowebex.androidsdk.message.Message
import com.ciscowebex.androidsdk.message.RemoteFile
import com.ciscowebex.androidsdk.utils.EmailAddress
import com.example.webexandroid.calling.CallActivity
import com.example.webexandroid.databinding.*
import com.example.webexandroid.messaging.composer.MentionsPlugin
import com.example.webexandroid.messaging.composer.MessageComposerViewModel
import com.example.webexandroid.messaging.spaces.SpacesViewModel
import com.example.webexandroid.person.PersonViewModel
import com.example.webexandroid.search.ui.dashboard.DashboardFragment
import com.example.webexandroid.utils.extensions.hideKeyboard
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_space_detail.*
import org.koin.android.ext.android.inject

class JoinRequestActivity : BaseActivity() {

    companion object {
        fun getIntent(context: Context, spaceId: String): Intent {
            val intent = Intent(context, JoinRequestActivity::class.java)
            intent.putExtra(Constants.Intent.SPACE_ID, spaceId)
            return intent
        }

    }

    var joinRequestAdapter: JoinRequestAdapter = JoinRequestAdapter()
    lateinit var binding: ActivityJoinRequestBinding


    private val joinRequestViewModel: JoinRequestViewModel by inject()
    private val spacesViewModel: SpacesViewModel by inject()
    private val personViewModel: PersonViewModel by inject()
    private val itemModelList = mutableListOf<JoinRequestActivity.ItemModel>()
    private val messageComposerViewModel: MessageComposerViewModel by inject()
    private lateinit var spaceId: String
    private var messageId: String? = null
    private var id: String? = null
    private var replyParentMessage: ReplyMessageModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tag = "SpaceDetailActivity"

        spaceId = intent.getStringExtra(Constants.Intent.SPACE_ID) ?: ""

        joinRequestViewModel.spaceId = spaceId
        id = joinRequestViewModel.spaceId
        //spaceMessageRecyclerView.scrollToPosition(messageClientAdapter.itemCount-1)
        DataBindingUtil.setContentView<ActivityJoinRequestBinding>(
            this,
            R.layout.activity_join_request
        )
            .also { binding = it }
            .apply {

                recyclerView.itemAnimator = DefaultItemAnimator()
                recyclerView.adapter = joinRequestAdapter
                setUpObservers()

                swipeContainer.setOnRefreshListener {
                    joinRequestViewModel.loadData()
                    swipeContainer.isRefreshing = false
                }


            }
    }

    override fun onResume() {
        super.onResume()
        joinRequestViewModel.loadData()
    }


    private fun setUpObservers() {

        joinRequestViewModel.requests.observe(this, Observer { list ->
            list?.let {
//                if (taskType == TaskType.TaskCallHistory) it.sortedBy { it.created } else it.sortedByDescending { it.lastActivity }

                if (it.isEmpty()) {
                } else {
                    itemModelList.clear()
                    for (i in it.indices) {
                        val senderID = it[i].senderID
                        personViewModel.getPersonDetail(senderID)
                        val item = itemModelList.find { listItem -> listItem.senderId == senderID }
                        if (item == null) {
                            val itemModel = JoinRequestActivity.ItemModel()
                            itemModel.recieverID = it[i].recieverID
                            itemModel.senderId = senderID
                            itemModel.spaceID = it[i].spaceID

                            personViewModel.person.observe(this, Observer {
                                itemModel.personName = it.displayName
                            })
                            //add in array list
                            itemModelList.add(itemModel)
                        }
                    }
                    joinRequestAdapter.itemList = itemModelList
                    joinRequestAdapter.notifyDataSetChanged()
                }
            }
        })

    }

    class ItemModel {
        lateinit var recieverID: String
        lateinit var senderId: String
        lateinit var spaceID: String
        var personName: String = "Rajitha Kantheti"
        var ongoing = false
    }

    class JoinRequestAdapter() :

        RecyclerView.Adapter<JoinRequestAdapter.ViewHolder>() {
        var itemList: MutableList<ItemModel> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, i: Int): ViewHolder {
            return ViewHolder(
                CommonFragmentItemListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(itemList[position])
        }

        override fun getItemCount(): Int {
            return itemList.size
        }

        inner class ViewHolder(val binding: CommonFragmentItemListBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(itemModel: ItemModel) {
                binding.listItem = itemModel
                val joinRequestActivity = JoinRequestActivity()
                lateinit var dbref: DatabaseReference

                binding.accept.setOnClickListener {
                    joinRequestActivity.spacesViewModel.createMembershipWithId(
                        itemModel.spaceID,
                        itemModel.senderId,
                        false
                    )
                    binding.accept.visibility = View.GONE
                    binding.accepted.visibility = View.VISIBLE
                    var idName =
                        itemModel.senderId + "*" + itemModel.recieverID + "*" + itemModel.spaceID
                    dbref =
                        FirebaseDatabase.getInstance().getReference("Join Requests").child(idName)
                    dbref.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (userSnapshot in snapshot.children) {
                                    userSnapshot.ref.removeValue()

                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                }


                    if (itemModel.ongoing) {
                        binding.ongoing.visibility = View.VISIBLE
                    } else {
                        binding.ongoing.visibility = View.GONE
                    }
                    binding.executePendingBindings()
                }
            }
        }

    }





