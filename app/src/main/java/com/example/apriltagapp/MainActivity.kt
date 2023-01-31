package com.example.apriltagapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.apriltagapp.ApriltagNative.*
import com.example.apriltagapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(){
    external fun stringFromJNI(): String
    lateinit var binding: ActivityMainBinding

    companion object {
        const val IMAGE_BUFFER_SIZE = 1

        init {
            System.loadLibrary("apriltag")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val navController = binding.frgNav.getFragment<NavHostFragment>().navController
        setupActionBarWithNavController(navController)


        setContentView(binding.root)

        native_init()
        apriltag_init("tagStandard41h12", 2, 4.0, 0.0, 1)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = binding.frgNav.getFragment<NavHostFragment>().navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}



