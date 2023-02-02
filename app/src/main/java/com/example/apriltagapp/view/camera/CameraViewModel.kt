package com.example.apriltagapp.view.camera

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.apriltagapp.ApriltagDetection
import com.example.apriltagapp.model.*
import com.example.apriltagapp.model.baseModel.Shape
import com.example.apriltagapp.model.baseShape.Rectangle

class CameraViewModel : ViewModel() {
    private val _curTag  = MutableLiveData<Tag>() // default 값
    private val _shape = MutableLiveData<Shape>()

    val curTag: LiveData<Tag>
        get() = _curTag
    val shape: LiveData<Shape>
        get() = _shape

    var destination: Int = 10

    // Spots Initialize
    val spots: Array<Spot> =
        arrayOf(Spot("항공대", 1000), Spot("현택이네", 1001), Spot("혁수네", 1002), Spot("은기네", 1003))

    // Tag Graph Initialize
    val tagGraph = TagGraph(tags_1)

    fun onFragmentCreated() {
        _curTag.value = tagGraph.tags[0]
    }

    /** renderer가 detection을 했을 때 호출하는 함수입니다. */
    fun onDetect(detection: ApriltagDetection, renderer: MyRenderer) {
//        if(curTag.value?.id == detection.id) {
//            onPreviousTagArrival(detection, renderer)
//        }
//        else {
            onNewTagArrival(detection, renderer)
//        }
    }

    /** 기존 tag와 새 tag가 일치할 때 호출하는 함수입니다. 수정된 좌표만 넘겨줍니다 */
    private fun onPreviousTagArrival(detection: ApriltagDetection, renderer: MyRenderer) {

    }

    /** 기존 tag와 다른 새로운 태그를 detect 했을 때 호출하는 함수입니다.*/
    private fun onNewTagArrival(detection: ApriltagDetection, renderer: MyRenderer) {
        val nextTag = tagGraph.shortestPath(detection.id, destination)
        val direction = _curTag.value?.run{
            this.linkedTags[nextTag.id]?.direction
        }
        _curTag.postValue(tagGraph.findTagById(detection.id))
        println("현재 태그 : ${_curTag.value?.id} / 목적지 : ${nextTag.id} / direction : $direction")
        if(direction == null) {
            println("direction을 찾지 못했습니다.")
            return
        }
        _shape.postValue(createShape(direction, renderer, detection.p))
    }

    private fun createShape(direction: Direction, renderer: MyRenderer, drawPos: DoubleArray): Shape {
        return when(direction) {

            Direction.DEFAULT ->
                Rectangle(renderer, drawPos)

            Direction.LEFT ->
                Rectangle(renderer, drawPos)

            Direction.RIGHT ->
                Rectangle(renderer, drawPos)

            Direction.BACKWARDS ->
                Rectangle(renderer, drawPos)

            Direction.STRAIT ->
                Rectangle(renderer, drawPos)

        }
    }
}