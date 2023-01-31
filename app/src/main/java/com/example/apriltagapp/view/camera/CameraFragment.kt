package com.example.apriltagapp.view.camera

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.apriltagapp.databinding.FragmentCameraBinding


class CameraFragment : Fragment() {
    var binding: FragmentCameraBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCameraBinding.inflate(inflater)
        // Inflate the layout for this fragment

        val glSurfaceView = GLSurfaceView(this.context)
        val surface = glSurfaceView.holder.surface
        val renderer = MyRenderer(glSurfaceView)

        return glSurfaceView
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}