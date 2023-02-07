package apriltag;

import android.graphics.Bitmap;
import java.util.ArrayList;

/**
 * Interface to native C AprilTag library.
 */

public class ApriltagNative {

    public static native void native_init_new();

    public static native void yuv_to_rgb(byte[] src, int width, int height, Bitmap dst);

    public static native void apriltag_init_new(String tagFamily, int errorBits, double decimateFactor,
                                            double blurSigma, int nthreads);

    public static native ArrayList<ApriltagDetection> apriltag_detect_yuv_new(byte[] src, int width, int height);
}
