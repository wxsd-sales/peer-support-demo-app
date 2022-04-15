package com.example.webexandroid.calling

import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val callModule = module {
    viewModel {
        CallViewModel(get())
    }
}