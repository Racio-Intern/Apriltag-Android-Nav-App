package com.example.apriltagapp.view.camera

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.apriltagapp.databinding.FragmentCameraBinding


class CameraFragment : Fragment(){
    var binding: FragmentCameraBinding? = null
    lateinit var renderer: MyRenderer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        checkCameraPermission()

        binding = FragmentCameraBinding.inflate(inflater)
        // Inflate the layout for this fragment

        val glSurfaceView = GLSurfaceView(this.context)
        val surface = glSurfaceView.holder.surface
        renderer = MyRenderer(glSurfaceView, this)

        return glSurfaceView
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun checkCameraPermission() {
        context?.let { context->
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                println("카메라 권한 필요")
                Toast.makeText(context,"카메라 권한 필요", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        renderer.onDestroy()
    }

}