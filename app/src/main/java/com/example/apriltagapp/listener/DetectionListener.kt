package com.example.apriltagapp.listener

import com.example.apriltagapp.ApriltagDetection
import com.example.apriltagapp.model.baseModel.Pos

interface DetectionListener {
    fun onTagDetection(detection: ApriltagDetection)
}