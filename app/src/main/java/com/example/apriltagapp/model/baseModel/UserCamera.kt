package com.example.apriltagapp.model.baseModel

import kotlin.math.*


class UserCamera(x: Double, y: Double, r: Double) {
    companion object {
        const val RAD2DEG = 180 / PI
    }

    var posX: Double = x
    var posY: Double = y
    var rotRad: Double  = r // 라디안 ( 0 ~ 3.14... )

    /**
     * camera estimation 으로 나온 좌표계를 UI용 int 좌표계로 변환한 값을 반환합니다.
     */
    fun getUICoords(): Pair<Int, Int> {
        return Pair(posX.toInt(), posY.toInt())
    }

//    private fun getRotVector(): Pair<Double, Double> {
//        return Pair(cos(rotRad), sin(rotRad))
//    }
//
//    fun getImgRotAngle(): Double {
//        val r1 = atan(posY / posX)
//        val rotVector = getRotVector()
//        val cosR2 = -(posX * rotVector.first + posY * rotVector.second) / (hypot(posX, posY) * hypot(rotVector.first, rotVector.second))
//        val r2 = acos(cosR2)
//
//        return abs(90 - (r1 + r2) * RAD2DEG)
//
//    }
}