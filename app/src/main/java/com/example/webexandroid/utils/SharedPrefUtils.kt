package com.example.webexandroid.utils

import android.content.Context
import com.example.webexandroid.auth.LoginActivity

object SharedPrefUtils {

    fun saveLoginTypePref(context:Context) {
        val pref = context.getSharedPreferences(Constants.Keys.WebexSharedPref, Context.MODE_PRIVATE)

        pref?.let {
            it.edit().putString(Constants.Keys.LoginType, "OAuth").apply()
        }
    }

    fun clearLoginTypePref(context:Context) {
        val pref = context.getSharedPreferences(Constants.Keys.WebexSharedPref, Context.MODE_PRIVATE)

        pref?.let {
            it.edit().remove(Constants.Keys.LoginType).apply()
        }
    }

    fun getLoginTypePref(context:Context): String? {
        val pref = context.getSharedPreferences(Constants.Keys.WebexSharedPref, Context.MODE_PRIVATE)

        pref?.let {
            return pref.getString(Constants.Keys.LoginType, null)
        }

        return null
    }

    fun saveEmailPref(context:Context, email: String) {
        val pref = context.getSharedPreferences(Constants.Keys.WebexSharedPref, Context.MODE_PRIVATE)
        pref?.edit()?.putString(Constants.Keys.Email, email)?.apply()
    }

    fun clearEmailPref(context:Context) {
        val pref = context.getSharedPreferences(Constants.Keys.WebexSharedPref, Context.MODE_PRIVATE)
        pref?.edit()?.remove(Constants.Keys.Email)?.apply()
    }

    fun getEmailPref(context:Context): String? {
        val pref = context.getSharedPreferences(Constants.Keys.WebexSharedPref, Context.MODE_PRIVATE)

        pref?.let {
            return pref.getString(Constants.Keys.Email, null)
        }

        return null
    }
}