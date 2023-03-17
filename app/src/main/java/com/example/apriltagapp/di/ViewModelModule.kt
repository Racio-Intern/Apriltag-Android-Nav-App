package com.example.apriltagapp.di

import androidx.lifecycle.ViewModel
import com.example.apriltagapp.view.camera.CameraViewModel
import com.example.apriltagapp.view.entry.EntryViewModel
import com.example.apriltagapp.view.search.SearchViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class ViewModelModule {

    @Provides
    @IntoMap
    @ViewModelKey(CameraViewModel::class)
    fun provideCameraViewModel(): ViewModel {
        return CameraViewModel()
    }

    @Provides
    @IntoMap
    @ViewModelKey(EntryViewModel::class)
    fun provideEntryViewModel(): ViewModel {
        return EntryViewModel()
    }

    @Provides
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    fun provideSearchViewModel(): ViewModel {
        return SearchViewModel()
    }

}