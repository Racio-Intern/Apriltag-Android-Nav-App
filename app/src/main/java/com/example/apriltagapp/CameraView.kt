package com.example.apriltagapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CameraView(val mContext: Context) : GLSurfaceView(mContext), SurfaceHolder.Callback {

    init  {
        holder.addCallback(this)
    }

    companion object {
        init {
            System.loadLibrary("apriltag")
        }
    }



}
