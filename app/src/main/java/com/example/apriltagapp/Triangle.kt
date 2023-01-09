package com.example.apriltagapp

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Triangle {

    val VERTEX_SHADER_CODE = "attribute vec4 vPosition;" +
            "void main(){" +
            "gl_Position = vPosition;" +
            "}";

    val FRAGMENT_SHADER_CODE = "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main(){" +
            "gl_FragColor = vColor;" +
            "}"

    private val VERTAX = 3
    private var triangleCoords = arrayOf(
        0.0f, 0.0f, 1.0f,
        -1.5f, 0.5f, 0.0f,
        0.5f, 0.5f, -1.0f
    ).toFloatArray()
    private val color = arrayOf(0.0f, 1.0f, 1.0f, 0.3f).toFloatArray()
    private var vertexBuffer: FloatBuffer
    private var mProgram: Int = -1
    private var positionHandle: Int = -1
    private var colorHandle: Int = -1
    init {
        var byteBuffer = ByteBuffer.allocateDirect(triangleCoords.size * 4)
        val vertexShader = MyRenderer.loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader  = MyRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)

        byteBuffer.order(ByteOrder.nativeOrder())
        vertexBuffer = byteBuffer.asFloatBuffer()
        vertexBuffer.put(triangleCoords)
        vertexBuffer.position(0)

        mProgram = GLES20.glCreateProgram()

        GLES20.glAttachShader(mProgram, vertexShader)
        GLES20.glAttachShader(mProgram, fragmentShader)

        GLES20.glLinkProgram(mProgram)

    }

    fun draw() {
        GLES20.glUseProgram(mProgram)

        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, VERTAX, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")

        GLES20.glUniform4fv(colorHandle, 1, color, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, triangleCoords.size / VERTAX)

        GLES20.glDisableVertexAttribArray(positionHandle)

        GLES20.glDisableVertexAttribArray(colorHandle)
    }
}