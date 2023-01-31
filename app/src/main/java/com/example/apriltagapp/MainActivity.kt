package com.example.apriltagapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.apriltagapp.ApriltagNative.*
import com.example.apriltagapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(){
    external fun stringFromJNI(): String
    lateinit var binding: ActivityMainBinding
    lateinit var glSurfaceView: GLSurfaceView
    lateinit var renderer: MyRenderer
    lateinit var previewSurface: Surface

    companion object {
        const val IMAGE_BUFFER_SIZE = 1

        init {
            System.loadLibrary("apriltag")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)


        super.onCreate(savedInstanceState)
        glSurfaceView = GLSurfaceView(binding.viewSurface.context)
        val surface = glSurfaceView.holder.surface
        renderer = MyRenderer(glSurfaceView)
        setContentView(glSurfaceView)

        native_init()
        apriltag_init("tagStandard41h12", 2, 4.0, 0.0, 1)
    }

}



