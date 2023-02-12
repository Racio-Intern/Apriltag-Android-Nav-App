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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import apriltag.ApriltagDetection
import apriltag.OpenCVNative
import com.example.apriltagapp.R
import com.example.apriltagapp.databinding.FragmentCameraBinding
import com.example.apriltagapp.listener.TagDetectionListener
import com.example.apriltagapp.view.ApriltagCamera2View
import com.example.apriltagapp.view.search.SearchFragment
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat

class CameraFragment : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback,
    CameraBridgeViewBase.CvCameraViewListener2, TagDetectionListener {
    private val viewModel: CameraViewModel by viewModels()
    var binding: FragmentCameraBinding? = null
    private val args: CameraFragmentArgs by navArgs()
    private var aprilDetection: ApriltagDetection? = null
    private var mOpenCvCameraView: ApriltagCamera2View? = null
    private var state: Boolean = true
    private val TAG = "opencv"
    private lateinit var matInput: Mat

    //DEFAULT, LEFT, RIGHT, STRAIT, BACKWARDS;
    init{
        drawArr[0] = ALLOW_DEFAULT
        drawArr[1] = ALLOW_LEFT
        drawArr[2] = ALLOW_RIGHT
        drawArr[3] = ALLOW_FORWARD
        drawArr[4] = ALLOW_BACKWARD
    }

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
            this.setOnDetectionListener(this@CameraFragment)
            this.visibility = SurfaceView.VISIBLE
            this.setCvCameraViewListener(this@CameraFragment)
            this.setCameraIndex(0) // front-camera(1),  back-camera(0)
        }


        //그래프가 만들어지면 전달받은 args를 viewmodel에 넘겨줍니다.
        viewModel.tagGraph.observe(viewLifecycleOwner) {
            if(args.sendingData != SearchFragment.DEFAULT_DESTINATION_ID) {
                viewModel.onSpotsObserved(args.sendingData)
            }
        }

        viewModel.isRunning.observe(viewLifecycleOwner){
            if(it == true){
                state = false
            }
            else{

                state = true
            }
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



    override fun onCameraViewStarted(width: Int, height: Int) {
    }

    override fun onCameraViewStopped() {
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        matInput = inputFrame.rgba()
        aprilDetection = aprilDetection?.let{ detection ->
            OpenCVNative.draw_polylines_on_apriltag(matInput.nativeObjAddr, detection.p, drawArr[viewModel.direction.value?.ordinal ?: 0])
            null
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

    override fun onTagDetect(detection: ApriltagDetection) {
        this.aprilDetection = detection
        if(state) {
            viewModel.onDetect(detection)
        }
    }

    companion object{
        val ALLOW_DEFAULT = arrayOf(
            0.5, 0.5, 0.0,
            0.5, -0.5, 0.0,
            -0.5, -0.5, 0.0,
            -0.5, 0.5, 0.0
        ).toDoubleArray()

        val ALLOW_RIGHT = arrayOf(
            2.0, 1.0, 0.0,
            2.0, 1.5, 0.0,
            3.0, 0.0, 0.0,
            2.0, -1.5, 0.0,
            2.0, -1.0, 0.0,
            -2.0, -1.0, 0.0,
            -2.0, 1.0, 0.0
        ).toDoubleArray()

        val ALLOW_LEFT = arrayOf(
            -2.0, 1.0, 0.0,
            -2.0, 1.5, 0.0,
            -3.0, 0.0, 0.0,
            -2.0, -1.5, 0.0,
            -2.0, -1.0, 0.0,
            2.0, -1.0, 0.0,
            2.0, 1.0, 0.0
        ).toDoubleArray()

        val ALLOW_FORWARD = arrayOf(
            -1.0, 0.0, -5.0,
            1.0, 0.0, -5.0,
            1.5, 0.0, -2.0,
            0.0, 0.0, -0.5,
            -1.5, 0.0, -2.0,
            -1.0, 0.0, -2.0,
            -1.5, 0.0, -5.0
        ).toDoubleArray()

        val ALLOW_BACKWARD = arrayOf(
            -1.2, 0.0, -0.3,
            -1.2, 0.0, -4.3,
            -1.7, 0.0, -4.3,
            0.0, 0.0, -5.3,
            1.7, 0.0, -4.3,
            1.2, 0.0, -4.3,
            1.2, 0.0, -0.3
        ).toDoubleArray()

        var drawArr = Array<DoubleArray>(5) { doubleArrayOf() }
    }
}