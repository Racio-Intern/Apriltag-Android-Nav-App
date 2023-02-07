package com.example.apriltagapp.model.baseShape

import android.opengl.GLES20
import com.example.apriltagapp.model.baseModel.Pos
import com.example.apriltagapp.view.camera.MyRenderer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class ThreeDimentionLine {

    val VERTEX_SHADER_CODE =
            "uniform mat4 uMVPMatrix;\n" +
            "attribute vec3 vPosition;\n" +
            "void main(){" +
            "   gl_Position = uMVPMatrix * vec4(vPosition.x, vPosition.y, vPosition.z, 1.0);" +
            "}";

    val FRAGMENT_SHADER_CODE = "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main(){" +
            "gl_FragColor = vColor;" +
            "}"

    private val color = arrayOf(1.0f, 0.0f, 0.0f, 1.0f).toFloatArray()
    private var vertexBuffer: FloatBuffer
    private var mProgram: Int = -1
    private var positionHandle: Int = -1
    private var colorHandle: Int = -1
    private var mMVPMatrixHandle: Int = -1

    init {
        val byteBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
        val vertexShader = MyRenderer.loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader  = MyRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)

        byteBuffer.order(ByteOrder.nativeOrder())
        vertexBuffer = byteBuffer.asFloatBuffer()

        mProgram = GLES20.glCreateProgram()

        GLES20.glAttachShader(mProgram, vertexShader)
        GLES20.glAttachShader(mProgram, fragmentShader)

        GLES20.glLinkProgram(mProgram)
    }

    fun draw(pos: Pos, matrix: FloatArray) {
        // Reuse points buffer if possible
        if (vertexBuffer.capacity() < (pos.dimension * pos.nPoints)) {
            val byteBuffer = ByteBuffer.allocateDirect(4 * pos.dimension * pos.nPoints)
            byteBuffer.order(ByteOrder.nativeOrder())
            vertexBuffer = byteBuffer.asFloatBuffer()
        }

        vertexBuffer.position(0)
        vertexBuffer.put(pos.points, 0, pos.dimension * pos.nPoints)
        vertexBuffer.position(0)

        GLES20.glUseProgram(mProgram)

        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer)

        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
        GLES20.glUniform4fv(colorHandle, 1, color, 0)

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, matrix, 0)

        GLES20.glLineWidth(8.0f)
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, pos.nPoints)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
        GLES20.glUseProgram(0)
    }
}