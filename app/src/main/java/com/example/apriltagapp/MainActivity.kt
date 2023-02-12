package com.example.apriltagapp


import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle

import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import apriltag.ApriltagNative
import com.example.apriltagapp.databinding.ActivityMainBinding
import com.example.apriltagapp.view.ApriltagCamera2View
import org.opencv.android.CameraBridgeViewBase


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding


    companion object {
        init {
            System.loadLibrary("opencv_java4")
            System.loadLibrary("opencv_native_lib")
            System.loadLibrary("apriltag_native_lib")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val navController = binding.frgNav.getFragment<NavHostFragment>().navController
        setupActionBarWithNavController(navController)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        ApriltagNative.apriltag_native_init()
        ApriltagNative.apriltag_init("tagStandard41h12", 2, 4.0, 0.0, 1)

        setContentView(binding.root)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = binding.frgNav.getFragment<NavHostFragment>().navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}



