package com.example.apriltagapp.view.camera

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apriltag.ApriltagDetection
import com.example.apriltagapp.model.*
import com.example.apriltagapp.model.baseModel.UserCamera
import com.example.apriltagapp.model.repository.TagFamilyRepository
import com.example.apriltagapp.utility.NonNullLiveData
import com.example.apriltagapp.utility.NonNullMutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

class CameraViewModel : ViewModel() {
    private val LOGTAG = "CameraViewModel"
    private val _userCamera = MutableLiveData<UserCamera>(UserCamera(3800.0, 2900.0, 0.0))
    val userCamera: LiveData<UserCamera>
        get() = _userCamera

    private val _isRunning = MutableLiveData<Boolean>(false)

    val isRunning: LiveData<Boolean>
        get() = _isRunning

    private val _estimatedPos = MutableLiveData<Pair<Double, Double>>(Pair(0.0, 0.0))
    val estimatedPos: LiveData<Pair<Double, Double>>
        get() = _estimatedPos


    var direction = Direction.DEFAULT
    private val tagFamilyRepository = TagFamilyRepository()

    private val _tagGraph = NonNullMutableLiveData<TagGraph>(TagGraph(tags_1))
    val tagGraph: NonNullLiveData<TagGraph>
        get() = _tagGraph

    private var destTag = Tag()
    private var currentTag: Tag = Tag()

    init {
        tagFamilyRepository.observeTagFamily(_tagGraph)

        var count = 0


        CoroutineScope(Dispatchers.IO).launch {
            var x = 500.0
            var y = 500.0

            var destX = 1700.0 // 1900
            var destY = 1900.0 // 1300

            var unit = 100.0 / 200

            var r = 90 - atan((3/7).toDouble()) * UserCamera.RAD2DEG - 20
            while(count < 50) {
                count ++
                x += -7 * unit
                y += -3 * unit
//                r -= 0.5
                _userCamera.postValue(UserCamera(x, y, r))
                delay((1000 / CameraFragment.FPS).toLong()) // delay(ms) = 1000ms / FPS
            }
            count = 0

            while(count < 90) {
                count ++
                r -= 1
                _userCamera.postValue(UserCamera(x, y, r))
                delay((1000 / CameraFragment.FPS).toLong()) // delay(ms) = 1000ms / FPS
            }

            count = 0
            while(count < 50) {
                count ++
                x += 2.5 * unit
                y += -7.5 * unit
                _userCamera.postValue(UserCamera(x, y, r))
                delay((1000 / CameraFragment.FPS).toLong()) // delay(ms) = 1000ms / FPS
            }

        }
    }

    fun onSpotsObserved(receivedTagId: Int) {
        //목적지
        destTag = _tagGraph.value.tagFamily.tagMap[receivedTagId] ?: return

    }

    /** renderer가 detection을 했을 때 호출하는 함수입니다. */
    fun onDetect(detection: ApriltagDetection) {
        if(destTag.id < 0) {
            Log.e(LOGTAG, "Error : 목적지가 비정상적입니다 ")
            return
        }
        if (currentTag.id == detection.id) {
            onPreviousTagArrival(detection)
        } else {
            _isRunning.postValue(true)
            onNewTagArrival(detection)
            _isRunning.postValue(false)
        }

//        println("${estPosMatrix[0]}, ${estPosMatrix[1]}, ${estPosMatrix[2]}")

    }

    fun onCameraFrame(estPosMat: DoubleArray) {
        _estimatedPos.postValue(estimateCameraPos(estPosMat[0], estPosMat[1], estPosMat[2]))
        //Log.d(LOGTAG, "camera pos : ${cameraPos.first} / ${cameraPos.second}")
    }

    /** 기존 tag와 새 tag가 일치할 때 호출하는 함수입니다. 수정된 좌표만 넘겨줍니다 */
    private fun onPreviousTagArrival(detection: ApriltagDetection) {

    }

    /** 기존 tag와 다른 새로운 태그를 detect 했을 때 호출하는 함수입니다.*/
    private fun onNewTagArrival(detection: ApriltagDetection) {

        // ApriltagDetection을 통해 현재 위치의 Tag 탐지
        currentTag = _tagGraph.value.tagFamily.tagMap[detection.id] ?: run {
            Log.e(LOGTAG, "Error : Tag not in tag family")
            return
        }

        // 목적지를 가기 위해 다음으로 가야하는 Tag 검색
        val nextTag: Tag = _tagGraph.value.shortestPath(detection.id, destTag.id) ?: run {
            Log.e(LOGTAG, "Error : Shortest path returns null")
            direction = Direction.DEFAULT
            return
        }


        // 다음 Tag로 가기 위한 방향 설정
        for (tag in currentTag.linkedTags) {
            if (tag.id == nextTag.id) {
                direction = tag.direction
                println("새로운 태그 : ${currentTag.id} / 목적지 : ${nextTag.id} / direction : $direction / Spots : ${currentTag.spots}")
                return
            }
        }

        Log.e(LOGTAG, "direction을 찾지 못했습니다.")
        direction = Direction.DEFAULT
    }


    private fun postTag(tag: Tag) {
        viewModelScope.launch {
            tagFamilyRepository.postTag(tag)
        }
    }

    private fun estimateCameraPos(x: Double, y: Double, z: Double): Pair<Double, Double> {

        val distance = hypot(x, z)
        val theta = atan(z / x) - currentTag.rot
//        println(" x / y / z :$x / $y / $z")
//        println("theta : ${theta / PI}ㅠ")


        val cos = if(theta > 0) cos(theta) else -cos(theta)
        val sin = if(theta > 0) sin(theta) else -sin(theta)
        val camPosX = currentTag.x + distance * cos
        val camPosY = currentTag.y + distance * sin
        return Pair(camPosX, camPosY)
    }
}