package com.example.apriltagapp

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.view.SurfaceHolder
import android.view.View
import java.util.ArrayList

class CameraView(private val con: Context): GLSurfaceView(con), SurfaceTexture.OnFrameAvailableListener {
    val renderer: MyRenderer
    init {
        renderer = MyRenderer(this)
        setEGLContextClientVersion(2)
        setRenderer(renderer)

    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        TODO("Not yet implemented")
    }

    override fun onResume() {
        super.onResume()
        renderer.onResume()
    }

    override fun onPause() {
        super.onPause()
        renderer.onPause()
    }



}
