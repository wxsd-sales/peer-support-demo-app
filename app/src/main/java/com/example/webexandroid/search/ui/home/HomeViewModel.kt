package com.example.webexandroid.search.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.webexandroid.BaseViewModel
import com.example.webexandroid.WebexRepository
import com.example.webexandroid.messaging.spaces.SpaceModel
import com.example.webexandroid.messaging.spaces.SpacesRepository
import com.ciscowebex.androidsdk.space.Space
import com.example.webexandroid.search.SearchRepository
import com.example.webexandroid.search.ui.home.HomeFragment
import com.google.firebase.database.*
import io.reactivex.android.schedulers.AndroidSchedulers

class HomeViewModel(private val searchRepo: SearchRepository, private val spacesRepo: SpacesRepository, private val webexRepo: WebexRepository) : BaseViewModel() {
    private val tag = "SearchViewModel"
    private val _spaces = MutableLiveData<List<FetchData>>()
    val spaces: LiveData<List<FetchData>> = _spaces

    private val _searchResult = MutableLiveData<List<Space>>()
    val searchResult: LiveData<List<Space>> = _searchResult

    private lateinit var dbref: DatabaseReference
    private lateinit var spaceArrayList: ArrayList<FetchData>

    private val _spaceEventLiveData = MutableLiveData<Pair<WebexRepository.SpaceEvent, Any?>>()

    val titles =
        listOf("Contacts","Search")

    init {
        webexRepo._spaceEventLiveData = _spaceEventLiveData
    }

    fun getSpaceEvent() = webexRepo._spaceEventLiveData

    fun isSpaceCallStarted() = webexRepo.isSpaceCallStarted
    fun spaceCallId() = webexRepo.spaceCallId

    fun loadData(taskType: String, maxSpaceCount: Int) {
        when (taskType) {
            HomeFragment.Companion.TaskType.TaskListSpaces -> {
                spaceArrayList= arrayListOf<FetchData>()
                getUserData()
//                spacesRepo.fetchSpacesList(null, maxSpaceCount).observeOn(AndroidSchedulers.mainThread()).subscribe({ spacesList ->
//                    _spaces.postValue(spacesList)
//                }, { _spaces.postValue(emptyList()) }).autoDispose()
            }
        }
    }
    private fun getUserData() {
        dbref= FirebaseDatabase.getInstance().getReference("Spaces")
        dbref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    for(userSnapshot in snapshot.children){
                        val user=userSnapshot.getValue(FetchData::class.java)
                        spaceArrayList.add(user!!)
                        //Log.e("new",spaceArrayList.toString())
                    }
                    _spaces.postValue(spaceArrayList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun search(query: String?) {
        query?.let { searchQuery ->
            searchRepo.search(searchQuery).observeOn(AndroidSchedulers.mainThread()).subscribe({
                _searchResult.postValue(it)
            }, {
                _searchResult.postValue(emptyList())
            }).autoDispose()
        }
    }
}

