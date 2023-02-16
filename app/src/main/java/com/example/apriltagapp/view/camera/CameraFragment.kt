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
import androidx.activity.result.contract.ActivityResultContracts
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
import kotlin.time.ExperimentalTime
import kotlin.time.TimedValue
import kotlin.time.measureTimedValue

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
    private lateinit var matResult: Mat
    private lateinit var cameraMatrixData: DoubleArray

    private var estPosMatrix = doubleArrayOf()

    private val permissionList = Manifest.permission.CAMERA

    private  val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) {
        when(it) {
            true -> {
                onCameraPermissionGranted()
            }
            false -> {
                showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.")
            }
        }
    }

    //DEFAULT, LEFT, RIGHT, STRAIT, BACKWARDS;
    init{
        coordnateArray[0] = defaultCoords
        coordnateArray[1] = arrowLeftCoords
        coordnateArray[2] = arrowRightCoords
        coordnateArray[3] = arrowForwardCoords
        coordnateArray[4] = arrowBackwardCoords
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
            state = it != true
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

    override fun onCameraViewStarted(width: Int, height: Int, focalLength: Double) {
        println("in fragment-> width : $width, height: $height, focalLength : $focalLength")
        cameraMatrixData = doubleArrayOf(
            focalLength, 0.0, width / 2.0,
            0.0, focalLength, height / 2.0,
            0.0, 0.0, 1.0
        )
    }

    override fun onCameraViewStopped() {
    }

    @OptIn(ExperimentalTime::class)
    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        matInput = inputFrame.rgba()

        aprilDetection?.let{ detection ->
            //matResult = Mat(matInput.cols(), matInput.rows(), matInput.type())
            //OpenCVNative.draw_polylines_on_apriltag(matInput.nativeObjAddr, detection.p, coordnateArray[viewModel.direction.ordinal])
            //OpenCVNative.put_text(matInput.nativeObjAddr, matResult.nativeObjAddr, intArrayOf(matInput.rows()/4, matInput.cols() * 3 / 4))
            estPosMatrix = OpenCVNative.apriltag_detect_and_pos_estimate(matInput.nativeObjAddr, detection.p, cameraMatrixData) // rx, ry, rz, tx, ty, tz
            aprilDetection = null
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

//    private val CAMERA_PERMISSION_REQUEST_CODE = 200


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
                requestPermission.launch(permissionList)
                havePermission = false
            }
        }
        if (havePermission) {
            onCameraPermissionGranted()
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun showDialogForPermission(msg: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("알림")
        builder.setMessage(msg)
        builder.setCancelable(false)
        builder.setPositiveButton(
            "예"
        ) { _, _ ->
            requestPermission.launch(
                Manifest.permission.CAMERA)
        }
        builder.setNegativeButton(
            "아니오"
        ) { _, _ ->
            findNavController().navigate(R.id.action_cameraFragment_to_entryFragment)
        }
        builder.create().show()

    }

    override fun onTagDetect(aprilDetection: ApriltagDetection) {
        this.aprilDetection = aprilDetection
        if(state) {
            viewModel.onDetect(aprilDetection)
        }
    }

    companion object{
        // points(x, y, z)
        val defaultCoords = arrayOf(
            0.5, 0.5, 0.0,
            0.5, -0.5, 0.0,
            -0.5, -0.5, 0.0,
            -0.5, 0.5, 0.0
        ).toDoubleArray()

        val arrowRightCoords = arrayOf(
            2.0, 1.0, 0.0,
            2.0, 1.5, 0.0,
            3.0, 0.0, 0.0,
            2.0, -1.5, 0.0,
            2.0, -1.0, 0.0,
            -2.0, -1.0, 0.0,
            -2.0, 1.0, 0.0
        ).toDoubleArray()

        val arrowLeftCoords = arrayOf(
            -2.0, 1.0, 0.0,
            -2.0, 1.5, 0.0,
            -3.0, 0.0, 0.0,
            -2.0, -1.5, 0.0,
            -2.0, -1.0, 0.0,
            2.0, -1.0, 0.0,
            2.0, 1.0, 0.0
        ).toDoubleArray()

        val arrowForwardCoords = arrayOf(
            1.0, 0.0, -5.0,
            1.0, 0.0, -2.0,
            1.5, 0.0, -2.0,
            0.0, 0.0, -0.5,
            -1.5, 0.0, -2.0,
            -1.0, 0.0, -2.0,
            -1.0, 0.0, -5.0
        ).toDoubleArray()

        val arrowBackwardCoords = arrayOf(
            -1.2, 0.0, -0.3,
            -1.2, 0.0, -4.3,
            -1.7, 0.0, -4.3,
            0.0, 0.0, -5.3,
            1.7, 0.0, -4.3,
            1.2, 0.0, -4.3,
            1.2, 0.0, -0.3
        ).toDoubleArray()

        var coordnateArray = Array<DoubleArray>(5) { doubleArrayOf() }
    }
}