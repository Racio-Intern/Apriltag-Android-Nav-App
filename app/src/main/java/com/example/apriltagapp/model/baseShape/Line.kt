package com.example.apriltagapp.model.baseShape

import android.opengl.GLES20
import com.example.apriltagapp.view.camera.MyRenderer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Line {

    val VERTEX_SHADER_CODE =
            "uniform mat4 uMVPMatrix;\n" +
            "attribute vec4 aPosition;\n" +
            "void main(){" +
            "   gl_Position = uMVPMatrix * aPosition;" +
            "}";

    val FRAGMENT_SHADER_CODE = "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main(){" +
            "gl_FragColor = vColor;" +
            "}"

    private val color = arrayOf(0.0f, 0.0f, 1.0f, 1.0f).toFloatArray()
    private lateinit var vertexBuffer: FloatBuffer
    private var mProgram: Int = -1
    private var positionHandle: Int = -1
    private var colorHandle: Int = -1
    private var mMVPMatrixHandle: Int = -1


    init {
        var byteBuffer = ByteBuffer.allocateDirect(4 * 20 * 2)
        val vertexShader = MyRenderer.loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader  = MyRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)

        byteBuffer.order(ByteOrder.nativeOrder())
        vertexBuffer = byteBuffer.asFloatBuffer()
        vertexBuffer.position(0)

        mProgram = GLES20.glCreateProgram()

        GLES20.glAttachShader(mProgram, vertexShader)
        GLES20.glAttachShader(mProgram, fragmentShader)

        GLES20.glLinkProgram(mProgram)

    }

    fun draw(point: FloatArray, nPoints: Int, matrix: FloatArray) {
        /*
        if ( vertexBuffer.capacity() == 0 || 2 * nPoints > vertexBuffer.capacity() ) {
            val byteBuffer = ByteBuffer.allocateDirect(4 * 2 * nPoints)
            byteBuffer.order(ByteOrder.nativeOrder())
            vertexBuffer = byteBuffer.asFloatBuffer()
        } */

        vertexBuffer.position(0)
        vertexBuffer.put(point, 0, 2 * nPoints)
        vertexBuffer.position(0)


        GLES20.glUseProgram(mProgram)

        positionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition")

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)

        // mProgram 객체로부터 fragment shader의 'vColor' 맴버에 대한 핸들을 가져옴
        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")

        // triangle 렌더링 시 사용할 색으로 color 변수에 정의한 값을 사용
        GLES20.glUniform4fv(colorHandle, 1, color, 0)

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, matrix, 0)
        GLES20.glLineWidth(4.0f)
        // count만큼 triangle을 렌더링한다.
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, nPoints)

        // vertex 속성 비활성화
        GLES20.glDisableVertexAttribArray(positionHandle)

        // color 속성 비활성화
        GLES20.glDisableVertexAttribArray(colorHandle)
    }
}