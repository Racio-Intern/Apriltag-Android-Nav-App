package com.example.apriltagapp.view.camera

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.apriltagapp.ApriltagDetection
import com.example.apriltagapp.model.*
import com.example.apriltagapp.model.Shape.Arrow
import com.example.apriltagapp.model.baseModel.Shape

class CameraViewModel : ViewModel() {
    private val _curTag  = MutableLiveData<Tag>() // default 값
    private val _shape = MutableLiveData<Shape>()
    private var direction = Direction.DEFAULT

    val curTag: LiveData<Tag>
        get() = _curTag
    val shape: LiveData<Shape>
        get() = _shape

    var destination: Int = 10

    // Spots Initialize
    val spots: Array<Spot> =
        arrayOf(Spot("항공대", 1000), Spot("현택이네", 1001), Spot("혁수네", 1002), Spot("은기네", 1003))

    // Tag Graph Initialize
    private val tagGraph = TagGraph(tags_1)

    fun onFragmentCreated() {
        _curTag.value = tagGraph.tagFamily.getOrDefault(1, Tag(1))
    }

    /** renderer가 detection을 했을 때 호출하는 함수입니다. */
    fun onDetect(detection: ApriltagDetection, renderer: MyRenderer) {
        if(curTag.value?.id == detection.id) {
            onPreviousTagArrival(detection, renderer)
        }
        else {
            onNewTagArrival(detection, renderer)
        }
    }

    /** 기존 tag와 새 tag가 일치할 때 호출하는 함수입니다. 수정된 좌표만 넘겨줍니다 */
    private fun onPreviousTagArrival(detection: ApriltagDetection, renderer: MyRenderer) {
        _shape.postValue(createShape(direction, renderer, detection.p))
        println("현재 태그 : ${_curTag.value?.id}")
    }

    /** 기존 tag와 다른 새로운 태그를 detect 했을 때 호출하는 함수입니다.*/
    private fun onNewTagArrival(detection: ApriltagDetection, renderer: MyRenderer) {
        val nextTag = try {
            tagGraph.shortestPath(detection.id, destination)
        }catch(e: Exception) {
            // shortest Path 검색 결과가 없을 때
            return
        }

        direction = _curTag.value?.run{
            this.linkedTags[nextTag.id]?.direction
        }?:Direction.DEFAULT

        _curTag.postValue(tagGraph.tagFamily[detection.id])
        println("새로운 태그 : ${_curTag.value?.id} / 목적지 : ${nextTag.id} / direction : $direction")
        if(direction == Direction.DEFAULT) {
            println("direction을 찾지 못했습니다.")
            return
        }
        _shape.postValue(createShape(direction, renderer, detection.p))
    }

    private fun createShape(direction: Direction, renderer: MyRenderer, drawPos: DoubleArray): Shape {
        return when(direction) {

        //_shape.postValue(Rectangle(renderer, detection.p)) // tag의 결과가 rectangle이라고 치고
            Direction.DEFAULT ->
                Arrow(renderer, drawPos, direction)

            Direction.LEFT ->
                Arrow(renderer, drawPos, direction)

            Direction.RIGHT ->
                Arrow(renderer, drawPos, direction)

            Direction.BACKWARDS ->
                Arrow(renderer, drawPos, direction)

            Direction.STRAIT ->
                Arrow(renderer, drawPos, direction)

        }
    }
}