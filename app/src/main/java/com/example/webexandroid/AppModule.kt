package com.example.webexandroid

import com.example.webexandroid.utils.PermissionsHelper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val mainAppModule = module {
    single { PermissionsHelper(androidContext()) }
}