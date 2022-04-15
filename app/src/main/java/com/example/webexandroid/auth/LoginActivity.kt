package com.example.webexandroid.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Base64.encodeToString
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.webexandroid.databinding.ActivityLoginBinding
import com.example.webexandroid.R
import com.example.webexandroid.WebexAndroidApp
import com.example.webexandroid.utils.Constants
import com.example.webexandroid.utils.SharedPrefUtils.getEmailPref
import com.example.webexandroid.utils.SharedPrefUtils.getLoginTypePref
import com.example.webexandroid.utils.SharedPrefUtils.saveEmailPref
import com.google.android.gms.common.util.Hex
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseError
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nimbusds.jwt.JWT
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders.BASE64
import io.jsonwebtoken.io.Encoders
import io.jsonwebtoken.security.Keys
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.security.Key
import java.security.Signer
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import java.util.Base64.getEncoder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.HashMap

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private lateinit var userRef: DatabaseReference

    enum class LoginType(var value: String) {
        OAuth("OAuth"),
        JWT("JWT")
    }

    private var loginTypeCalled = LoginType.OAuth

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userRef=FirebaseDatabase.getInstance().getReference().child("Users");

        DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login)
            .also { binding = it }
            .apply {

                val type = getLoginTypePref(this@LoginActivity)

                textEmailAddress.setText(getEmailPref(this@LoginActivity))

                btnOauthLogin.setOnClickListener {
                    loginTypeCalled = LoginType.OAuth

                    var emailaddr = textEmailAddress.text
                    if (emailaddr.isEmpty()) {
                        runOnUiThread(Runnable {
                            Toast.makeText(
                                getApplicationContext(),
                                "Please enter email address for user",
                                Toast.LENGTH_LONG
                            ).show()
                        })
                        return@setOnClickListener
                    }
                    //var uid= java.util.UUID.randomUUID().toString()
                    var profileMap : HashMap<String, String>
                            = HashMap<String, String> ()
                    //profileMap.put("uid",uid)
                    var email=emailaddr.toString()
                    email=email.replace('.','*')
                    profileMap.put("email",emailaddr.toString())

                    val postListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if(dataSnapshot.exists()){

                            }
                            else{
                                userRef.child(email).updateChildren(profileMap as Map<String, Any>).addOnCompleteListener(
                                    OnCompleteListener {
                                        if(it.isSuccessful)
                                        {
                                            Toast.makeText(this@LoginActivity,"info updated", Toast.LENGTH_LONG)
                                        }
                                    })
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    }
                    userRef.orderByChild("email").equalTo(emailaddr.toString()).addValueEventListener(postListener)

//                    userRef.child(uid).updateChildren(profileMap as Map<String, Any>).addOnCompleteListener(
//                        OnCompleteListener {
//                            if(it.isSuccessful)
//                            {
//                                Toast.makeText(this@LoginActivity,"info updated", Toast.LENGTH_LONG)
//                            }
//                        })

                    saveEmailPref(this@LoginActivity, textEmailAddress.text.toString())
                    startOAuthActivity()
                }

                btnJwtLogin?.setOnClickListener{
                    loginTypeCalled = LoginType.JWT
//                    val key="GbE5QAITGiu7ah3Z5OwwXzheZm0DwDP4t8ah/JW03O4="
//                    val seckey = Keys.hmacShaKeyFor((BASE64.decode(key)))
//                    //Log.e("Secret encoded",seckey.toString())
//                    val now = Date()
//                    val jwt = Jwts.builder()
//                        .setHeaderParam("typ", "JWT")
//                        .setHeaderParam("alg","HS256")
//                        .claim("sub", "guest90")
//                        .claim("name","GuestUser20")
//                        .claim("iss","Y2lzY29zcGFyazovL3VzL09SR0FOSVpBVElPTi80ZThlNDgyZS03OGJhLTQ3MDMtOTY3Yy0zMzMxZTU0MjNiOGI")
//                        .setExpiration(Date(now.time + 2 * 1000 * 60 * 60))
//                        .signWith(seckey)
//                        .compact()
//                    //Log.e("JWTToken",jwt)
                    startJWTActivity()
                }
            }
    }


    private fun startOAuthActivity() {
        (application as WebexAndroidApp).loadKoinModules(loginTypeCalled)
        startActivity(Intent(this@LoginActivity, OAuthWebLoginActivity::class.java))
        finish()
    }

    private fun startJWTActivity() {
        (application as WebexAndroidApp).loadKoinModules(loginTypeCalled)
        startActivity(Intent(this@LoginActivity, JWTLoginActivity::class.java))
        finish()
    }
}
