package com.example.webexandroid.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.webexandroid.HomeActivity
import com.example.webexandroid.R
import com.example.webexandroid.WebexAndroidApp
import com.example.webexandroid.databinding.ActivityOauthBinding
import com.example.webexandroid.messaging.spaces.detail.SpaceDetailViewModel
import com.example.webexandroid.person.PersonViewModel
import com.example.webexandroid.search.SearchActivity
import com.example.webexandroid.search.SearchActivity3
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class OAuthWebLoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityOauthBinding
    private val loginViewModel: LoginViewModel by viewModel()
    private val personViewModel: PersonViewModel by viewModel()
    var userEmail:String?=null
    var userID:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityOauthBinding>(this, R.layout.activity_oauth)
                .also { binding = it }
                .apply {
                    progressLayout.visibility = View.VISIBLE
                    setupViewModelObservers()
                    loginViewModel.isAuthorized.observe(this@OAuthWebLoginActivity, Observer { isAuthorized ->
                        progressLayout.visibility = View.GONE
                        isAuthorized?.let {
                            if (it) {
                                onLoggedIn()
                            } else {
                                onLoginFailed()
                            }
                        }
                    })

                    loginViewModel.isAuthorizedCached.observe(this@OAuthWebLoginActivity, Observer { isAuthorizedCached ->
                        progressLayout.visibility = View.GONE
                        isAuthorizedCached?.let {
                            if (it) {
                                onLoggedIn()
                            } else {
                                appBarLayout.visibility = View.GONE
                                binding.exitButton.visibility = View.GONE
                                loginFailedTextView.visibility = View.GONE
                                loginWebview.visibility = View.VISIBLE
                                loginViewModel.authorizeOAuth(loginWebview)
                            }
                        }
                    })

                    loginViewModel.errorData.observe(this@OAuthWebLoginActivity, Observer { errorMessage ->
                        onLoginFailed(errorMessage)
                    })

                    exitButton.setOnClickListener {
                        // close application as user needs to reload koin modules, currently unloading and reloading of koin modules doesn't work
                        (application as WebexAndroidApp).closeApplication()
                    }

                    loginViewModel.initialize()
                }
    }

    override fun onBackPressed() {
        (application as WebexAndroidApp).closeApplication()
    }

    private fun onLoggedIn() {
        ////Log.e("Log In", "Entered Login method")
        setupViewModelObservers()
        personViewModel.getMe()
        ////Log.e("user email",userEmail.toString())
        ////Log.e("user ID",userID.toString())
        startActivity(Intent(this, SearchActivity3::class.java))
        finish()
    }
    private fun setupViewModelObservers()
    {
        personViewModel.person.observe(this@OAuthWebLoginActivity, Observer { model ->
            model?.let {
                userEmail = it.emails.toString()
                userID=it.personId


            }
        })
    }

    private fun onLoginFailed(failureMessage: String = getString(R.string.login_failed)) {
        ////Log.d("auth : ", "onLoginFailed, updating ui")
        binding.loginWebview.visibility = View.GONE
        binding.appBarLayout.visibility = View.VISIBLE
        binding.exitButton.visibility = View.VISIBLE
        binding.loginFailedTextView.visibility = View.VISIBLE
        binding.loginFailedTextView.text = failureMessage
    }
}