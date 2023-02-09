package com.example.apriltagapp.view.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.apriltagapp.model.Spot
import com.example.apriltagapp.model.Tag

/** 출발지와 목적지 장소를 firebase에서 받아옵니다*/
class SearchViewModel : ViewModel() {

    private val _destinationTag = MutableLiveData<Tag>()
    val destinationTag: LiveData<Tag>
        get() = _destinationTag

    val spots: Array<Spot> = arrayOf(
        Spot("시리우스", 1),
        Spot("나타", 1),
        Spot("찰리", 1),
        Spot("깅", 2),
        Spot("수", 2),
        Spot("간식", 3),
        Spot("정수기", 3),
        Spot("출입구", 4)
    )
    val spotsNames: ArrayList<String> = arrayListOf()

    init {
        for(spot in spots) {
            spotsNames.add(spot.name)
        }
    }

    fun onTransitionSet(position: Int) {
        println("출발지 : ${spots[position].name}")
    }

    fun onDestinationSet(position: Int) {
        println("도착지 : ${spots[position].name}")

    }

}