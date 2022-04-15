package com.example.webexandroid.search.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.Webex
import com.example.webexandroid.R
import com.example.webexandroid.WebexRepository
import com.example.webexandroid.databinding.*
import com.example.webexandroid.messaging.spaces.SpacesViewModel
import com.example.webexandroid.search.JoinRequestActivity
import com.example.webexandroid.messaging.spaces.detail.SpaceDetailActivity
import com.example.webexandroid.messaging.spaces.detail.SpaceDetailViewModel
import com.example.webexandroid.search.CreateSpaceActivity
import kotlinx.android.synthetic.main.fragment_common.*
import org.koin.android.ext.android.inject

class DashboardFragment : Fragment() {
    private val dashboardViewModel: DashboardViewModel by inject()
    private var adapter: CustomAdapter = CustomAdapter()
    private val itemModelList = mutableListOf<ItemModel>()
    lateinit var taskType: String
    private var binding2: FragmentDashboardBinding? = null
    private val spaceDetailViewModel: SpaceDetailViewModel by inject()
    private val messagingRepo: SpacesViewModel by inject()
    private val spacesViewModel: SpacesViewModel by inject()

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
        return FragmentDashboardBinding.inflate(inflater, container, false).apply {
            // lifecycleOwner = this@HomeFragment

            recyclerView.itemAnimator = DefaultItemAnimator()

            recyclerView.adapter = adapter

            taskType = TaskType.TaskListSpaces

            newSpace.setOnClickListener{
                val intent = Intent (getActivity(), CreateSpaceActivity::class.java)
                getActivity()?.startActivity(intent)
            }

//            remove_button.setOnClickListener{
//               // messagingRepo?.delete(name.toString())
//                Toast.makeText(getActivity(),name.toString(),Toast.LENGTH_LONG)
//            }


            setUpViewModelObservers()
            swipeContainer.setOnRefreshListener {
                dashboardViewModel.loadData(taskType, resources.getInteger(R.integer.space_list_size))
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
        dashboardViewModel.loadData(taskType, resources.getInteger(R.integer.space_list_size))
    }

    private fun setUpViewModelObservers() {
        // TODO: Put common code inside a function
        dashboardViewModel.spaces.observe(viewLifecycleOwner, Observer { list ->
            list?.let {
//                if (taskType == TaskType.TaskCallHistory) it.sortedBy { it.created } else it.sortedByDescending { it.lastActivity }

                if (it.isEmpty()) {
                    updateEmptyListUI(true)
                } else {
                    updateEmptyListUI(false)
                    itemModelList.clear()
                    for (i in it.indices) {
                        val id = it[i].id
                        val item = itemModelList.find { listItem -> listItem.callerId == id }
                        if (item == null) {
                            val itemModel = ItemModel()
                            itemModel.name = it[i].title
                            itemModel.callerId = id
                            itemModel.ownerID=it[i].toString()
                            itemModel.ongoing = dashboardViewModel.isSpaceCallStarted() && dashboardViewModel.spaceCallId() == id
                            //add in array list
                            spaceDetailViewModel.getMeData.observe(viewLifecycleOwner, Observer { model ->
                                model?.let {
                                    itemModel.userEmail = it.emails.toString()
                                    itemModel.userID=it.personId
                                }
                            })
                            spacesViewModel.getMeetingInfo(itemModel.callerId)
                            spacesViewModel.spaceMeetingInfo.observe(viewLifecycleOwner,Observer { model->
                                model?.let{
                                }

                            })
                            itemModelList.add(itemModel)
                        }
                    }
                    adapter.itemList = itemModelList
                    adapter.notifyDataSetChanged()
                }
            }
        })

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

        dashboardViewModel.getSpaceEvent()?.observe(viewLifecycleOwner, Observer {
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
        lateinit var userID:String
        lateinit var userEmail:String
        lateinit var ownerID: String
    }

    class CustomAdapter() :

        RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
        var itemList: MutableList<ItemModel> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, i: Int): ViewHolder {
            return ViewHolder(CommonGroupItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(itemList[position])
        }

        override fun getItemCount(): Int {
            return itemList.size
        }

        inner class ViewHolder(val binding: CommonGroupItemListBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(itemModel: ItemModel) {
                binding.listItem = itemModel
//                binding.phoneImage.setOnClickListener {
//                    it.context.startActivity(CallActivity.getOutgoingIntent(it.context, itemModel.callerId))
//                }
//
//                binding.messageImage.setOnClickListener {
//                    it.context.startActivity(SpaceDetailActivity.getIntent(it.context, itemModel.callerId,SpaceDetailActivity.Companion.ComposerType.POST_SPACE, null, null))
//                }
                binding.joinRequestButton.setOnClickListener{
                    it.context.startActivity(
                        JoinRequestActivity.getIntent(it.context, itemModel.callerId))
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