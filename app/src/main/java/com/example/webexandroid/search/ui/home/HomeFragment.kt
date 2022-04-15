package com.example.webexandroid.search.ui.home

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.example.webexandroid.WebexRepository
import com.example.webexandroid.databinding.*
import com.example.webexandroid.messaging.spaces.SpacesViewModel
import com.example.webexandroid.messaging.spaces.detail.MessageActionBottomSheetFragment
import com.example.webexandroid.messaging.spaces.detail.SpaceDetailActivity
import com.example.webexandroid.messaging.spaces.detail.SpaceDetailViewModel
import com.example.webexandroid.messaging.spaces.members.MembershipViewModel
import com.example.webexandroid.person.PersonDialogFragment
import com.example.webexandroid.search.CreateSpaceActivity
import com.example.webexandroid.search.ui.home.HomeViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.*
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.all_spaces_list.*
import kotlinx.android.synthetic.main.common_group_item_list.*
import kotlinx.android.synthetic.main.fragment_common.*
import org.koin.android.ext.android.inject
import org.koin.java.KoinJavaComponent.inject
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.webexandroid.search.SearchActivity3
import kotlinx.coroutines.joinAll
import android.R
import androidx.test.core.app.ApplicationProvider

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import java.time.Duration


class HomeFragment : Fragment() {
    private val homeViewModel: HomeViewModel by inject()
    private var adapter: CustomAdapter = CustomAdapter()
    private val itemModelList = mutableListOf<ItemModel>()
    lateinit var taskType: String
    private var binding2: FragmentHomeBinding? = null
    private val spaceDetailViewModel: SpaceDetailViewModel by inject()
    private val spacesViewModel: SpacesViewModel by inject()
    private val messagingRepo: SpacesViewModel by inject()
    private val membershipViewModel: MembershipViewModel by inject()
    var email: String?=null
    var id:String?=null
    private lateinit var spaceRef: DatabaseReference

