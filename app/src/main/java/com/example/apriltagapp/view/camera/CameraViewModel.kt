package com.example.apriltagapp.view.camera

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apriltag.ApriltagDetection
import com.example.apriltagapp.model.*
import com.example.apriltagapp.model.repository.TagFamilyRepository
import com.example.apriltagapp.utility.NonNullLiveData
import com.example.apriltagapp.utility.NonNullMutableLiveData
import com.example.apriltagapp.utility.ParcelableArrivals
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel() {
    private var direction = Direction.DEFAULT
    private val tagFamilyRepository = TagFamilyRepository()
    private val _tagGraph = NonNullMutableLiveData<TagGraph>(TagGraph(tags_1))
    val tagGraph: NonNullLiveData<TagGraph>
        get() = _tagGraph

    private val _spots = MutableLiveData<HashMap<String, Int>>()
    val spots: LiveData<HashMap<String, Int>>
        get() = _spots


    private var transTag = Tag()
    private var destTag = Tag()
    private var currentTag: Tag = Tag()

    init {
        tagFamilyRepository.observeTagFamily(_tagGraph)
        tagFamilyRepository.observeSpots(_spots)
    }

    fun onViewCreate(receivedData: ParcelableArrivals) {
        println("전달받은 목적지 : ${receivedData.destination}")
        _spots.value?.let { spot ->
            //목적지
            var tagId = spot[receivedData.destination]
            destTag = _tagGraph.value.tagFamily.tagMap[tagId] ?: return

            //출발지
            tagId = spot[receivedData.transition]
            transTag = _tagGraph.value.tagFamily.tagMap[tagId] ?: return

        }

    }

    /** renderer가 detection을 했을 때 호출하는 함수입니다. */
    fun onDetect(detection: ApriltagDetection) {
        println("detected!!")
        if (currentTag.id == detection.id) {
            onPreviousTagArrival(detection)
        } else {
            onNewTagArrival(detection)
        }
    }

    /** 기존 tag와 새 tag가 일치할 때 호출하는 함수입니다. 수정된 좌표만 넘겨줍니다 */
    private fun onPreviousTagArrival(detection: ApriltagDetection) {

    }

    /** 기존 tag와 다른 새로운 태그를 detect 했을 때 호출하는 함수입니다.*/
    private fun onNewTagArrival(detection: ApriltagDetection) {

        // ApriltagDetection을 통해 현재 위치의 Tag 탐지
        currentTag = _tagGraph.value.tagFamily.tagMap[detection.id] ?: run {
            Log.d("ERROR", "Error : Detected tag not in tag family")
            return
        }

        // 목적지를 가기 위해 다음으로 가야하는 Tag 검색
        val nextTag: Tag = _tagGraph.value.shortestPath(detection.id, destTag.id) ?: run {
            Log.d("ERROR", "Error : Shortest path returns null")
            return
        }

        println("next tag: ${nextTag.id}")

        // 다음 Tag로 가기 위한 방향 설정
        for (tag in currentTag.linkedTags) {
            if (tag.id == nextTag.id) {
                direction = tag.direction
                println("새로운 태그 : ${currentTag.id} / 목적지 : ${nextTag.id} / direction : $direction / Spots : ${currentTag.spots}")
                return
            }
        }

        println("direction을 찾지 못했습니다.")
        direction = Direction.DEFAULT
    }

    private fun createShape() {
    }

    private fun postTag(tag: Tag) {
        viewModelScope.launch {
            tagFamilyRepository.postTag(tag)
        }
    }

    private fun sendInitialQuery() {

    }
}