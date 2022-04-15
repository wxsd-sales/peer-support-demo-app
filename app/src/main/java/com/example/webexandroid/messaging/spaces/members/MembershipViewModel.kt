package com.example.webexandroid.messaging.spaces.members

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.webexandroid.BaseViewModel
import com.example.webexandroid.WebexRepository
import com.ciscowebex.androidsdk.membership.Membership
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.*
import io.reactivex.android.schedulers.AndroidSchedulers
import java.nio.file.Files.size

class MembershipViewModel(private val membershipRepo: MembershipRepository, private val webexRepository: WebexRepository) : BaseViewModel() {
    private val _memberships = MutableLiveData<List<String>>()
    val memberships: LiveData<List<String>> = _memberships

    private val _membershipsSize = MutableLiveData<Int>()
    val membershipsSize: LiveData<Int> = _membershipsSize

    private val _membershipDetail = MutableLiveData<MembershipModel>()
    val membershipDetail: LiveData<MembershipModel> = _membershipDetail

    private val _deleteMembership = MutableLiveData<Pair<Boolean, Int>>()
    val deleteMembership: LiveData<Pair<Boolean, Int>> = _deleteMembership

    private val _membershipError = MutableLiveData<String>()
    val membershipError: LiveData<String> = _membershipError

    private val _membershipEventLiveData = MutableLiveData<Pair<WebexRepository.MembershipEvent, Membership?>>()
    val membershipEventLiveData: LiveData<Pair<WebexRepository.MembershipEvent, Membership?>> = _membershipEventLiveData

    private lateinit var spaceRef: DatabaseReference

    init {
        webexRepository._membershipEventLiveData = _membershipEventLiveData
    }

    override fun onCleared() {
        super.onCleared()
        webexRepository._membershipEventLiveData = null
    }

    fun getMembersIn(spaceId: String?, spaceName:String?,max: Int?):Int {
        var memberSize:Int=0
        //Log.e("Space ID",spaceId)

        membershipRepo.getMembersInSpace(spaceId, max).observeOn(AndroidSchedulers.mainThread()).subscribe({ memberships ->
            spaceRef= FirebaseDatabase.getInstance().getReference().child("Spaces")
            //Log.e("Space ID",spaceId)
            _memberships.postValue(listOf(memberships.toString()))
            val list_of_members_id=mutableListOf<String>()
            val list_of_members_email=mutableListOf<String>()

            //Log.e("memberhips",memberships.toString())
            _membershipsSize.postValue(memberships.size)
            memberSize=memberships.size
            val member= memberships
            //Log.e("MemberData",member.toString())
            val memberIterator=member.iterator()
            while (memberIterator.hasNext()) {
                val nextMember=memberIterator.next()
                val person=nextMember.personId
                val personEmail=nextMember.personEmail
                //Log.e("Member IDs", person)
                list_of_members_id.add(person)
                list_of_members_email.add(personEmail)
            }

            var profileMap : HashMap<String, String>
                    = HashMap<String, String> ()
            profileMap.put("peerSize", memberships.size.toString())
            profileMap.put("memberIDs",list_of_members_id.toString())
            profileMap.put("memberEmails",list_of_members_email.toString())

            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.exists()){
                        spaceRef.child(spaceName!!).updateChildren(profileMap as Map<String, Any>).addOnCompleteListener(
                            OnCompleteListener {
                                if(it.isSuccessful)
                                {
                                }
                            })
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            }
            spaceRef.orderByChild("sid").equalTo(spaceId).addValueEventListener(postListener)

        }, { _memberships.postValue(emptyList()) }).autoDispose()
        return memberSize
    }

    fun getMembership(membershipId: String) {
        membershipRepo.getMembership(membershipId).observeOn(AndroidSchedulers.mainThread()).subscribe({ membership ->
            _membershipDetail.postValue(membership)
        }, { error -> _membershipError.postValue(error.message) }).autoDispose()
    }

    fun updateMembershipWith(membershipId: String, isModerator : Boolean) {
        membershipRepo.updateMembershipWith(membershipId, isModerator).observeOn(AndroidSchedulers.mainThread()).subscribe({ membership ->
            _membershipDetail.postValue(membership)
        }, { error -> _membershipError.postValue(error.message) }).autoDispose()
    }

    fun deleteMembership(itemPosition: Int, membershipId: String) {
        membershipRepo.delete(membershipId).observeOn(AndroidSchedulers.mainThread()).subscribe({ response ->
            _deleteMembership.postValue(Pair(response, itemPosition))
        }, { error -> _membershipError.postValue(error.message) }).autoDispose()
    }

}