    companion object {
        object TaskType {
            const val TaskSearchSpace = "SearchSpace"
            const val TaskCallHistory = "CallHistory"
            const val TaskListSpaces = "ListSpaces"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //binding2?.sampleText?.visibility = View.GONE
        return FragmentHomeBinding.inflate(inflater, container, false).apply {
            // lifecycleOwner = this@HomeFragment

            recyclerView.itemAnimator = DefaultItemAnimator()

            recyclerView.adapter = adapter

            taskType = TaskType.TaskListSpaces


//            remove_button.setOnClickListener{
//               // messagingRepo?.delete(name.toString())
//                Toast.makeText(getActivity(),name.toString(),Toast.LENGTH_LONG)
//            }
//            val itemModel = ItemModel()
//            Join_button.setOnClickListener{
//                startActivity(SpaceDetailActivity.getIntent(it.context, itemModel.callerId,SpaceDetailActivity.Companion.ComposerType.POST_SPACE, null, null))
//            }


            setUpViewModelObservers()
            swipeContainer.setOnRefreshListener {
                homeViewModel.loadData(taskType, 100)
                // spaceMessageRecyclerView.smoothScrollToPosition(spaceMessageRecyclerView.getAdapter()?.itemCount!!)
            }

        }.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //updateSearchInputViewVisibility()
        //progress_bar.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.loadData(taskType, 100)
    }

    private fun setUpViewModelObservers() {
        // TODO: Put common code inside a function
        spaceRef= FirebaseDatabase.getInstance().getReference().child("Spaces");
        homeViewModel.spaces.observe(viewLifecycleOwner, Observer { list ->
            list?.let {
//                if (taskType == TaskType.TaskCallHistory) it.sortedBy { it.created } else it.sortedByDescending { it.lastActivity }

                if (it.isEmpty()) {
                    updateEmptyListUI(true)
                } else {
                    updateEmptyListUI(false)
                    itemModelList.clear()
                    for (i in it.indices) {
                        val id = it[i].sid
                        val item = itemModelList.find { listItem -> listItem.callerId == id }
                        if (item == null) {
                            val itemModel = ItemModel()
                            itemModel.name = it[i].topic
                            itemModel.callerId = id
                            itemModel.ongoing = homeViewModel.isSpaceCallStarted() && homeViewModel.spaceCallId() == id
                            itemModel.email= it[i].email
                            itemModel.ownerID=it[i].ownerID
                            spaceDetailViewModel.getMeData.observe(viewLifecycleOwner, Observer { model ->
                                model?.let {
                                    itemModel.userEmail = it.emails.toString()
                                    itemModel.userID=it.personId
                                }
                            })
                            membershipViewModel.getMembersIn(itemModel.callerId,itemModel.name,500)
//                            membershipViewModel.membershipsSize.observe(viewLifecycleOwner,Observer{
//                                //val itemModel = ItemModel()
//                                itemModel.peerSize=it.toString()
//                                Log.e("peerSize",it.toString())
//                                var profileMap : HashMap<String, String>
//                                        = HashMap<String, String> ()
//                                profileMap.put("peerSize",it.toString())
//
//                                val postListener = object : ValueEventListener {
//                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//                                        if(dataSnapshot.exists()){
//                                            spaceRef.child(itemModel.name).updateChildren(profileMap as Map<String, Any>).addOnCompleteListener(
//                                                OnCompleteListener {
//                                                    if(it.isSuccessful)
//                                                    {
//                                                    }
//                                                })
//                                        }
//                                    }
//
//                                    override fun onCancelled(databaseError: DatabaseError) {
//                                    }
//                                }
//                                spaceRef.orderByChild("sid").equalTo(itemModel.callerId).addValueEventListener(postListener)
//                            })
                            //add in array list
                            itemModelList.add(itemModel)
                        }
                    }
                    adapter.itemList = itemModelList
                    adapter.notifyDataSetChanged()
                }
            }
        })

//        membershipViewModel.membershipsSize.observe(viewLifecycleOwner,Observer{
//            val itemModel = ItemModel()
//            itemModel.peerSize=it.toString()
//        })




//        spaceDetailViewModel.getMeData.observe(viewLifecycleOwner, Observer { model ->
//            model?.let {
//                val itemModel= ItemModel()
//                itemModel.userEmail = it.emails.toString()
//                itemModel.userID=it.personId
//            }
//        })

//        searchViewModel.searchResult.observe(viewLifecycleOwner, Observer { list ->
//            list?.let {
//                if (it.isEmpty()) {
//                    updateEmptyListUI(true)
//                } else {
//                    updateEmptyListUI(false)
//                    itemModelList.clear()
//                    for (i in it.indices) {
//                        val itemModel = ItemModel()
//                        val space = it[i]
//                        itemModel.name = space.title.orEmpty()
//                        itemModel.phoneImage = R.drawable.ic_call
//
//                        itemModel.callerId = space.id.orEmpty()
//                        itemModelList.add(itemModel)
//                    }
//                    adapter.itemList = itemModelList
//                    adapter.notifyDataSetChanged()
//                }
//            }
//        })

        homeViewModel.getSpaceEvent()?.observe(viewLifecycleOwner, Observer {
            when (it.first) {
                WebexRepository.SpaceEvent.CallStarted -> {
                    if (it.second is String?) {
                        val spaceId = it.second as String?
                        spaceId?.let { id ->
                            updateSpaceCallStatus(id, true)
                        }
                    }
                }
                WebexRepository.SpaceEvent.CallEnded -> {
                    if (it.second is String?) {
                        val spaceId = it.second as String?
                        spaceId?.let { id ->
                            updateSpaceCallStatus(id, false)
                        }
                    }
                }
                else -> {}
            }
        })
    }

    private fun updateSpaceCallStatus(spaceId: String, callStarted: Boolean) {
        val index = adapter.getPositionById(spaceId)
        if (index != -1) {
            val model = adapter.itemList[index]
            model.ongoing = callStarted
            adapter.notifyItemChanged(index)
        }
    }

    private fun updateEmptyListUI(listEmpty: Boolean) {
        // progress_bar.visibility = View.GONE
        binding2?.swipeContainer?.isRefreshing ?:  false
        if (listEmpty) {
            tv_empty_data.visibility = View.VISIBLE
            recycler_view.visibility = View.GONE
        } else {
            tv_empty_data.visibility = View.GONE
            recycler_view.visibility = View.VISIBLE
        }
    }

//    private fun updateSearchInputViewVisibility() {
//        when (taskType) {
//            TaskType.TaskSearchSpace -> {
////                search_view.visibility = View.VISIBLE
//            }
//            else -> {
////                search_view.visibility = View.GONE
//            }
//        }
//    }


    class ItemModel {
        lateinit var name: String
        lateinit var callerId: String
        var ongoing = false
        lateinit var email: String
        lateinit var userID: String
        lateinit var userEmail: String
        var peerSize: String="hi"
        lateinit var ownerID: String

    }

    class CustomAdapter :
        RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
        var itemList: MutableList<ItemModel> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, i: Int): ViewHolder {
            return ViewHolder(AllSpacesListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(itemList[position])
        }

        override fun getItemCount(): Int {
            return itemList.size
        }

        inner class ViewHolder(val binding: AllSpacesListBinding) :

            RecyclerView.ViewHolder(binding.root) {

            fun bind(itemModel: ItemModel) {
                lateinit var dbref: DatabaseReference
                lateinit var joinRequestRef: DatabaseReference
                lateinit var reqref: DatabaseReference
                lateinit var binding2: FragmentHomeBinding
                var spaceArrayList: ArrayList<FetchData>? = null
                val homeFragment = HomeFragment()
                var memberlist: String= ""
                var memberemaillist: String= ""
                var memberList= mutableListOf<String>()
                var memberEmailList= mutableListOf<String>()
                var duration: String=""
                var age:String=""
                var description:String=""
                var gender:String=""
                var currentState:String = "new"
                var guestUser=false
                homeFragment.membershipViewModel.getMembersIn(itemModel.callerId,itemModel.name,500)
                binding.listItem = itemModel
//                binding.phoneImage.setOnClickListener {
//                    it.context.startActivity(CallActivity.getOutgoingIntent(it.context, itemModel.callerId))
//                }
//
                binding.activeButton.visibility=View.VISIBLE
                binding.joinButton.visibility=View.VISIBLE

                if(itemModel.callerId=="e0349c60-61b2-11ec-aae0-ab6121ffd588")
                {
                    binding.textView14.visibility=View.GONE
                    binding.peerNumber.visibility=View.GONE
                    binding.joinButton.visibility=View.GONE
                    binding.activeButton.visibility=View.GONE
                    binding.closedButton.visibility=View.VISIBLE
                }

                dbref =
                    FirebaseDatabase.getInstance().getReference("Spaces").child(itemModel.name)
                dbref.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val user = snapshot.getValue(FetchData::class.java)
                            //Log.e("PeersizeValue",user?.peerSize)
                            //Log.e("memberListValue",user?.memberIDs.toString())
                            binding.peerNumber.setText(user?.peerSize)
                            duration=user?.duration.toString()
                            age=user?.age.toString()
                            gender=user?.gender.toString()
                            description=user?.roomDesc.toString()
                            memberlist = user?.memberIDs.toString()
                            memberemaillist= user?.memberEmails.toString()
                            //Log.e("memberListLength",memberlist.length.toString())
                            if(memberlist.length!=0) {
                                memberlist = memberlist.substring(1, memberlist.length - 1)
                                memberList = mutableListOf(*memberlist.split(",").toTypedArray())
                                //Log.e("DatabaseMemberId", memberList.toString())
                            }
                            if(memberemaillist.length!=0)
                            {
                                memberemaillist = memberemaillist.substring(1, memberemaillist.length - 1)
                                memberEmailList = mutableListOf(*memberemaillist.split(",").toTypedArray())
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })


//                homeFragment.spaceDetailViewModel.getMe()
//
//                var jid2=itemModel.userID+"."+itemModel.ownerID+"."+itemModel.callerId
//                dbref= FirebaseDatabase.getInstance().getReference("Join Requests").child(jid2)
//                dbref.addValueEventListener(object: ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        if(snapshot.exists())
//                        {
//                            binding.joinButton.visibility=View.GONE
//                            binding.reqsentButton.visibility=View.VISIBLE
//                        }
//                        else
//                        {
//                            binding.joinButton.visibility=View.VISIBLE
//                            binding.reqsentButton.visibility=View.GONE
//                        }
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        TODO("Not yet implemented")
//                    }
//                })

                binding.joinButton.setOnClickListener {
                    //Log.e("UserID",itemModel.userID)

                    //Log.e("MembershipList",memberList.toString())
                    var contains:String="doesnot"
                    val itr = memberList.listIterator()    // or, use `iterator()`
                    while (itr.hasNext()) {
                        var uid=itr.next()
                        if(uid.first()==' ')
                        {
                            //Log.e("uidIterator",uid)
                            //Log.e("uiditemMOdel",itemModel.userID)
                            uid = uid.substring(1, uid.length)
                        }
                        if(uid==itemModel.userID)
                        {
                            contains="contains"
                            break
                        }
                    }
                    //Log.e("COntains",contains)

                    if(contains=="contains")
                    {
                        //Log.e("CointainsOrNot","contains")
                        //Log.e("User Email",itemModel.userEmail)
                        if(itemModel.userEmail.contains("appid.ciscospark.com"))
                        {
                            guestUser=true
                        }
                        it.context.startActivity(SpaceDetailActivity.getIntent(it.context, itemModel.callerId,SpaceDetailActivity.Companion.ComposerType.POST_SPACE, null, null,guestUser))
                        binding.reqsentButton.visibility=View.GONE
                    }
                    else{
                        //Log.e("CointainsOrNot","Doesnot contain")
                        binding.joinButton.visibility=View.GONE
                        binding.reqsentButton.visibility=View.VISIBLE
                        //sendJoinRequest();
                        joinRequestRef=FirebaseDatabase.getInstance().getReference().child("Join Requests")
                        var jid=itemModel.userID+"*"+itemModel.ownerID+"*"+itemModel.callerId
                        var joinMap : HashMap<String, String>
                                = HashMap<String, String> ()
                        joinMap.put("senderID",itemModel.userID)
                        joinMap.put("recieverID",itemModel.ownerID)
                        joinMap.put("spaceID",itemModel.callerId)
                        joinRequestRef.child(jid).updateChildren(joinMap as Map<String, Any>).addOnCompleteListener(
                            OnCompleteListener {
                                if(it.isSuccessful)
                                {
                                }
                            })

                    }

                   // homeFragment.spacesViewModel.getMeetingInfo(itemModel.callerId)

                }

                binding.infoIcon.setOnClickListener{
                    val description="Description"+" : "+description
                    val owner="Owner : "+itemModel.email
                    val roomID="Session ID : "+itemModel.callerId
                    val members="Members"+" : "+memberemaillist
                    val duration="Duration : "+duration
                    val age="Age : "+age
                    val gender="Gender : "+gender

                    val items = arrayOf(description,owner,roomID,members,duration,age,gender)
                    val builder = AlertDialog.Builder(it.context)
                    builder.setTitle(itemModel.name)
                        .setItems(
                            items
                        ) { dialog, which ->
                            Toast.makeText(
                                getApplicationContext(),
                                items[which] + " is clicked",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    builder.setNegativeButton("CANCEL", null)

                    val alertDialog = builder.create()

                    alertDialog.show()
                }

                if (itemModel.ongoing) {
                    binding.ongoing.visibility = View.VISIBLE
                } else {
                    binding.ongoing.visibility = View.GONE
                }
                binding.executePendingBindings()
            }
        }

        fun getPositionById(spaceId: String): Int {
            return itemList.indexOfFirst { it.callerId == spaceId }
        }
    }
}