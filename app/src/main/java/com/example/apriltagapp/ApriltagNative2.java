package com.example.apriltagapp;

import android.graphics.Bitmap;
import java.util.ArrayList;

import apriltag.ApriltagDetection;

/**
 * Interface to native C AprilTag library.
 */

public class ApriltagNative2 {

    public static native void native_init();

    public static native void yuv_to_rgb(byte[] src, int width, int height, Bitmap dst);

    public static native void apriltag_init(String tagFamily, int errorBits, double decimateFactor,
                                            double blurSigma, int nthreads);

    public static native ArrayList<ApriltagDetection1> apriltag_detect_yuv(byte[] src, int width, int height);
}
