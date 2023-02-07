package com.example.apriltagapp.view.camera

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apriltagapp.ApriltagDetection1
import com.example.apriltagapp.model.*
import com.example.apriltagapp.model.Shape.Arrow
import com.example.apriltagapp.model.baseModel.Shape
import com.example.apriltagapp.model.repository.TagFamilyRepository
import com.example.apriltagapp.utility.NonNullLiveData
import com.example.apriltagapp.utility.NonNullMutableLiveData
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel() {
    private val _shape = MutableLiveData<Shape>()
    private var direction = Direction.DEFAULT
    private val tagFamilyRepository = TagFamilyRepository()
    private val _tagGraph = NonNullMutableLiveData<TagGraph>(TagGraph(tags_1))

    val shape: LiveData<Shape>
        get() = _shape
    val tagGraph: NonNullLiveData<TagGraph>
        get() = _tagGraph

    private var destination: Int = 10
    private var currentTag: Tag = Tag()

    // Spots Initialize
    val spots: Array<Spot> =
        arrayOf(Spot("항공대"), Spot("현택이네"), Spot("혁수네"), Spot("은기네"))

    init {
        tagFamilyRepository.observeTagFamily(_tagGraph)
    }

    /** renderer가 detection을 했을 때 호출하는 함수입니다. */
    fun onDetect(detection: ApriltagDetection1, renderer: MyRenderer) {
        if(currentTag.id == detection.id) {
            onPreviousTagArrival(detection, renderer)
        }
        else {
            onNewTagArrival(detection, renderer)
        }
    }

    /** 기존 tag와 새 tag가 일치할 때 호출하는 함수입니다. 수정된 좌표만 넘겨줍니다 */
    private fun onPreviousTagArrival(detection: ApriltagDetection1, renderer: MyRenderer) {
        _shape.postValue(createShape(direction, renderer, detection.p))
    }

    /** 기존 tag와 다른 새로운 태그를 detect 했을 때 호출하는 함수입니다.*/
    private fun onNewTagArrival(detection: ApriltagDetection1, renderer: MyRenderer) {
        val nextTag = try {
            _tagGraph.value.shortestPath(detection.id, destination)
        }catch(e: Exception) {
            // shortest Path 검색 결과가 없을 때
            return
        }

        currentTag = _tagGraph.value.tagFamily.tagMap[detection.id]?:Tag()
        direction = currentTag.run{
            this.linkedTags[nextTag.id]?.direction
        }?:Direction.DEFAULT

        println("새로운 태그 : ${currentTag.id} / 목적지 : ${nextTag.id} / direction : $direction / Spots : ${currentTag.spots}")
        if(direction == Direction.DEFAULT) {
            println("direction을 찾지 못했습니다.")
            return
        }
        _shape.postValue(createShape(direction, renderer, detection.p))
    }

    private fun createShape(direction: Direction, renderer: MyRenderer, drawPos: DoubleArray): Shape {
        return when(direction) {

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

    private fun postTag(tag: Tag) {
        viewModelScope.launch {
            tagFamilyRepository.postTag(tag)
        }
    }

    private fun sendInitialQuery() {

    }
}