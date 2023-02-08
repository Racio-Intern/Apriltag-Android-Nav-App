package com.example.apriltagapp.listener

import apriltag.ApriltagDetection

interface DetectionListener {
    fun onTagDetection(detection: ApriltagDetection)
}