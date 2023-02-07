package com.example.apriltagapp.view.camera

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
import android.opengl.Matrix
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import com.example.apriltagapp.*
import com.example.apriltagapp.listener.DetectionListener
import com.example.apriltagapp.model.baseModel.BaseShape
import com.example.apriltagapp.model.baseModel.Drawing
import com.example.apriltagapp.model.baseShape.Line
import com.example.apriltagapp.model.baseShape.ThreeDimentionLine
import com.example.apriltagapp.model.baseShape.Triangle
import java.util.*
import java.util.concurrent.Semaphore
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MyRenderer(val view: GLSurfaceView, val fragment: CameraFragment, val detectListener: DetectionListener) : GLSurfaceView.Renderer,
    SurfaceTexture.OnFrameAvailableListener, OnRequestPermissionsResultCallback {
    private lateinit var cameraTexture: CameraTexture
    private lateinit var line: Line
    private lateinit var triangle: Triangle
    private lateinit var threeDimentionLine: ThreeDimentionLine
    private lateinit var surface: Surface
    val mPreviewSize: Size = Size(1280, 720)
    var drawList: ArrayList<Drawing> = arrayListOf()
    var state: Boolean = false


    private lateinit var texture: SurfaceTexture
    private lateinit var hTex: IntArray
    private var updateState = false

    // Projection * View * Model matrix
    private var modelMatrix = FloatArray(16)
    private var viewMatrix = FloatArray(16)
    private var projectionMatrix = FloatArray(16)
    private var PVM = FloatArray(16)


    init {
        view.setEGLContextClientVersion(2)
        view.setRenderer(this)
        view.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    companion object {
        fun loadShader(type: Int, shaderCode: String): Int {
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)

            return shader
        }

        const val MY_PERMISSIONS_REQUEST_CAMERA = 77
        const val CAMERA_BACK = "0"
        const val CAMERA_FRONT = "1"
        const val IMAGE_BUFFER_SIZE = 1
    }


    private var isSurfaceCreated = false

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        isSurfaceCreated = true
        initTex()
        texture = SurfaceTexture(hTex[0])
        texture.setOnFrameAvailableListener(this)

        // Set the background frame color
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)

        cameraTexture = CameraTexture(hTex[0])
        line = Line()
        triangle = Triangle()
        threeDimentionLine = ThreeDimentionLine()

        startBackgroundThread()
        checkCameraPermission()
        updateState = true
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        // matrix start~
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.translateM(viewMatrix, 0, width / 2.0f, height / 2.0f, 0.0f)
        Matrix.orthoM(projectionMatrix, 0, 0.0f, width.toFloat(), 0f, height.toFloat(), -1.0f, 1.0f)

        // Update surface dimensions and scale preview to fit the surface
        // Scaling is done to maintain aspect ratio but maximally fill the surface
        // Note: Camera preview size and surface size have width/height swapped
        val preWidth = mPreviewSize.height.toFloat()
        val preHeight = mPreviewSize.width.toFloat()

        val widthRatio = width / preWidth
        val heightRatio = height / preHeight
        val scale_ratio = Math.max(widthRatio, heightRatio)

        val draw_width = (mPreviewSize.width * scale_ratio).toInt()
        val draw_height = (mPreviewSize.height * scale_ratio).toInt()

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.scaleM(modelMatrix, 0, draw_height.toFloat(), draw_width.toFloat(), 1.0f)
    }

    override fun onDrawFrame(gl: GL10?) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        if (updateState) {
            // M * V * P
            Matrix.multiplyMM(PVM, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(PVM, 0, projectionMatrix, 0, PVM, 0)

            // M * V * P
            texture.updateTexImage()
            cameraTexture.draw(PVM)

            if(state) {
                for(list in drawList) {
                    if (list.type == BaseShape.TRIANGLE) {
                        triangle.draw(list.pos.points, list.pos.nPoints, PVM)
                    }
                    else if (list.type == BaseShape.LINE && list.pos.dimension == 2) {
                        line.draw(list.pos.points, list.pos.nPoints, PVM)
                    }
                    else if (list.type == BaseShape.LINE && list.pos.dimension == 3) {
                        threeDimentionLine.draw(list.pos, PVM)
                    }
                }
                state = false
            }
        }
        updateState = false
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        updateState = true
        view.requestRender()
    }

    private fun initTex() {
        hTex = IntArray(1)
        GLES20.glGenTextures(1, hTex, 0)
        println("hTex : ${hTex[0]}")
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
    }


    private var cameraDevice: CameraDevice? = null
    private lateinit var backgroundThread: HandlerThread
    private lateinit var backgroundHandler: Handler
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var imageReader: ImageReader

    private val cameraOpenCloseLock = Semaphore(1)

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
            cameraOpenCloseLock.release()
            camera.close()
            cameraDevice = null
            println("카메라 종료")
        }

        override fun onError(camera: CameraDevice, error: Int) {
            println("카메라 오류")
        }

    }

    private fun previewSession() {

        texture.setDefaultBufferSize(mPreviewSize.width, mPreviewSize.height)
        surface = Surface(texture)

        imageReader = ImageReader.newInstance(
            mPreviewSize.width, mPreviewSize.height, ImageFormat.YUV_420_888,
            MainActivity.IMAGE_BUFFER_SIZE
        )
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireNextImage()
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining()).apply { buffer.get(this) }
            println(bytes.size)
            buffer.clear()

            val mDetections = ApriltagNative2.apriltag_detect_yuv(bytes, mPreviewSize.width, mPreviewSize.height)

            for(detection in mDetections) {
                detectListener.onTagDetection(detection)
                break
            }

            image.close()

        }, backgroundHandler)
        cameraDevice?.let { cameraDevice->

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(imageReader.surface)
            captureRequestBuilder.addTarget(surface)
            try {
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
                            val previewRequest = captureRequestBuilder.build()
                            println("preview request : ${previewRequest}")
                            captureSession.setRepeatingRequest(
                                previewRequest,
                                null,
                                backgroundHandler
                            )
                        }
                    },
                    null
                )
            } catch (e: CameraAccessException) {
                println("session 생성 실패 : $e")
            }
        }


    }

    private fun closeCamera() {
        cameraDevice?.let { cameraDevice->
            captureSession.close()
            cameraDevice.close()
        }
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
        cameraDevice?.let {cameraDevice->
            cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureRequestBuilder.addTarget(imageReader.surface)
            captureSession.capture(
                captureRequestBuilder.build(),
                object : CameraCaptureSession.CaptureCallback() {},
                null
            )
        }
    }

    private fun <T> cameraCharacteristics(cameraId: String, key: CameraCharacteristics.Key<T>): T {
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
                lens == cameraCharacteristics(
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
        } else {
            connectCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        println("requestCode = $requestCode")
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CAMERA -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    println("승인 완료")
                } else {
                    println("승인 안됨")
                }
                return
            }
        }
    }

    fun onDestroy() {
        closeCamera()
        stopBackgroundThread()
    }

    fun onResume() {
        if(isSurfaceCreated ){
            startBackgroundThread()
            checkCameraPermission()
        }
    }

    fun onPause() {
        closeCamera()
        stopBackgroundThread()
    }
}