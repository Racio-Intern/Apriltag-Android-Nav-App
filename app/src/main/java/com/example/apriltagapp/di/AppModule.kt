package com.example.apriltagapp.di

import com.example.apriltagapp.model.repository.MyRepositoryImpl
import com.example.apriltagapp.model.repository.Repository
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
class AppModule {

    @Provides
    fun provideRepository(): Repository {
        return MyRepositoryImpl()
    }
}