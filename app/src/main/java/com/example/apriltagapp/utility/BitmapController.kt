package com.example.apriltagapp.utility

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import com.example.apriltagapp.view.camera.CameraFragment

class BitmapController() {

    /** 전달받은 bitmap을 degree만큼 회전시킨 bitmap을 반환합니다.**/
    fun rotateImage(src: Bitmap, degree: Float): Bitmap {
        // Matrix 객체 생성
        val matrix = Matrix()
        // 회전 각도 셋팅
        matrix.postRotate(degree)
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(
            src, 0, 0, src.width,
            src.height, matrix, true
        )
    }

    /** 전달받은 (x, y)좌표와 길이를 기준으로 정사각형 모양의 bitmap을 반환합니다.
     * 기준대로 crop하지 못 할 경우 null을 반환합니다.
      */
    fun cropSquareArea(bitmap: Bitmap, x: Int, y: Int, squareLength: Int): Bitmap? {
        val result = try {
            Bitmap.createBitmap(bitmap, x, y, squareLength, squareLength)
        } catch (e: RuntimeException) {
            return null
        }

        return result
    }
}