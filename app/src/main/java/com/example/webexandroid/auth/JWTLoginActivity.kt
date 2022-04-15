package com.example.webexandroid.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.webexandroid.R
import com.example.webexandroid.WebexAndroidApp
import com.example.webexandroid.databinding.ActivityLoginWithTokenBinding
import com.example.webexandroid.search.SearchActivity3
import com.example.webexandroid.utils.showDialogWithMessage
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class JWTLoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginWithTokenBinding
    private val loginViewModel: LoginViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityLoginWithTokenBinding>(this, R.layout.activity_login_with_token)
                .also { binding = it }
                .apply {
//                    title.text = getString(R.string.login_jwt)
//                    progressLayout.visibility = View.VISIBLE
//                    loginButton.setOnClickListener {
//                        binding.loginFailedTextView.visibility = View.GONE
//                        if (jwtTokenText.text.isEmpty()) {
//                            showDialogWithMessage(this@JWTLoginActivity, R.string.error_occurred, resources.getString(R.string.jwt_login_token_empty_error))
//                        }
//                        else {
                    progressLayout.visibility = View.VISIBLE
                    val key="H7NM8w55JfZbQnq3O/qijjOcCIe8YZi6nv7dKGiKlIQ="
                    val seckey = Keys.hmacShaKeyFor((Decoders.BASE64.decode(key)))
                    //Log.e("Secret encoded",seckey.toString())
                    val now = Date()
                    val jwt = Jwts.builder()
                        .setHeaderParam("typ", "JWT")
                        .setHeaderParam("alg","HS256")
                        .claim("sub", "guest90")
                        .claim("name","Alpian")
                        .claim("iss","Y2lzY29zcGFyazovL3VzL09SR0FOSVpBVElPTi84YTRhYTFmOC1kZWZiLTRmMWYtOTljOC00YTg2YTRiOWIzMWE")
                        .setExpiration(Date(now.time + 2 * 1000 * 60 * 60))
                        .signWith(seckey)
                        .compact()
                    //Log.e("JWTToken",jwt)
                            ///binding.loginButton.visibility = View.GONE
                            //progressLayout.visibility = View.VISIBLE
                            val token = jwt
                            loginViewModel.loginWithJWT(token)
//                        }
//                    }

                    loginViewModel.isAuthorized.observe(this@JWTLoginActivity, Observer { isAuthorized ->
                        progressLayout.visibility = View.GONE
                        isAuthorized?.let {
                            if (it) {
                                onLoggedIn()
                            } else {
                                onLoginFailed()
                            }
                        }
                    })

                    loginViewModel.isAuthorizedCached.observe(this@JWTLoginActivity, Observer { isAuthorizedCached ->
                        progressLayout.visibility = View.GONE
                        isAuthorizedCached?.let {
                            if (it) {
                                onLoggedIn()
                            } else {
//                                jwtTokenText.visibility = View.VISIBLE
//                                loginButton.visibility = View.VISIBLE
//                                loginFailedTextView.visibility = View.GONE
                            }
                        }
                    })

                    loginViewModel.errorData.observe(this@JWTLoginActivity, Observer { errorMessage ->
                        progressLayout.visibility = View.GONE
                        onLoginFailed(errorMessage)
                    })

                    loginViewModel.initialize()
                }
    }

    override fun onBackPressed() {
        (application as WebexAndroidApp).closeApplication()
    }

    private fun onLoggedIn() {
        startActivity(Intent(this, SearchActivity3::class.java))
        finish()
    }

    private fun onLoginFailed(failureMessage: String = getString(R.string.jwt_login_failed)) {
//        binding.loginButton.visibility = View.VISIBLE
//        binding.loginFailedTextView.visibility = View.VISIBLE
//        binding.loginFailedTextView.text = failureMessage
    }
}