package com.example.apriltagapp


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Camera
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.lifecycleScope
import com.example.apriltagapp.ApriltagNative.*
import com.example.apriltagapp.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class MainActivity : AppCompatActivity() {
    external fun stringFromJNI(): String
    companion object {
        const val MY_PERMISSIONS_REQUEST_CAMERA = 77
        const val CAMERA_BACK = "0"
        const val CAMERA_FRONT = "1"
        const val IMAGE_BUFFER_SIZE = 1

        init {
            System.loadLibrary("apriltag")

        }
    }
        private lateinit var binding: ActivityMainBinding
        var cameraId = CAMERA_BACK

        private lateinit var cameraDevice: CameraDevice
        private lateinit var backgroundThread: HandlerThread
        private lateinit var backgroundHandler: Handler
        private lateinit var captureSession: CameraCaptureSession
        private lateinit var captureRequestBuilder: CaptureRequest.Builder
        private lateinit var imageReader: ImageReader

        private val cameraManager by lazy {
            getSystemService(Context.CAMERA_SERVICE) as CameraManager
        }

        private val deviceStateCallback = object: CameraDevice.StateCallback() {
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
            val surface = binding.viewSurface.holder.surface
            imageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, IMAGE_BUFFER_SIZE)
            imageReader.setOnImageAvailableListener({ reader->
                val image = reader.acquireNextImage()
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining()).apply { buffer.get(this) }
                val results = apriltag_detect_yuv(bytes, 5664, 4248)
                for(result in results) {
                    println("태그 중심 : $result.c")
                }
                println("함수 끝나고 출력")
                image.close()

            }, backgroundHandler)
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)
            cameraDevice.createCaptureSession(listOf(surface, imageReader.surface), object: CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    println("session 생성 실패")
                }

                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                    captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null)
                }
            }, null)
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
            }catch (e: Exception){
                println("background 오류")
            }

        }

        private fun captureStillImage() {
            cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureRequestBuilder.addTarget(imageReader.surface)
            captureSession.capture(captureRequestBuilder.build(), object: CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)
                    println("capture success")
                }
            }, null)
        }

        private fun <T> cameraCharactersitics(cameraId: String, key: CameraCharacteristics.Key<T>) : T {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            return when(key) {
                CameraCharacteristics.LENS_FACING -> characteristics.get(key)!!
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP -> characteristics.get(key)!!
                else -> throw IllegalArgumentException("Key not recognized")
            }
        }

        private fun cameraId(lens: Int): String {
            var deviceId = listOf<String>()
            try {
                val cameraIdList = cameraManager.cameraIdList
                deviceId = cameraIdList.filter { lens == cameraCharactersitics(it, CameraCharacteristics.LENS_FACING)}
            }catch(e: CameraAccessException) {
                println("Camera Access Excection : $e")
            }
            return deviceId[0]
        }

        private fun connectCamera() {
            val deviceId = cameraId(CameraCharacteristics.LENS_FACING_BACK)
            try {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                )  return
                cameraManager.openCamera(deviceId, deviceStateCallback, backgroundHandler)
            }catch(e: CameraAccessException) {
                Log.d("MYLOG", "카메라 접근 권한이 없습니다.")
            }catch(e: InterruptedException) {
                Log.e("MYLOG", "interrupt 오류")
            }
        }
        private fun checkCameraPermission() {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
            } else {
                connectCamera()
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            binding = ActivityMainBinding.inflate(layoutInflater)
            binding.viewSurface.holder.setFixedSize(1920, 1080)
            super.onCreate(savedInstanceState)

            binding.viewSurface.holder.setFixedSize(1920, 1080)
            setContentView(binding.root)

            binding.sampleText.text = stringFromJNI()

            binding.btnSetting.setOnClickListener {
                captureStillImage()
            }

            native_init()


            if( ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
            }else {
                println("카메라 권한 획득")
            }

            binding.viewSurface.holder.addCallback(object: SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    checkCameraPermission()
                }

                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                }

            })

//            CoroutineScope(Dispatchers.Main).launch {
//                while(true) {
//                    delay(3000)
//                    captureStillImage()
//                }
//            }
        }

        override fun onPause() {
            stopBackgroundThread()
            closeCamera()
            super.onPause()
        }

        override fun onResume() {
            super.onResume()

            startBackgroundThread()

        }
    }