package apriltag;

public class ApriltagPosEstimation {
    // The decoded ID of the tag
    public int id;

    public double[] rvecs = new double[3]; // 카메라 rotation

    public double[] tvecs = new double[3]; // 카메라 translation

    //
    public double[] relativePos = new double[3];
}