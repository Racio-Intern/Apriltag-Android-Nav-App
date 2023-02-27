package com.example.apriltagapp.view.camera

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import apriltag.ApriltagDetection
import apriltag.ApriltagPosEstimation
import com.example.apriltagapp.model.*
import com.example.apriltagapp.model.baseModel.UserCamera
import com.example.apriltagapp.model.repository.TagFamilyRepository
import com.example.apriltagapp.utility.NonNullLiveData
import com.example.apriltagapp.utility.NonNullMutableLiveData
import kotlin.math.*

class CameraViewModel : ViewModel() {
    private val LOGTAG = "CameraViewModel"
    private val _userCamera = NonNullMutableLiveData<UserCamera>(UserCamera(425.0, 675.0, 0.0))
    val userCamera: NonNullLiveData<UserCamera>
        get() = _userCamera

    private val _isRunning = MutableLiveData<Boolean>(false)

    val isRunning: LiveData<Boolean>
        get() = _isRunning

    private val _estimatedPos = MutableLiveData<Pair<Double, Double>>(Pair(0.0, 0.0))
    val estimatedPos: LiveData<Pair<Double, Double>>
        get() = _estimatedPos


    var direction = Direction.DEFAULT
    private val tagFamilyRepository = TagFamilyRepository()

    private val _tagGraph = NonNullMutableLiveData<TagGraph>(TagGraph(arrayListOf()))
    val tagGraph: NonNullLiveData<TagGraph>
        get() = _tagGraph

    private var destTag = Tag()
    private var currentTag: Tag = Tag()

    init {
        tagFamilyRepository.observeTagFamily(_tagGraph)
        var count = 0


//        CoroutineScope(Dispatchers.IO).launch {
//            var x = 500.0
//            var y = 500.0
//
//            var destX = 1700.0 // 1900
//            var destY = 1900.0 // 1300
//
//            var unit = 100.0 / 200
//
//            var r = 90 - atan((3/7).toDouble()) * UserCamera.RAD2DEG - 20
//            while(count < 50) {
//                count ++
//                x += -7 * unit
//                y += -3 * unit
////                r -= 0.5
//                _userCamera.postValue(UserCamera(x, y, r))
//                delay((1000 / CameraFragment.FPS).toLong()) // delay(ms) = 1000ms / FPS
//            }
//            count = 0
//
//            while(count < 90) {
//                count ++
//                r -= 1
//                _userCamera.postValue(UserCamera(x, y, r))
//                delay((1000 / CameraFragment.FPS).toLong()) // delay(ms) = 1000ms / FPS
//            }
//
//            count = 0
//            while(count < 50) {
//                count ++
//                x += 2.5 * unit
//                y += -7.5 * unit
//                _userCamera.postValue(UserCamera(x, y, r))
//                delay((1000 / CameraFragment.FPS).toLong()) // delay(ms) = 1000ms / FPS
//            }
//
//        }
    }

    /**
     * 목적지의 tag id를 전달받고 destination tag로 설정합니다.
     */
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

    /**
     * camera view의 onCameraFrame에 호출하는 함수입니다.
     * opencv에서 추정한 pos matrix를 전달받아 위치 추정을 하고, 그 결과를 livedata 에 반영합니다.
     */
    fun onCameraFrame(posEstimations: ArrayList<ApriltagPosEstimation>) {
        var avgX = 0.0
        var avgY = 0.0
        var count = 0

        for(est in posEstimations){
            val absPos = estimateAbsCamPos(est)
            if(absPos == null) {
                count ++
                continue
            }
            avgX += absPos.first
            avgY += absPos.second
        }
        _estimatedPos.postValue(Pair(avgX / (posEstimations.size - count), avgY / (posEstimations.size - count)))

        _userCamera.value.updatePos(avgX / posEstimations.size, avgY / posEstimations.size, 0.0)
        _userCamera.postValue(_userCamera.value)


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

    /**
     * opencv가 구한 카메라 추정 위치를 이용해 평면 좌표계상 위치를 추정합니다.
     */
    private fun estimateAbsCamPos(posEstimation: ApriltagPosEstimation): Pair<Double, Double>? {
        val x = posEstimation.relativePos[0]
        val z = posEstimation.relativePos[2]
        val detectedTag = tagGraph.value.tagFamily.tagMap[posEstimation.id]?:return null

        val distance = hypot(x, z)
        val theta = atan(z / x) - detectedTag.rot
//        println(" x / y / z :$x / $y / $z")
//        println("theta : ${theta / PI}ㅠ")


        val cos = if(theta > 0) cos(theta) else -cos(theta)
        val sin = if(theta > 0) sin(theta) else -sin(theta)
        val camPosX = detectedTag.x - distance * cos
        val camPosY = detectedTag.y + distance * sin
//        println("cam pos :${detectedTag.x} $camPosX $camPosY")
        return Pair(camPosX, camPosY)
    }
}