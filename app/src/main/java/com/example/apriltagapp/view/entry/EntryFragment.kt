package com.example.apriltagapp.view.entry

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.apriltagapp.R
import com.example.apriltagapp.databinding.FragmentEntryBinding
import com.example.apriltagapp.view.camera.MyRenderer


class EntryFragment : Fragment() {
    var binding: FragmentEntryBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEntryBinding.inflate(inflater)
        // Inflate the layout for this fragment
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.btnCamera?.setOnClickListener {
            findNavController().navigate(R.id.action_entryFragment_to_cameraFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}