package com.example.apriltagapp.model.Shape

import android.graphics.PointF
import com.example.apriltagapp.model.Direction
import com.example.apriltagapp.model.baseModel.BaseShape
import com.example.apriltagapp.model.baseModel.Drawing
import com.example.apriltagapp.model.baseModel.Pos
import com.example.apriltagapp.model.baseModel.Shape
import com.example.apriltagapp.view.camera.MyRenderer
import java.util.*

/*
    point_0 = (det[0], det[1]),  point_1 = (det[2], det[3]),
    point_2 = (det[4], det[5]),  point_3 = (det[6], det[7]),
*/

/*
     in cameraView
   +y <-------------------------------------
      point_1              point_0
         *********************
         *                   *
         *                   *
 point_b *                   * point_a
         *                   *
         *                   *
         *                   *
         *********************
       point_2             points_3
                                          +x
 */

/* in surfaceView(실제가 우리한테 보이는 뷰)
      point_3              point_2
         *********************
         *                   *
         *                   *
 point_a *                   * point_b
         *                   *
         *                   *
         *                   *
         *********************
       point_0             points_1
 ------------------------------------------> +y
 */

class Arrow(renderer: MyRenderer, det: DoubleArray, direction: Direction): Shape(renderer) {
    override lateinit var drawList: ArrayList<Drawing>
    private val pointA: PointF
    private val pointB: PointF

    init {
        pointA = PointF(((det[2*0 + 0] + det[2*3 + 0]) / 2.0).toFloat(), ((det[2*0 + 1] + det[2*3 + 1]) / 2.0).toFloat())
        pointB = PointF(((det[2*1 + 0] + det[2*2 + 0]) / 2.0).toFloat(), ((det[2*1 + 1] + det[2*2 + 1]) / 2.0).toFloat())

        // 1. draw rectangle
        val newDetection = DoubleArray(8)

        for(i in 0..3) {
            val point: PointF = if(i == 0 || i == 3) pointB else pointA

            newDetection[2*i + 0] = 2.0 * det[2*i + 0] - point.x.toDouble()
            newDetection[2*i + 1] = 2.0 * det[2*i + 1] - point.y.toDouble()
        }

        val points = FloatArray(8)

        for( i in 0..3) {
            val x = 0.5f - (newDetection[2*i + 1] / renderer.mPreviewSize.height.toFloat())
            val y = 0.5f - (newDetection[2*i + 0] / renderer.mPreviewSize.width.toFloat())
            points[2 * i + 0] = x.toFloat()
            points[2 * i + 1] = y.toFloat()
        }

        // Determine corner points
        val point_0 = points.copyOfRange(0, 2)
        val point_1 = points.copyOfRange(2, 4)
        val point_2 = points.copyOfRange(4, 6)
        val point_3 = points.copyOfRange(6, 8)

        val rectanglePos = Pos(2, 4,
            floatArrayOf( point_1[0], point_1[1], point_2[0], point_2[1],
            point_0[0], point_0[1], point_3[0], point_3[1]))
        drawList = arrayListOf(Drawing(BaseShape.TRIANGLE, rectanglePos, floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f)))

        // 2. draw triangle
        val trianglePoints = FloatArray(6)
        if (direction == Direction.RIGHT) { // right
            trianglePoints[0] = (2.0f * det[2 * 1 + 0] - det[2 * 3 + 0]).toFloat()
            trianglePoints[1] = (2.05f * det[2 * 1 + 1] - det[2 * 3 + 1]).toFloat()

            trianglePoints[2] = (2.0f * det[2 * 2 + 0] - det[2 * 0 + 0]).toFloat()
            trianglePoints[3] = (2.05f * det[2 * 2 + 1] - det[2 * 0 + 1]).toFloat()

            trianglePoints[4] = ((pointB.x - pointA.x) * 2.0f + pointB.x)
            trianglePoints[5] = ((pointB.y - pointA.y) * 2.0f + pointB.y)
        }
        else if(direction == Direction.LEFT){ // left
            trianglePoints[0] = (2.0f * det[2 * 3 + 0] - det[2 * 1 + 0]).toFloat()
            trianglePoints[1] = (2.0f * det[2 * 3 + 1] - det[2 * 1 + 1]).toFloat()

            trianglePoints[2] = (2.0f * det[2 * 0 + 0] - det[2 * 2 + 0]).toFloat()
            trianglePoints[3] = (2.0f * det[2 * 0 + 1] - det[2 * 2 + 1]).toFloat()

            trianglePoints[4] = ((pointA.x - pointB.x) * 2.0f + pointA.x)
            trianglePoints[5] = ((pointA.y - pointB.y) * 2.0f + pointA.y)
        }
        val newPoints = FloatArray(6)

        for( i in 0..2) {
            val x = 0.5f - (trianglePoints[2*i + 1] / renderer.mPreviewSize.height.toFloat())
            val y = 0.5f - (trianglePoints[2*i + 0] / renderer.mPreviewSize.width.toFloat())
            newPoints[2 * i + 0] = x
            newPoints[2 * i + 1] = y
        }

        val trianglePos = Pos(2, 3, newPoints)
        drawList.add(Drawing(BaseShape.TRIANGLE, trianglePos, floatArrayOf(0.0f, 0.0f, 1.0f, 1.0f)))
    }
}