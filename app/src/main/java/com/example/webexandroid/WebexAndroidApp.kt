package com.example.webexandroid

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.webexandroid.person.personModule
import com.example.webexandroid.calling.callModule
import com.example.webexandroid.auth.LoginActivity
import com.example.webexandroid.auth.loginModule
import com.example.webexandroid.messaging.messagingModule
import com.example.webexandroid.search.searchModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.unloadKoinModules


class WebexAndroidApp : Application(), LifecycleObserver {

    companion object {
        lateinit var instance: WebexAndroidApp
            private set

        fun applicationContext(): Context {
            return instance.applicationContext
        }

        fun get(): WebexAndroidApp {
            return instance
        }

        var inForeground: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@WebexAndroidApp)
        }
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        instance = this
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        // app moved to foreground
        inForeground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        // app moved to background
        inForeground = false
    }

    fun closeApplication() {
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    fun loadKoinModules(type: LoginActivity.LoginType) {
        when (type) {
            LoginActivity.LoginType.JWT -> {
                loadKoinModules(listOf(mainAppModule, webexModule, loginModule, JWTWebexModule, searchModule, callModule, messagingModule, personModule))
            }
            else -> {
                loadKoinModules(listOf(mainAppModule, webexModule, loginModule, OAuthWebexModule, searchModule, callModule, messagingModule, personModule))
            }
        }
        //loadKoinModules(listOf(mainAppModule, webexModule, loginModule,JWTWebexModule, OAuthWebexModule, searchModule, callModule, messagingModule, personModule))
    }

    fun unloadKoinModules() {
        unloadKoinModules(listOf(mainAppModule, webexModule, loginModule, JWTWebexModule, OAuthWebexModule, searchModule, callModule, messagingModule, personModule))
    }
}