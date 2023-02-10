#include "opencv2/opencv.hpp"
#include <jni.h>
#include <android/log.h>

using namespace cv;
using namespace std;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_apriltagapp_view_camera_CameraFragment_ConvertRGBtoGray(JNIEnv *env, jobject thiz,
                                                                         jlong mat_addr_input,
                                                                         jlong mat_addr_result) {
    Mat &matInput = *(Mat *)mat_addr_input;
    Mat &matResult = *(Mat *)mat_addr_result;

    cvtColor(matInput, matResult, COLOR_RGBA2GRAY);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_apriltagapp_view_camera_CameraFragment_DrawRectangle(JNIEnv *env, jobject thiz,
                                                                      jlong mat_addr_input,
                                                                      jdoubleArray arr) {
    Mat &matInput = *(Mat *)mat_addr_input;

    jdouble* arr2 = (*env).GetDoubleArrayElements( arr, NULL);
    Point_ <int> pts1[4] = {Point((int)arr2[0], (int)arr2[1]), Point((int)arr2[2], (int)arr2[3]),
                            Point((int)arr2[4], (int)arr2[5]), Point((int)arr2[6], (int)arr2[7])};

    const Point* pts = pts1;
    int npts = 4;

    std::vector<Point2i> vector_pts;
    vector_pts.push_back(Point((int)arr2[0], (int)arr2[1]));
    vector_pts.push_back(Point((int)arr2[2], (int)arr2[3]));
    vector_pts.push_back(Point((int)arr2[4], (int)arr2[5]));
    vector_pts.push_back(Point((int)arr2[6], (int)arr2[7]));

    polylines(matInput, vector_pts, true, Scalar(255.0, 0.0, 0.0), 2);
}

// apriltag 위에 openCV로 화살표 그리는 함수
extern "C"
JNIEXPORT void JNICALL
Java_apriltag_ApriltagDrawNative_DrawArrow(JNIEnv *env, jclass thiz,
                                                               jlong mat_addr_input,
                                                               jdoubleArray arr,
                                                               jdoubleArray drawArray) {
    Mat &matInput = *(Mat *)mat_addr_input;
    jdouble* jni_arr = (*env).GetDoubleArrayElements(arr, NULL);
    jdouble* jni_draw_array = (*env).GetDoubleArrayElements(drawArray, NULL);
    int len = (*env).GetArrayLength(drawArray);

    double data[9] = { 3156.71852, 0, 359.097908,
                       0, 3129.52242, 239.736909,
                       0, 0, 1};
    Mat cameraM = Mat(3, 3, CV_64FC1, data);
    Mat distortionC = Mat::zeros(5, 1, CV_64FC1);

    vector<Point3f> objectPoint;
    vector<Point2f> imagePoint;

    imagePoint.push_back(Point2f((float) jni_arr[0], (float)jni_arr[1]));
    imagePoint.push_back(Point2f((float) jni_arr[2], (float)jni_arr[3]));
    imagePoint.push_back(Point2f((float) jni_arr[4], (float)jni_arr[5]));
    imagePoint.push_back(Point2f((float) jni_arr[6], (float)jni_arr[7]));

//    -   With @ref SOLVEPNP_IPPE_SQUARE this is a special case suitable for marker pose estimation.
//            Number of input points must be 4. Object points must be defined in the following order:
//    - point 0: [-squareLength / 2,  squareLength / 2, 0]
//    - point 1: [ squareLength / 2,  squareLength / 2, 0]
//    - point 2: [ squareLength / 2, -squareLength / 2, 0]
//    - point 3: [-squareLength / 2, -squareLength / 2, 0]

    float squareLength = 1;

    objectPoint.push_back(Point3f(-squareLength/2, squareLength / 2, 0));
    objectPoint.push_back(Point3f(squareLength/2, squareLength / 2, 0));
    objectPoint.push_back(Point3f(squareLength/2, -squareLength / 2, 0));
    objectPoint.push_back(Point3f(-squareLength/2, -squareLength / 2, 0));

    Mat rvecs, tvecs; // 카메라 rotation, translation

    // 카메라 rotation과 translation 벡터 찾기
    solvePnP(objectPoint, imagePoint,cameraM, distortionC, rvecs, tvecs, false, SOLVEPNP_IPPE_SQUARE);
    vector<cv::Point3f> obj_pts;

    for(int i = 0; i < len / 3; i++)
    {
        obj_pts.push_back(Point3f((float)jni_draw_array[3*i], (float)jni_draw_array[3*i+1], (float)jni_draw_array[3*i+2]));
    }

    // 3D 포인터를 이미지 평면에 투영
    projectPoints(obj_pts, rvecs, tvecs, cameraM, distortionC, imagePoint);

    std::vector<Point2i> vector_pts;
    for(Point_<float> & i : imagePoint)
    {
        vector_pts.push_back(i);
    }

    polylines(matInput, vector_pts, true, Scalar(255.0, 0.0, 0.0), 20);

    (*env).ReleaseDoubleArrayElements(arr, jni_arr, 0);
    (*env).ReleaseDoubleArrayElements(drawArray, jni_draw_array, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_apriltagapp_view_camera_CameraFragment_DrawArrow2(JNIEnv *env, jobject thiz,
                                                                  jlong mat_addr_input,
                                                                  jdoubleArray arr,
                                                                  jlong camera_matrix,
                                                                  jlong distortion_coefficients) {
    Mat &matInput = *(Mat *)mat_addr_input;
    jdouble* arr2 = (*env).GetDoubleArrayElements( arr, NULL);
    Mat &cameraM = *(Mat *)camera_matrix;
    Mat &distortionC = *(Mat *)distortion_coefficients;

    vector<Point3f> objectPoint;
    vector<Point2f> imagePoint;


    imagePoint.push_back(Point2f((float) arr2[0], (float)arr2[1]));
    imagePoint.push_back(Point2f((float) arr2[2], (float)arr2[3]));
    imagePoint.push_back(Point2f((float) arr2[4], (float)arr2[5]));
    imagePoint.push_back(Point2f((float) arr2[6], (float)arr2[7]));

    Mat rvecs, tvecs;

    float squareLength = 1;

    objectPoint.push_back(Point3f(-squareLength/2, squareLength / 2, 0));
    objectPoint.push_back(Point3f(squareLength/2, squareLength / 2, 0));
    objectPoint.push_back(Point3f(squareLength/2, -squareLength / 2, 0));
    objectPoint.push_back(Point3f(-squareLength/2, -squareLength / 2, 0));


    solvePnP(objectPoint, imagePoint,cameraM, distortionC, rvecs, tvecs, false, SOLVEPNP_IPPE_SQUARE);
    vector<cv::Point3f> obj_pts;

    obj_pts.emplace_back(-1.2, 0, -0.3);
    obj_pts.emplace_back(-1.2, 0, -4.3);
    obj_pts.emplace_back(-1.7, 0, -4.3);
    obj_pts.emplace_back(0, 0, -5.3);
    obj_pts.emplace_back(1.7, 0, -4.3);
    obj_pts.emplace_back(1.2, 0, -4.3);
    obj_pts.emplace_back(1.2, 0, -0.3);


    projectPoints(obj_pts, rvecs, tvecs, cameraM, distortionC, imagePoint);

    std::vector<Point2i> vector_pts;
    vector_pts.push_back(imagePoint.at(0));
    vector_pts.push_back(imagePoint.at(1));
    vector_pts.push_back(imagePoint.at(2));
    vector_pts.push_back(imagePoint.at(3));
    vector_pts.push_back(imagePoint.at(4));
    vector_pts.push_back(imagePoint.at(5));
    vector_pts.push_back(imagePoint.at(6));
    polylines(matInput, vector_pts, true, Scalar(255.0, 0.0, 0.0), 30);
}