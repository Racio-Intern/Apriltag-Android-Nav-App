package com.example.apriltagapp.listener

import apriltag.ApriltagDetection

interface TagDetectionListener {
    fun onTagDetect(aprilDetection: ArrayList<ApriltagDetection>)
}