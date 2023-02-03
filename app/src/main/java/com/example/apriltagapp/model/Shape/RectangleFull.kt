package com.example.apriltagapp.model.Shape

import com.example.apriltagapp.model.baseModel.BaseShape
import com.example.apriltagapp.model.baseModel.Drawing
import com.example.apriltagapp.model.baseModel.Pos
import com.example.apriltagapp.model.baseModel.Shape
import com.example.apriltagapp.view.camera.MyRenderer
import java.util.*

class RectangleFull(renderer: MyRenderer, det: DoubleArray): Shape(renderer) {
    override lateinit var drawList: ArrayList<Drawing>

    init {
        val points = FloatArray(8)

        for( i in 0..3) {
            val x = 1f - (det[2*i + 1] / renderer.mPreviewSize.height.toFloat())
            val y = 1f - (det[2*i + 0] / renderer.mPreviewSize.width.toFloat())
            points[2 * i + 0] = x.toFloat()
            points[2 * i + 1] = y.toFloat()
        }

        // Determine corner points
        val point_0 = Arrays.copyOfRange(points, 0, 2)
        val point_1 = Arrays.copyOfRange(points, 2, 4)
        val point_2 = Arrays.copyOfRange(points, 4, 6)
        val point_3 = Arrays.copyOfRange(points, 6, 8)

        val result = floatArrayOf( point_1[0], point_1[1], point_2[0], point_2[1],
            point_0[0], point_0[1], point_3[0], point_3[1],)

        val pos = Pos(2, 4, result)
        drawList = arrayListOf(Drawing(BaseShape.TRIANGLE, pos, floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f)))
    }

}