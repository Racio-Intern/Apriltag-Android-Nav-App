package com.example.apriltagapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.apriltagapp.ApriltagNative.*
import com.example.apriltagapp.databinding.ActivityMainBinding
import com.example.apriltagapp.model.Spot
import com.example.apriltagapp.model.TagGraph
import com.example.apriltagapp.model.tags_1
import com.example.apriltagapp.view.camera.MyRenderer

class MainActivity : AppCompatActivity(), OnRequestPermissionsResultCallback{
    external fun stringFromJNI(): String
    lateinit var binding: ActivityMainBinding

    companion object {
        const val IMAGE_BUFFER_SIZE = 1
        const val MY_PERMISSIONS_REQUEST_CAMERA = 1;

        init {
            System.loadLibrary("apriltag")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val navController = binding.frgNav.getFragment<NavHostFragment>().navController
        setupActionBarWithNavController(navController)

        requestCameraPermission()

        setContentView(binding.root)

        native_init()
        apriltag_init("tagStandard41h12", 2, 4.0, 0.0, 1)


        // Spots Initialize
        val spots: Array<Spot> = arrayOf(Spot("항공대", 1000), Spot("현택이네", 1001), Spot("혁수네", 1002), Spot("은기네", 1003))

        // Tag Graph Initialize
        val tagGraph = TagGraph(tags_1)
        for(spot in spots) {
            tagGraph.tags[0].addSpot(spot)
        }
        tagGraph.printGraph()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = binding.frgNav.getFragment<NavHostFragment>().navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("카메라 권한 필요")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                MyRenderer.MY_PERMISSIONS_REQUEST_CAMERA
            )
        }
    }
}



