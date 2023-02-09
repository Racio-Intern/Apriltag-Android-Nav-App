package com.example.apriltagapp.view.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.apriltagapp.model.Spot
import com.example.apriltagapp.model.Tag
import com.example.apriltagapp.model.repository.TagFamilyRepository
import com.example.apriltagapp.utility.NonNullLiveData
import com.example.apriltagapp.utility.NonNullMutableLiveData

/** 출발지와 목적지 장소를 firebase에서 받아옵니다*/
class SearchViewModel : ViewModel() {
    private val tagFamilyRepository = TagFamilyRepository()

    private val _spots = MutableLiveData<HashMap<String, Int>>()
    val spots: LiveData<HashMap<String, Int>>
        get() = _spots

    private val _destination = MutableLiveData<Spot>()
    val destination: LiveData<Spot>
        get() = _destination

    init {
        // firebase에서 spot 데이터를 불러옵니다.
        tagFamilyRepository.observeSpots(_spots)
    }
}