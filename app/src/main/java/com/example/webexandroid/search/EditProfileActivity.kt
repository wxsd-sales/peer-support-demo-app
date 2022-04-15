package com.example.webexandroid.search

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.lifecycle.Observer
import com.example.webexandroid.R
import com.example.webexandroid.databinding.ActivityCreateSpaceBinding
import com.example.webexandroid.databinding.ActivityEditProfileBinding
import com.example.webexandroid.databinding.FragmentNotificationsBinding
import com.example.webexandroid.messaging.spaces.detail.SpaceDetailViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.koin.android.viewmodel.ext.android.viewModel

class EditProfileActivity : AppCompatActivity() {
    var gender:String?=null
    var age:String?=null
    var anonymous:String?=null
    var email:String?=null
    var id:String?=null
    var anonymousName:String?=null
    private lateinit var profileRef: DatabaseReference
    val spaceDetailViewModel: SpaceDetailViewModel by viewModel()
    private lateinit var binding: ActivityEditProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpViewModelObservers()
        //Log.e("message of selection",binding.anonymousPreference.selectedItem.toString())
        //Log.e("email",email.toString())

        binding.saveChanges.setOnClickListener{
            profileRef= FirebaseDatabase.getInstance().getReference().child("Users");
            gender=binding.genderPreference.selectedItem.toString()
            age=binding.agePreference.selectedItem.toString()
            var profileMap : HashMap<String, String>
                    = HashMap<String, String> ()
            profileMap.put("gender", gender.toString())
            profileMap.put("age", age.toString())
            profileMap.put("anonymous",anonymous.toString())
            profileMap.put("uid",id.toString())
            //Log.e("email",email.toString())
            var email1=email.toString().replace(".","*")
            email1=email1.substring( 1, email1.length - 1 )
            //profileRef.child(email1).removeValue()
            profileRef.child(email1).updateChildren(profileMap as Map<String, Any>).addOnCompleteListener(
                OnCompleteListener {
                    if(it.isSuccessful)
                    {
                        Toast.makeText(this@EditProfileActivity,"info updated", Toast.LENGTH_LONG)
                    }
                })

            startActivity(Intent (this, SearchActivity3::class.java))
        }
    }

    private fun setUpViewModelObservers() {
        spaceDetailViewModel.getMeData.observe(this@EditProfileActivity, Observer { model ->
            model?.let {
                email = it.emails.toString()
                id=it.personId
            }
        })
    }
}