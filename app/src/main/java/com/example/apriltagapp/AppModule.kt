package com.example.apriltagapp

import com.example.apriltagapp.model.repository.MyRepositoryImpl
import com.example.apriltagapp.model.repository.Repository
import dagger.Module
import dagger.Provides

@Module
class AppModule {

    @Provides
    fun provideRepository(): Repository {
        return MyRepositoryImpl()
    }
}