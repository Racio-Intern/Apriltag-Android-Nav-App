package com.example.apriltagapp.di

import com.example.apriltagapp.model.repository.Repository
import com.example.apriltagapp.view.ViewModelFactory
import dagger.Component

@Component(modules = [AppModule::class, ViewModelModule::class, ViewModelFactoryModule::class])
interface AppComponent {

    fun getRepository(): Repository

    fun getViewModelFactory(): ViewModelFactory
}