package com.example.webexandroid.calling

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.webexandroid.R
import com.example.webexandroid.databinding.ActivityDialerBinding

class DialerActivity : AppCompatActivity(){
    lateinit var binding: ActivityDialerBinding

    companion object{
        const val IS_ADDING_CALL = "isAddingCall"
        fun getIntent(context: Context): Intent{
            return Intent(context, DialerActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityDialerBinding>(this, R.layout.activity_dialer).also {
            binding = it
        }.apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayShowHomeEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            setDialerFragment()
            handleNavigationClickListener()
        }

    }

    private fun handleNavigationClickListener() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setDialerFragment() {
        val dialFragment = DialFragment()
        val bundle = Bundle()
        bundle.putBoolean(IS_ADDING_CALL, true)
        dialFragment.arguments = bundle

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, dialFragment)
        transaction.commit()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}