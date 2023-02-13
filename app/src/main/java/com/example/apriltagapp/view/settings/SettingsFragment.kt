package com.example.apriltagapp.view.settings

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import apriltag.ApriltagNative
import com.example.apriltagapp.R
import com.example.apriltagapp.databinding.FragmentSearchBinding
import com.example.apriltagapp.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment() {
    private var binding: FragmentSettingsBinding? = null

    val deci: Array<String> = arrayOf("1", "1.5", "2", "3", "4")
    val threads: Array<String> = arrayOf("0", "2", "4", "6", "8")
    val tagFamilies: Array<String> = arrayOf("tag16h5" ,"tag25h9","tag36h10" ,"tag36h11",  "tagStandard41h12")

    var decimateFactor = 4.0
    var tagFamily = "tagStandard41h12"
    var thread = 1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater)
        // Inflate the layout for this fragment
        binding?.btnDecimation?.setOnClickListener {
            val builder =
                AlertDialog.Builder(this.activity)
            builder.setTitle("Select Station").setItems(
                deci
            ) { _, which ->
                Toast.makeText(
                    this.activity,
                    "",
                    Toast.LENGTH_SHORT
                ).show()
                decimateFactor = deci[which].toDouble()

                ApriltagNative.apriltag_init(tagFamily, 2, decimateFactor, 0.0, thread)
            }.show()


        }

        binding?.btnThreads?.setOnClickListener {
            val builder =
                AlertDialog.Builder(this.activity)
            builder.setTitle("Select Station").setItems(
                threads
            ) { _, which ->
                Toast.makeText(
                    this.activity,
                    "",
                    Toast.LENGTH_SHORT
                ).show()
                thread = threads[which].toInt()
                ApriltagNative.apriltag_init(tagFamily, 2, decimateFactor, 0.0, thread)
            }.show()


        }
        binding?.btnTagFamily?.setOnClickListener {
            val builder =
                AlertDialog.Builder(this.activity)
            builder.setTitle("Select Station").setItems(
                tagFamilies
            ) { _, which ->
                Toast.makeText(
                    this.activity,
                    "",
                    Toast.LENGTH_SHORT
                ).show()
                tagFamily = tagFamilies[which]
                ApriltagNative.apriltag_init(tagFamily, 2, decimateFactor, 0.0, thread)
            }.show()


        }
        return binding?.root
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}