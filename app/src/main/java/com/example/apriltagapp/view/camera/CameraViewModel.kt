package com.example.apriltagapp.view.camera

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apriltag.ApriltagDetection
import com.example.apriltagapp.model.*
import com.example.apriltagapp.model.repository.TagFamilyRepository
import com.example.apriltagapp.utility.NonNullLiveData
import com.example.apriltagapp.utility.NonNullMutableLiveData
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel() {
    private var direction = Direction.DEFAULT
    private val tagFamilyRepository = TagFamilyRepository()
    private val _tagGraph = NonNullMutableLiveData<TagGraph>(TagGraph(tags_1))

    val tagGraph: NonNullLiveData<TagGraph>
        get() = _tagGraph

    private var destination: Int = 10
    private var currentTag: Tag = Tag()

    // Spots Initialize
    val spots: Array<Spot> =
        arrayOf(Spot("항공대", 1), Spot("현택이네", 1), Spot("혁수네", 2), Spot("은기네", 2))

    init {
        tagFamilyRepository.observeTagFamily(_tagGraph)
    }

    /** renderer가 detection을 했을 때 호출하는 함수입니다. */
    fun onDetect(detection: ApriltagDetection) {
        if(currentTag.id == detection.id) {
            onPreviousTagArrival(detection)
        }
        else {
            onNewTagArrival(detection)
        }
    }

    /** 기존 tag와 새 tag가 일치할 때 호출하는 함수입니다. 수정된 좌표만 넘겨줍니다 */
    private fun onPreviousTagArrival(detection: ApriltagDetection) {

    }

    /** 기존 tag와 다른 새로운 태그를 detect 했을 때 호출하는 함수입니다.*/
    private fun onNewTagArrival(detection: ApriltagDetection) {

        // ApriltagDetection을 통해 현재 위치의 Tag 탐지
        currentTag = _tagGraph.value.tagFamily.tagMap[detection.id]?:return

        // 목적지를 가기 위해 다음으로 가야하는 Tag 검색
        val nextTag: Tag = _tagGraph.value.shortestPath(detection.id, destination)?:return

        // 다음 Tag로 가기 위한 방향 설정
        for(tag in currentTag.linkedTags) {
            if(tag.id == nextTag.id) {
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