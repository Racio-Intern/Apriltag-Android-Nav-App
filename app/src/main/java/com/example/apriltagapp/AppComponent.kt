package com.example.apriltagapp

import androidx.lifecycle.ViewModel
import com.example.apriltagapp.model.repository.Repository
import dagger.Component

@Component(modules = [AppModule::class])
interface AppComponent {

    fun getRepository(): Repository
}