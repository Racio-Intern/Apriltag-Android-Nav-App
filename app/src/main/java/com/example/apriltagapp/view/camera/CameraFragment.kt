package com.example.apriltagapp.view.camera


import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import apriltag.ApriltagDetection
import com.example.apriltagapp.R
import com.example.apriltagapp.databinding.FragmentCameraBinding
import com.example.apriltagapp.listener.DetectionListener
import com.example.apriltagapp.listener.TagDetectionListener
import com.example.apriltagapp.view.ApriltagCamera2View
import com.example.apriltagapp.view.CameraCalibrator
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat


class CameraFragment : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback,
    DetectionListener, CameraBridgeViewBase.CvCameraViewListener2, TagDetectionListener {
    private lateinit var cameraCalibrator: CameraCalibrator

    var binding: FragmentCameraBinding? = null

    private var detArray: DoubleArray? = null
    private var state = false

    private var mOpenCvCameraView: ApriltagCamera2View? = null

    private val TAG = "opencv"
    private lateinit var matInput: Mat

    external fun ConvertRGBtoGray(matAddrInput: Long, matAddrResult: Long)
    external fun DrawRectangle(matAddrInput: Long, arr: DoubleArray?)
    external fun Draw3D(matAddrInput: Long, arr: DoubleArray?, cameraMatrix: Long, distortionCoefficients: Long)
    external fun DrawArrow(matAddrInput: Long, arr: DoubleArray?, cameraMatrix: Long, distortionCoefficients: Long)
    external fun DrawArrow2(matAddrInput: Long, arr: DoubleArray?, cameraMatrix: Long, distortionCoefficients: Long)

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(context) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    mOpenCvCameraView?.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCameraBinding.inflate(inflater)

        mOpenCvCameraView = binding?.activitySurfaceView?.apply {
            this.setOnListener(this@CameraFragment)
            this.visibility = SurfaceView.VISIBLE
            this.setCvCameraViewListener(this@CameraFragment)
            this.setCameraIndex(0) // front-camera(1),  back-camera(0)
        }

        return binding?.root
    }


    override fun onPause() {
        super.onPause()
        mOpenCvCameraView?.disableView()
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, context, mLoaderCallback)
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mOpenCvCameraView?.disableView()
        binding = null
    }


    override fun onTagDetection(detection: ApriltagDetection) {
//        viewModel.onDetect(detection, renderer)
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        cameraCalibrator = CameraCalibrator(width, height)
    }

    override fun onCameraViewStopped() {
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        matInput = inputFrame!!.rgba()

         if (state) {
            //DrawRectangle(matInput.nativeObjAddr, detArray)
            //Draw3D(matInput.nativeObjAddr, detArray, cameraCalibrator.cameraMatrix.nativeObjAddr, cameraCalibrator.distortionCoefficients.nativeObjAddr)
             DrawArrow(matInput.nativeObjAddr, detArray, cameraCalibrator.cameraMatrix.nativeObjAddr, cameraCalibrator.distortionCoefficients.nativeObjAddr)
            state = false
        }
        return matInput
    }




    //여기서부턴 퍼미션 관련 메소드
    private fun getCameraViewList(): List<CameraBridgeViewBase>? {

        mOpenCvCameraView?.let {
            return listOf<ApriltagCamera2View>(it)
        }
        return null
    }

    private val CAMERA_PERMISSION_REQUEST_CODE = 200


    private fun onCameraPermissionGranted() {
        val cameraViews = getCameraViewList() ?: return
        for (cameraBridgeViewBase in cameraViews) {
            cameraBridgeViewBase.setCameraPermissionGranted()
        }
    }

    override fun onStart() {
        super.onStart()
        var havePermission = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
                havePermission = false
            }
        }
        if (havePermission) {
            onCameraPermissionGranted()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onCameraPermissionGranted()
        } else {
            showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.")
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun showDialogForPermission(msg: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("알림")
        builder.setMessage(msg)
        builder.setCancelable(false)
        builder.setPositiveButton(
            "예"
        ) { dialog, id ->
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
        builder.setNegativeButton(
            "아니오"
        ) { arg0, arg1 ->
            findNavController().navigate(R.id.action_cameraFragment_to_entryFragment)
        }
        builder.create().show()

    }

    override fun onTagDetect(arr: DoubleArray) {
        detArray = arr
        state = true
    }

}