package com.example.webexandroid

import com.example.webexandroid.calling.RingerManager
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val webexModule = module(createdAtStart = true) {
    single { WebexRepository(get()) }
    single { RingerManager(get()) }

    viewModel {
        WebexViewModel(get(), get())
    }
}