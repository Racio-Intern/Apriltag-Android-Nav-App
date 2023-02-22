package com.example.apriltagapp.view.sensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.apriltagapp.R
import com.example.apriltagapp.databinding.FragmentSensorBinding

class SensorFragment : Fragment(), SensorEventListener {
    private val LOGTAG = "SensorFragment"
    var binding: FragmentSensorBinding? = null

    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Create a constant to convert nanoseconds to seconds.
    private val NS2S = 1.0f / 1000000000.0f
    private var timestamp: Double = 0.0

    private var mSteps: Int = 0
    private var mCounterSteps: Int = 0

    private var mLastAccelerometer = FloatArray(3)
    private var mLastAccelerometerSet: Boolean = false

    // 가속도센서로부터 측정된 각 축에 대한 가속도의 크기
    private var accelX: Float = 0f
    private var accelY: Float = 0f
    private var accelZ: Float = 0f

    // 측정된 각 축에 대한 가속도의 크기를 저장하는 배열
    private val arrayX: FloatArray = FloatArray(30)
    private val arrayY: FloatArray = FloatArray(30)
    private val arrayZ: FloatArray = FloatArray(30)

    // 측정된 각 축에 대한 가속도 크기들의 평균값
    private var averageX: Double = 0.0
    private var averageY: Double = 0.0
    private var averageZ: Double = 0.0

    // 각 축에 대한 가속도 크기가 측정된 횟수
    private var sensorCountX: Int = 0
    private var sensorCountY: Int = 0
    private var sensorCountZ: Int = 0

    private var magnitudeStore: Double = 0.0
    private var delay: Int = MIN_DELAY
    private var mCountStep: Int = 0

    @RequiresApi(Build.VERSION_CODES.Q)
    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
        when(it){
            true -> {

            }
            false -> {
                showDialogForPermission()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showDialogForPermission(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("알림")
        builder.setMessage("기능을 실행하려면 신체활동 퍼미션을 허가하셔야합니다.")
        builder.setCancelable(false)
        builder.setNegativeButton(
            "예"
        ) { _, _ ->
            findNavController().navigate(R.id.action_sensorFragment_to_entryFragment)
        }
        builder.create().show()
    }

    override fun onStart() {
        super.onStart()
        var havePermission = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                havePermission = false
            }
        }
        if (havePermission) {

        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSensorBinding.inflate(inflater)
        return binding?.root
    }

    override fun onResume() {
        super.onResume()

        /*
        // 가속도계와 지가계 센서 가져오기
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        */

        // 중력을 뺀 가속도 값 가져오기
        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)?.also {linearAcceleration ->
            sensorManager.registerListener(
                this,
                linearAcceleration,
                SensorManager.SENSOR_DELAY_FASTEST,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        // STEP COUNTER 가져오기
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_FASTEST,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        super.onPause()
        // Don't receive any more updates from either sensor.
        // sensor를 쓰는 것은 큰 비용이기에 마지막에 해제 꼭 하기
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.size)
            mLastAccelerometerSet = true
            accelX = Math.abs(event.values[0]) // Reading Acceleration in X-axis
            accelY = Math.abs(event.values[1]) // Reading Acceleration in Y-axis
            accelZ = Math.abs(event.values[2]) // Reading Acceleration in Z-axis

            // 가속도의 크기가 0에 가깝다면 반영 안함
            if (accelX < 0.1f)
                accelX = -1f
            if (accelY < 0.1f)
                accelY = -1f
            if (accelZ < 0.1f)
                accelZ = -1f

        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_STEP_COUNTER){
            if (mCounterSteps < 1){
                mCounterSteps =  event.values[0].toInt()
            }
            mSteps = event.values[0].toInt() - mCounterSteps
            binding?.stepCountTxt?.text = "step_count : ${mSteps}"
        }

        adjustValues()
        calculateStepCount()
        //updateOrientationAngles()
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        // "mRotationMatrix" now has up-to-date information.
        // 방위각(z축을 중심으로 한 각도), 경사(x축을 중심으로 한 각도), 롤(y축을 중심으로 한 각도)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        Log.d(LOGTAG, "orientationA : ${orientationAngles.toList()}")
        // "mOrientationAngles" now has up-to-date information.
    }

    fun adjustValues() {
        if (accelX in 0f .. 11f) {
            arrayX[sensorCountX++] = accelX

            if (sensorCountX == 30) {
                var sumX = 0.0
                for(i in 5 .. 29){
                    sumX += arrayX[i]
                }

                averageX = sumX / 25.0
                sensorCountX = 0
            }
        }

        if (accelY in 0f .. 11f) {
            arrayY[sensorCountY++] = accelY

            if (sensorCountY == 30) {
                var sumY = 0.0
                for(i in 5 .. 29){
                    sumY += arrayY[i]
                }

                averageY = sumY / 25.0
                sensorCountY = 0
            }
        }

        if (accelZ in 0f .. 11f) {
            arrayZ[sensorCountZ++] = accelZ

            if (sensorCountZ == 30) {
                var sumZ = 0.0
                for(i in 5 .. 29){
                    sumZ += arrayZ[i]
                }

                averageZ = sumZ / 25.0
                sensorCountZ = 0
            }
        }
    }

    fun calculateStepCount() {
        // 가속도의 크기
        val magnitude = Math.pow((averageX * averageX) + (averageY * averageY) + (averageZ * averageZ), 0.5)

        val dM = magnitude - magnitudeStore     // delta Magnitude
        magnitudeStore = magnitude
        delay -= 1

        if (dM > STEP_DETECT_THRESHOLD && delay < 0) {
            mCountStep += 1
            delay = 200
            binding?.myStepCountTxt?.text = "나의 걸음걸이 : ${mCountStep}"
        }
    }

    /*
    변경할 수 있는 점
    1. x, y, z축 모두에 대해서 보는게 아니라 대체로 3개의 축 중에서 가장 큰 변화를
    가지는 한 축을 결정하고 이를 토대로 패턴을 뽑아낸다. 이때 평균적인 패턴에서 크게 벗어난
    데이터(인사를 할 때에 손을 흔드는 행위 등)는 필터링 한다.

    2. 필터링을 위한 알고리즘으로 가속도 센서가 연속적으로 10번 움직여야만 10이 카운팅이 되고,
    그 외의 데이터는 무시하는 게 있겠다.

    현재 문제점
    1. 한 번 움직이고 비슷한 속도로 걸을 때 걸음 수를 잘 찾지 못한다.
     */

    companion object {
        /**
         * 사람이 한번 걷기 위해 필요한 최소 delay 횟수
         */
        private val MIN_DELAY = 200

        /**
         * 다음 발을 내딛기 위해 몸을 앞으로 끌어당길 때에 가속도 크기의 임계치
         */
        private val STEP_DETECT_THRESHOLD = 0.1
    }
}
