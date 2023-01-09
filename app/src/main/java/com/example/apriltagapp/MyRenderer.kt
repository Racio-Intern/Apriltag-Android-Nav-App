package com.example.apriltagapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MyRenderer(val view: CameraView): GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private lateinit var triangle: Triangle
    companion object {
        fun loadShader(type: Int, shaderCode: String): Int{
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)

            return shader
        }

        const val MY_PERMISSIONS_REQUEST_CAMERA = 77
        const val CAMERA_BACK = "0"
        const val CAMERA_FRONT = "1"
        const val IMAGE_BUFFER_SIZE = 1

        init {
            System.loadLibrary("apriltag")
        }
    }

    private lateinit var cameraDevice: CameraDevice
    private lateinit var backgroundThread: HandlerThread
    private lateinit var backgroundHandler: Handler
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var imageReader: ImageReader
    private lateinit var texture: SurfaceTexture
    private lateinit var hTex: IntArray

    private val cameraManager by lazy {
        view.context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private val deviceStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            println("카메라 시작")
            previewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            println("카메라 종료")
        }

        override fun onError(camera: CameraDevice, error: Int) {
            println("카메라 오류")
        }

    }

    private fun previewSession() {
        texture.setDefaultBufferSize(1280, 720)
        val surface = Surface(texture)
        imageReader = ImageReader.newInstance(1280, 720, ImageFormat.YUV_420_888,
            MainActivity.IMAGE_BUFFER_SIZE
        )
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireNextImage()
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining()).apply { buffer.get(this) }
            buffer.clear()


            val results = ApriltagNative.apriltag_detect_yuv(bytes, 1280, 720)
            for (result in results) {
                println("태그 ID : ${result.id}")
            }
            image.close()

        }, backgroundHandler)
        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(surface)
        cameraDevice.createCaptureSession(
            listOf(surface, imageReader.surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    println("session 생성 실패")
                }

                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    captureRequestBuilder.set(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                    )
                    captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null)
                }
            },
            null
        )
    }

    private fun closeCamera() {
        captureSession.close()
        cameraDevice.close()
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("Camera2").apply { start() }
        backgroundHandler = Handler(backgroundThread.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread.quitSafely()
        try {
            backgroundThread.join()
        } catch (e: Exception) {
            println("background 오류")
        }

    }

    private fun captureStillImage() {
        cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureRequestBuilder.addTarget(imageReader.surface)
        captureSession.capture(
            captureRequestBuilder.build(),
            object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)
                }
            },
            null
        )
    }

    private fun <T> cameraCharactersitics(cameraId: String, key: CameraCharacteristics.Key<T>): T {
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        return when (key) {
            CameraCharacteristics.LENS_FACING -> characteristics.get(key)!!
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP -> characteristics.get(key)!!
            else -> throw IllegalArgumentException("Key not recognized")
        }
    }

    private fun cameraId(lens: Int): String {
        var deviceId = listOf<String>()
        try {
            val cameraIdList = cameraManager.cameraIdList
            deviceId = cameraIdList.filter {
                lens == cameraCharactersitics(
                    it,
                    CameraCharacteristics.LENS_FACING
                )
            }
        } catch (e: CameraAccessException) {
            println("Camera Access Excection : $e")
        }
        return deviceId[0]
    }

    private fun connectCamera() {
        val deviceId = cameraId(CameraCharacteristics.LENS_FACING_BACK)
        try {
            if (ActivityCompat.checkSelfPermission(
                    view.context,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) return
            cameraManager.openCamera(deviceId, deviceStateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.d("MYLOG", "카메라 접근 권한이 없습니다.")
        } catch (e: InterruptedException) {
            Log.e("MYLOG", "interrupt 오류")
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                view.context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("카메라 권한 필요")
//            ActivityCompat.requestPermissions(
//                view.context,
//                arrayOf(Manifest.permission.CAMERA),
//                MainActivity.MY_PERMISSIONS_REQUEST_CAMERA
//            )
        } else {
            connectCamera()
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        startBackgroundThread()
        initTex()
        //GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
        triangle = Triangle()
        texture = SurfaceTexture(hTex[0])
        texture.setOnFrameAvailableListener(this)

        checkCameraPermission()

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        //triangle.draw()
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        view.requestRender()
        captureStillImage()
    }

    private fun initTex() {
        hTex = IntArray(1)
        GLES20.glGenTextures(1, hTex, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0])
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_NEAREST
        )
    }

    fun onResume() {
        startBackgroundThread()
    }

    fun onPause() {
        closeCamera()
        stopBackgroundThread()
    }



}