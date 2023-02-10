package com.example.apriltagapp.listener

import apriltag.ApriltagDetection

interface TagDetectionListener {
    fun onTagDetect(detection: ApriltagDetection)
}