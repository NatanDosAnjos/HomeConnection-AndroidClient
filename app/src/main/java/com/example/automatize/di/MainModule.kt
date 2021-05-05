package com.example.automatize.di

import com.example.automatize.model.ServerToConnect
import com.example.automatize.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {
    single { ServerToConnect(context = get()) }

    viewModel {
        MainViewModel(server = get())
    }
}