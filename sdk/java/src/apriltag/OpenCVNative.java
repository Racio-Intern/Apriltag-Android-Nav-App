package apriltag;

public class OpenCVNative {

    public static native void draw_polylines_on_apriltag(long matAddrInput, double[] arr, double[] arrowRight);

    public static native void draw_polylines(long matAddrInput, double[] arr);

    public static native void put_text(long matAddrInput, long matAddrOutput, int[] arr);
}
