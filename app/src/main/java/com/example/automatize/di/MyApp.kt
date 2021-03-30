package com.example.automatize.di

import android.app.Application
import com.example.automatize.view.activity.MainActivity
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(MainActivity())

            modules(mainModule)
        }

    }
}