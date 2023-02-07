package com.example.apriltagapp.listener

import com.example.apriltagapp.ApriltagDetection1

interface DetectionListener {
    fun onTagDetection(detection: ApriltagDetection1)
}