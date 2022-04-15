package com.example.webexandroid.auth

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Header(val typ:String="JWT",val alg:String="HS256")
