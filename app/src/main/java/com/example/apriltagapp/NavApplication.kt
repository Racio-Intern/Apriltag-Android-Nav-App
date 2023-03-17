package com.example.apriltagapp

import android.app.Application
import com.example.apriltagapp.di.AppComponent
import com.example.apriltagapp.di.DaggerAppComponent

class NavApplication: Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.create()
    }
}