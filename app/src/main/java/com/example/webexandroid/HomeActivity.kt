package com.example.webexandroid

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.webexandroid.auth.LoginActivity
import com.example.webexandroid.databinding.ActivityHomeBinding
import com.example.webexandroid.utils.Constants
import com.example.webexandroid.calling.CallActivity
import com.example.webexandroid.search.SearchActivity
import com.example.webexandroid.utils.SharedPrefUtils.clearLoginTypePref
import com.example.webexandroid.utils.SharedPrefUtils.saveLoginTypePref

class HomeActivity : BaseActivity() {

    lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tag = "HomeActivity"

        val authenticator = webexViewModel.webex.authenticator

        webexViewModel.enableBackgroundConnection(webexViewModel.enableBgConnectiontoggle)
        webexViewModel.setLogLevel(webexViewModel.logFilter)
        webexViewModel.enableConsoleLogger(webexViewModel.isConsoleLoggerEnabled)

/*        //Log.d(tag, "Service URls METRICS: ${webexViewModel.getServiceUrl(Phone.ServiceUrlType.METRICS)}" +
                "\nCLIENT_LOGS: ${webexViewModel.getServiceUrl(Phone.ServiceUrlType.CLIENT_LOGS)}" +
                "\nKMS: ${webexViewModel.getServiceUrl(Phone.ServiceUrlType.KMS)}")*/

        authenticator?.let {
            saveLoginTypePref(this)
        }

        webexViewModel.signOutListenerLiveData.observe(this@HomeActivity, Observer {
            it?.let {
                if (it) {
                    clearLoginTypePref(this)
                    finish()
                }
                else {
                    binding.progressLayout.visibility = View.GONE
                }
            }
        })

        DataBindingUtil.setContentView<ActivityHomeBinding>(this, R.layout.activity_home)
                .also { binding = it }
                .apply {

                    ivStartCall.setOnClickListener {
                        startActivity(Intent(this@HomeActivity, SearchActivity::class.java))
                    }

                    ivWaitingCall.setOnClickListener {
                        startActivity(CallActivity.getIncomingIntent(this@HomeActivity))
                    }

                    ivLogout.setOnClickListener {
                        progressLayout.visibility = View.VISIBLE
                        webexViewModel.signOut()
                    }
                }

        //used some delay because sometimes it gives empty stuff in personDetails
        Handler().postDelayed(Runnable {

        }, 1000)

        webexViewModel.setSpaceObserver()
        webexViewModel.setMembershipObserver()
        webexViewModel.setMessageObserver()
    }

    override fun onBackPressed() {
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    override fun onResume() {
        super.onResume()
        webexViewModel.setGlobalIncomingListener()
    }
}