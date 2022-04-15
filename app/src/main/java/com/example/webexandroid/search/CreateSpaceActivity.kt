package com.example.webexandroid.search

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.Webex
import com.example.webexandroid.databinding.ActivityCreateSpaceBinding
import com.example.webexandroid.messaging.spaces.SpaceModel
import com.example.webexandroid.messaging.spaces.SpacesViewModel
import com.example.webexandroid.messaging.spaces.detail.SpaceDetailViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.Observable
import io.reactivex.Single
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.HashMap

class CreateSpaceActivity : AppCompatActivity() {
    var currentFragment : Fragment? =null
    private lateinit var binding: ActivityCreateSpaceBinding
    private lateinit var spaceRef: DatabaseReference
    val spacesViewModel: SpacesViewModel by inject()
    val spaceDetailViewModel: SpaceDetailViewModel by viewModel()
    var roomDesc:String?=null
    var topic:String?=null
    var duration:String?=null
    var gender:String?=null
    var age:String?=null
    var anonymous:Boolean?=null
    var sid:String?=null
    var email:String?=null
    var ownerID:String?=null
    var created:String="no"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        spaceRef= FirebaseDatabase.getInstance().getReference().child("Spaces");
        binding = ActivityCreateSpaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpViewModelObservers()
        binding.createButton.setOnClickListener{
            roomDesc=binding.roomDesc.text.toString()
            topic=binding.topic.selectedItem.toString()
            duration=binding.duration.selectedItem.toString()
            gender=binding.genderPreference.selectedItem.toString()
            age=binding.agePreference.selectedItem.toString()
            spacesViewModel.addSpace(topic!!,null)
            created="yes"
            binding.createButton.visibility= View.GONE
            binding.createdButton.visibility=View.VISIBLE
            binding.createdButton.isEnabled=false
            Toast.makeText(this@CreateSpaceActivity,"Session Created", Toast.LENGTH_LONG)
            setUpViewModelObservers()
//            var spaceMap : HashMap<String, String>
//                    = HashMap<String, String> ()
//            spaceMap.put("email",email.toString())
//            spaceMap.put("ownerID",ownerID.toString())
//            spaceMap.put("sid", sid.toString())
//            spaceMap.put("roomDesc", roomDesc.toString())
//            spaceMap.put("topic", topic.toString())
//            spaceMap.put("duration", duration.toString())
//            spaceMap.put("gender", gender.toString())
//            spaceMap.put("age", age.toString())
//
//
//            spaceRef.child(topic.toString()).updateChildren(spaceMap as Map<String, Any>).addOnCompleteListener(
//                OnCompleteListener {
//                    if(it.isSuccessful)
//                    {
//                        Toast.makeText(this@CreateSpaceActivity,"space info updated", Toast.LENGTH_LONG)
//                    }
//                })
//
//
//
//            startActivity(Intent (this, SearchActivity3::class.java))
        }
        binding.backButton.setOnClickListener{
            setUpViewModelObservers()
            if(created==="yes") {
                var spaceMap: HashMap<String, String> = HashMap<String, String>()
                spaceMap.put("email", email.toString())
                spaceMap.put("ownerID", ownerID.toString())
                spaceMap.put("sid", sid.toString())
                spaceMap.put("roomDesc", roomDesc.toString())
                spaceMap.put("topic", topic.toString())
                spaceMap.put("duration", duration.toString())
                spaceMap.put("gender", gender.toString())
                spaceMap.put("age", age.toString())


                spaceRef.child(topic.toString()).updateChildren(spaceMap as Map<String, Any>)
                    .addOnCompleteListener(
                        OnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(
                                    this@CreateSpaceActivity,
                                    "space info updated",
                                    Toast.LENGTH_LONG
                                )
                            }
                        })
            }


            startActivity(Intent (this, SearchActivity3::class.java))
        }
    }

    private fun setUpViewModelObservers() {
        spacesViewModel.getID()?.observe(this@CreateSpaceActivity, Observer{ id ->
            id?.let{
                //Log.v("CreatedIDFrom",id.toString())
                sid=it
                //Log.e("sid",sid)
            }
        })
        spaceDetailViewModel.getMeData.observe(this@CreateSpaceActivity, Observer { model ->
            model?.let {
                email = it.emails.toString()
                ownerID=it.personId
            }
        })
}

//    fun startTimeCounter(view: View) {
//        val countTime=10
//        object : CountDownTimer(50000, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                countTime.text = counter.toString()
//                counter++
//            }
//            override fun onFinish() {
//                countTime.text = "Finished"
//            }
//        }.start()
//    }
    override fun onResume() {
        super.onResume()
    }
}