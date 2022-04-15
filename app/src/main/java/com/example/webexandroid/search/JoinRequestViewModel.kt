package com.example.webexandroid.search
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.webexandroid.BaseViewModel
import com.example.webexandroid.WebexRepository
import com.example.webexandroid.messaging.spaces.SpaceModel
import com.example.webexandroid.messaging.spaces.SpacesRepository
import com.ciscowebex.androidsdk.space.Space
import com.example.webexandroid.person.PersonModel
import com.example.webexandroid.person.PersonRepository
import com.example.webexandroid.search.SearchRepository
import com.example.webexandroid.search.ui.home.FetchData
import com.example.webexandroid.search.ui.home.HomeFragment
import com.google.firebase.database.*
import io.reactivex.android.schedulers.AndroidSchedulers

class JoinRequestViewModel(private val searchRepo: SearchRepository, private val personRepo: PersonRepository, private val spacesRepo: SpacesRepository, private val webexRepo: WebexRepository) : BaseViewModel() {
    private val tag = "SearchViewModel"
    lateinit var spaceId: String
    private var person: String? = null
    private val _requests = MutableLiveData<List<JoinRequestData>>()
    val requests: LiveData<List<JoinRequestData>> = _requests

    private val _searchResult = MutableLiveData<List<Space>>()
    val searchResult: LiveData<List<Space>> = _searchResult

    private val _getMeData = MutableLiveData<PersonModel>()
    val getMeData: LiveData<PersonModel> = _getMeData

    private val _personName = MutableLiveData<String>()
    val personName: LiveData<String> = _personName

    private lateinit var dbref: DatabaseReference
    private lateinit var spaceArrayList: ArrayList<JoinRequestData>

    private val _spaceEventLiveData = MutableLiveData<Pair<WebexRepository.SpaceEvent, Any?>>()

    val titles =
        listOf("Contacts","Search")

    init {
        webexRepo._spaceEventLiveData = _spaceEventLiveData
    }

    fun getSpaceEvent() = webexRepo._spaceEventLiveData

    fun isSpaceCallStarted() = webexRepo.isSpaceCallStarted
    fun spaceCallId() = webexRepo.spaceCallId

    fun loadData() {
        spaceArrayList= arrayListOf<JoinRequestData>()
        getUserData()
    }
    private fun getUserData() {
        dbref= FirebaseDatabase.getInstance().getReference("Join Requests")
        //Log.e("person info",person.toString())
        personRepo.getMe().observeOn(AndroidSchedulers.mainThread()).subscribe {
            person = it.personId
            _personName.postValue(it.displayName)
            //Log.e("person info",person.toString())
            dbref.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists())
                    {
                        //Log.e("Snapshot","exists")
                        for(userSnapshot in snapshot.children){
                            var user=userSnapshot.getValue(JoinRequestData::class.java)
                            if(user?.spaceID==spaceId)
                            {
                                if(user?.recieverID==person)
                                {
                                    spaceArrayList.add(user!!)
                                    //Log.e("SpaceArrayList",spaceArrayList.toString())
                                }
                            }

                        }
                        _requests.postValue(spaceArrayList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
//            getMessages()
        }.autoDispose()

    }
}

