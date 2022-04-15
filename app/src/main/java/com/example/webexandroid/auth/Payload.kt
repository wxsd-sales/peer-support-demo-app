package com.example.webexandroid.auth
import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Payload(val sub: String="guest-user-7349", val name: String="Guest User's Display Name",val iss:String="Y2lzY29zcGFyazovL3VzL09SR0FOSVpBVElPTi8yZmQ0ZTIwNC0wMTMxLTQxOGQtYTI1YS1iYTE5YTRkZjZlYWI",val exp: String="1511286849")
