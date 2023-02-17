package com.example.apriltagapp.model.baseModel

class UserCamera(x: Double, y: Double, r: Double) {
    var posX: Double = x
    var posY: Double = y
    var rot: Double  = r

    fun uiCoordsPos(): Pair<Int, Int> {
        return Pair(posX.toInt(), posY.toInt())
    }
}