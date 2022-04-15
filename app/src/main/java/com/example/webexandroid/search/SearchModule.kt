package com.example.webexandroid.search

import com.example.webexandroid.search.ui.dashboard.DashboardViewModel
import com.example.webexandroid.search.ui.home.HomeViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val searchModule = module {
    viewModel {
        HomeViewModel(get(), get(), get())
    }
    viewModel { DashboardViewModel(get(), get(), get()) }
    viewModel { JoinRequestViewModel(get(), get(), get(), get()) }
    single { SearchRepository(get()) }
}