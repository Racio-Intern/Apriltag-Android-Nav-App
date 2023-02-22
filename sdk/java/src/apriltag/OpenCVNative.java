package apriltag;

public class OpenCVNative {

    public static native void draw_polylines_on_apriltag(long matAddrInput, double[] arr, double[] arrowRight);

    public static native void draw_polylines(long matAddrInput, double[] arr);

    public static native void put_text(long matAddrInput, long matAddrOutput, int[] arr);

    public static native double[] apriltag_detect_and_pos_estimate(long matAddrInput, double[] arr, double[] cameraMatrixData);

    public static native double[] calibrateCamera(long matAddrInput, double[] arr, int[] imageSize);

    public static native double[] find_camera_focal_length(double[] arr, int[] imageSize);
}
