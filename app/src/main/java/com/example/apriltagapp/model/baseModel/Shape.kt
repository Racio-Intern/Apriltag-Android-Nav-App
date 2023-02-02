package com.example.apriltagapp.model.baseModel

import com.example.apriltagapp.view.camera.MyRenderer

enum class BaseShape{
    LINE, TRIANGLE
}

data class Drawing(
    val type: BaseShape,
    val pos: Pos,
    val color: FloatArray
)

abstract class Shape(val renderer: MyRenderer){
    abstract var drawList: Array<Drawing>
    fun draw() {
        renderer.drawList = drawList
        renderer.state = true
    }
}
