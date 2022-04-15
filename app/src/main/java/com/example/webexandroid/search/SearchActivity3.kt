package com.example.webexandroid.search

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.webexandroid.R
import com.example.webexandroid.WebexViewModel
import com.example.webexandroid.databinding.ActivitySearch3Binding
import com.example.webexandroid.search.ui.dashboard.DashboardFragment
import com.example.webexandroid.search.ui.home.HomeFragment
import com.example.webexandroid.search.ui.notifications.NotificationsFragment
import com.example.webexandroid.utils.Constants
import com.example.webexandroid.utils.SharedPrefUtils
import kotlinx.android.synthetic.main.activity_dialer.*
import org.koin.android.viewmodel.ext.android.viewModel
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.webexandroid.WebexAndroidApp
import com.example.webexandroid.auth.LoginActivity

import androidx.navigation.fragment.NavHostFragment
import com.example.webexandroid.databinding.FragmentNotificationsBinding
import com.example.webexandroid.person.PersonViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_search3.*


class SearchActivity3 : AppCompatActivity() {

    private lateinit var binding: ActivitySearch3Binding
    val webexViewModel: WebexViewModel by viewModel()
    var currentFragment : Fragment? =null
    private var binding2: FragmentNotificationsBinding? = null
    private val personViewModel: PersonViewModel by viewModel()
    var userEmail:String?=null
    var userID:String?=null
    private lateinit var userRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try{
            binding = ActivitySearch3Binding.inflate(layoutInflater)
            setContentView(binding.root)
            userRef= FirebaseDatabase.getInstance().getReference().child("Users");

            webexViewModel.signOutListenerLiveData.observe(this@SearchActivity3, Observer {
                it?.let {
                    if (it) {
                        SharedPrefUtils.clearLoginTypePref(this)
                        (application as WebexAndroidApp).unloadKoinModules()
                        finish()
                    }
//                    else {
//                        progressLayout.visibility = View.GONE
//                    }
                }
            })
            personViewModel.getMe()
            personViewModel.person.observe(this@SearchActivity3, Observer { model ->
                model?.let {
                    userEmail = it.emails.toString()
                    userID=it.personId
                    ////Log.e("SearchActivityuserID",userID)
                    var email= userEmail!!.replace('.','*')
                    email= email.substring( 1, email.length - 1 )
                    var profileMap : HashMap<String, String>
                            = HashMap<String, String> ()
                    profileMap.put("uid", userID!!)
                    val postListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if(dataSnapshot.exists()){

                            }
                            else{
                                userRef.child(email).updateChildren(profileMap as Map<String, Any>).addOnCompleteListener(
                                    OnCompleteListener {
                                        if(it.isSuccessful)
                                        {
                                            Toast.makeText(this@SearchActivity3,"info updated", Toast.LENGTH_LONG)
                                        }
                                    })
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    }
                    userRef.orderByChild("email").equalTo(userEmail.toString()).addValueEventListener(postListener)

                }
            })

            val navView: BottomNavigationView = binding.navView

//            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//            val navController = navHostFragment.navController
            val navController = Navigation.findNavController(this, R.id.fragment_nav)
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_contacts,
                    R.id.navigation_support_groups,
                    R.id.navigation_aboutme,
                    R.id.navigation_logout
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
            nav_view.setOnItemSelectedListener { item ->
                var fragment: Fragment
                when (item.itemId) {
                    R.id.navigation_contacts -> {


                        toolbar?.setTitle("Webex Sample App")
                        fragment = DashboardFragment()

                        replaceFragment(fragment)
                        true
                    }
                    R.id.navigation_support_groups -> {
                        //binding2?.sampleText?.visibility = View.GONE

                        toolbar?.setTitle("Webex Sample App")
                        fragment = HomeFragment()
                        replaceFragment(fragment)
                        true
                    }
                    R.id.navigation_aboutme -> {
                        toolbar?.setTitle("Webex Sample App")
                        fragment = NotificationsFragment()
                        replaceFragment(fragment)
                        true
                    }
                    R.id.navigation_logout -> {
                        //progressLayout.visibility = View.VISIBLE
                        webexViewModel.signOut()
                        //startActivity(Intent(this@SearchActivity3, LoginActivity::class.java))
                        true
                    }
                    else -> false
                }

            }
        }
        catch (e: Exception) {
            //Log.e(TAG, "onCreateView", e);
            throw e;
        }
    }

    private fun loadFragment(fragment: Fragment) {
        // load fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_nav, fragment)
            .commit()
    }
    private fun replaceFragment(fragment: Fragment)
    {
        if(!fragment.equals(currentFragment))
        {
            val transaction=supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_nav, fragment)
            transaction.commit()
            currentFragment= fragment
        }
    }
}