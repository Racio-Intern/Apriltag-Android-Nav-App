#include "opencv2/opencv.hpp"
#include <jni.h>
#include <android/log.h>
#include <camera_params.h>

#define SQUARE_LENGTH 8.4f

using namespace cv;
using namespace std;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_apriltagapp_view_camera_CameraFragment_ConvertRGBtoGray(JNIEnv *env, jobject thiz,
                                                                         jlong mat_addr_input,
                                                                         jlong mat_addr_result) {
    Mat &matInput = *(Mat *) mat_addr_input;
    Mat &matResult = *(Mat *) mat_addr_result;

    cvtColor(matInput, matResult, COLOR_RGBA2GRAY);
}

/*
 * Class:     Java_apriltag_OpenCVNative
 * Method:    draw_polylines
 * openCV polyline을 그립니다.
 */
extern "C"
JNIEXPORT void JNICALL
Java_apriltag_OpenCVNative_draw_1polylines(JNIEnv *env, jclass clazz, jlong mat_addr_input,
                                           jdoubleArray arr) {
    Mat &matInput = *(Mat *) mat_addr_input;
    jdouble *jni_arr = (*env).GetDoubleArrayElements(arr, NULL);
    int len = (*env).GetArrayLength(arr);

    // polyline을 그립니다.
    vector<Point2i> vector_pts;
    for (int i = 0; i < len; i++) {
        vector_pts.emplace_back(jni_arr[2 * i], jni_arr[2 * i + 1]);
    }
    polylines(matInput, vector_pts, true, Scalar(255.0, 0.0, 0.0), 20);

    (*env).ReleaseDoubleArrayElements(arr, jni_arr, 0);
}

/*
 * Class:     Java_apriltag_OpenCVNative
 * Method:    draw_polylines_on_apriltag
 * Signature: cv::solvePnP, cv::projectPoints
 * apriltag 위에 openCV polyline을 그립니다.
 */
extern "C"
JNIEXPORT void JNICALL
Java_apriltag_OpenCVNative_draw_1polylines_1on_1apriltag(JNIEnv *env, jclass clazz,
                                                         jlong mat_addr_input,
                                                         jdoubleArray arr,
                                                         jdoubleArray drawArray) {
    Mat &matInput = *(Mat *) mat_addr_input;
    jdouble *jni_arr = (*env).GetDoubleArrayElements(arr, NULL);
    jdouble *jni_draw_array = (*env).GetDoubleArrayElements(drawArray, NULL);
    int len = (*env).GetArrayLength(drawArray);

    Mat cameraM = Mat(3, 3, CV_64FC1, camera_matrix_data);
    Mat distortionC = Mat::zeros(5, 1, CV_64FC1); // 왜곡 계수

    vector<Point2f> imagePoint;
    imagePoint.emplace_back((float) jni_arr[0], (float) jni_arr[1]);
    imagePoint.emplace_back((float) jni_arr[2], (float) jni_arr[3]);
    imagePoint.emplace_back((float) jni_arr[4], (float) jni_arr[5]);
    imagePoint.emplace_back((float) jni_arr[6], (float) jni_arr[7]);

    vector<Point3f> objectPoint;
    objectPoint.emplace_back(-SQUARE_LENGTH / 2, SQUARE_LENGTH / 2, 0);
    objectPoint.emplace_back(SQUARE_LENGTH / 2, SQUARE_LENGTH / 2, 0);
    objectPoint.emplace_back(SQUARE_LENGTH / 2, -SQUARE_LENGTH / 2, 0);
    objectPoint.emplace_back(-SQUARE_LENGTH / 2, -SQUARE_LENGTH / 2, 0);

    // 카메라 rotation과 translation 벡터 찾기
    // @ref SOLVEPNP_IPPE_SQUARE this is a special case suitable for marker pose estimation.
    Mat rvecs, tvecs; // 카메라 rotation, translation
    solvePnP(objectPoint, imagePoint, cameraM, distortionC, rvecs, tvecs, false,
             SOLVEPNP_IPPE_SQUARE);

    // 3D 포인터를 이미지 평면에 투영
    vector<cv::Point3f> obj_pts;
    for (int i = 0; i < len / 3; i++) {
        obj_pts.emplace_back((float) jni_draw_array[3 * i], (float) jni_draw_array[3 * i + 1],
                             (float) jni_draw_array[3 * i + 2]);
    }
    projectPoints(obj_pts, rvecs, tvecs, cameraM, distortionC, imagePoint);

    vector<Point2i> vector_pts;
    // 이미지 평면에 투영 시킨 점들을 가지고 polyline을 그립니다.
    for (Point_<float> &i: imagePoint) {
        vector_pts.push_back(i);
    }
    polylines(matInput, vector_pts, true, Scalar(255.0, 0.0, 0.0), 2);

    (*env).ReleaseDoubleArrayElements(arr, jni_arr, 0);
    (*env).ReleaseDoubleArrayElements(drawArray, jni_draw_array, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_apriltag_OpenCVNative_put_1text(JNIEnv *env, jclass clazz, jlong mat_addr_input,
                                     jlong mat_addr_output, jintArray arr) {
    Mat &matInput = *(Mat *) mat_addr_input;
    Mat &matResult = *(Mat *) mat_addr_output;
    jint *jni_arr = (*env).GetIntArrayElements(arr, NULL);

    string text = "Hello, apriltag";
    rotate(matInput, matResult, ROTATE_90_CLOCKWISE);
    putText(matResult, text, Point(jni_arr[0], jni_arr[1]), FONT_HERSHEY_COMPLEX, 1,
            Scalar(0.0, 0.0, 255.0), 3);
    rotate(matResult, matInput, ROTATE_90_COUNTERCLOCKWISE);

    (*env).ReleaseIntArrayElements(arr, jni_arr, 0);
}


extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_apriltag_OpenCVNative_apriltag_1detect_1and_1pos_1estimate(JNIEnv *env, jclass clazz,
                                                                jlong mat_addr_input,
                                                                jdoubleArray arr,
                                                                jdoubleArray camera_matrix_data) {

    Mat &matInput = *(Mat *) mat_addr_input;
    jdouble *jni_arr = (*env).GetDoubleArrayElements(arr, NULL);
    jdouble *jni_cameraM_data = (*env).GetDoubleArrayElements(camera_matrix_data, NULL);

    Mat cameraM = Mat(3, 3, CV_64FC1, jni_cameraM_data);
    Mat distortionC = Mat::zeros(5, 1, CV_64FC1); // 왜곡 계수

    vector<Point2f> imagePoint;
    imagePoint.emplace_back((float) jni_arr[0], (float) jni_arr[1]);
    imagePoint.emplace_back((float) jni_arr[2], (float) jni_arr[3]);
    imagePoint.emplace_back((float) jni_arr[4], (float) jni_arr[5]);
    imagePoint.emplace_back((float) jni_arr[6], (float) jni_arr[7]);

    vector<Point3f> objectPoint;
    objectPoint.emplace_back(-SQUARE_LENGTH / 2, SQUARE_LENGTH / 2, 0);
    objectPoint.emplace_back(SQUARE_LENGTH / 2, SQUARE_LENGTH / 2, 0);
    objectPoint.emplace_back(SQUARE_LENGTH / 2, -SQUARE_LENGTH / 2, 0);
    objectPoint.emplace_back(-SQUARE_LENGTH / 2, -SQUARE_LENGTH / 2, 0);

    // 카메라 rotation과 translation 벡터 찾기
    // @ref SOLVEPNP_IPPE_SQUARE this is a special case suitable for marker pose estimation.
    Mat rvecs, tvecs; // 카메라 rotation, translation
    solvePnP(objectPoint, imagePoint,cameraM, distortionC, rvecs, tvecs, false, SOLVEPNP_IPPE_SQUARE);

    // 3D 포인터를 이미지 평면에 투영
    vector<cv::Point3f> obj_pts;
    obj_pts.emplace_back(-SQUARE_LENGTH / 2, SQUARE_LENGTH / 2, 0);
    obj_pts.emplace_back(SQUARE_LENGTH / 2, SQUARE_LENGTH / 2, 0);
    obj_pts.emplace_back(SQUARE_LENGTH / 2, -SQUARE_LENGTH / 2, 0);
    obj_pts.emplace_back(-SQUARE_LENGTH / 2, -SQUARE_LENGTH / 2, 0);

    projectPoints(obj_pts, rvecs, tvecs, cameraM, distortionC, imagePoint);

    vector<Point2i> vector_pts;
    // 이미지 평면에 투영 시킨 점들을 가지고 polyline을 그립니다.
    for (Point_<float> &i: imagePoint) {
        vector_pts.push_back(i);
    }
    polylines(matInput, vector_pts, true, Scalar(255.0, 0.0, 0.0), 2);

    (*env).ReleaseDoubleArrayElements(arr, jni_arr, 0);
    (*env).ReleaseDoubleArrayElements(camera_matrix_data, jni_cameraM_data, 0);

    Mat rt;
    Rodrigues(rvecs, rt);
    Mat r = rt.inv();
    Mat pos = -r * tvecs;


    double *p = (double *) pos.data;
    __android_log_print(ANDROID_LOG_INFO, "apriltag_jni",
                        "%f %f %f", p[0], p[1], p[2]);

    jdoubleArray mat_arr = (*env).NewDoubleArray(3);

    (*env).SetDoubleArrayRegion(mat_arr, 0, 3, p);

    return mat_arr;
}

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_apriltag_OpenCVNative_calibrateCamera(JNIEnv *env, jclass clazz, jlong mat_addr_input,
                                           jdoubleArray arr,
                                           jintArray image_size) {
    Mat &matInput = *(Mat *) mat_addr_input;
    jdouble *jni_arr = (*env).GetDoubleArrayElements(arr, NULL);
    jint *image_arr = (*env).GetIntArrayElements(image_size, NULL);

    // Creating vector to store vectors of 3D points for each apriltag image
    std::vector<std::vector<cv::Point3f> > objpoints;

    // Creating vector to store vectors of 2D points for each apriltag image
    std::vector<std::vector<cv::Point2f> > imgpoints;

    vector<Point2f> imagePoint;
    imagePoint.emplace_back((float) jni_arr[0], (float) jni_arr[1]);
    imagePoint.emplace_back((float) jni_arr[2], (float) jni_arr[3]);
    imagePoint.emplace_back((float) jni_arr[4], (float) jni_arr[5]);
    imagePoint.emplace_back((float) jni_arr[6], (float) jni_arr[7]);

    vector<Point3f> objectPoint;
    objectPoint.emplace_back(-SQUARE_LENGTH / 2, SQUARE_LENGTH / 2, 0);
    objectPoint.emplace_back(SQUARE_LENGTH / 2, SQUARE_LENGTH / 2, 0);
    objectPoint.emplace_back(SQUARE_LENGTH / 2, -SQUARE_LENGTH / 2, 0);
    objectPoint.emplace_back(-SQUARE_LENGTH / 2, -SQUARE_LENGTH / 2, 0);

    objpoints.push_back(objectPoint);
    imgpoints.push_back(imagePoint);

    Mat cameraM, rvecs, tvecs;
    Mat distortionC = Mat::zeros(5, 1, CV_64FC1); // 왜곡 계수
    calibrateCamera(objpoints, imgpoints, cv::Size(image_arr[0], image_arr[1]), cameraM, distortionC, rvecs, tvecs);
    //cameraM = initCameraMatrix2D(objpoints, imgpoints,cv::Size(image_arr[0], image_arr[1]));

    __android_log_print(ANDROID_LOG_INFO, "apriltag_jni_matrix",
                        "%f %f %f %f", cameraM.at<double>(0,0), cameraM.at<double>(0,2), cameraM.at<double>(1,1), cameraM.at<double>(1,2) );

    solvePnP(objectPoint, imagePoint, cameraM, distortionC, rvecs, tvecs, false,
             SOLVEPNP_IPPE_SQUARE);

    projectPoints(objectPoint, rvecs, tvecs, cameraM, distortionC, imagePoint);

    vector<Point2i> vector_pts;
    // 이미지 평면에 투영 시킨 점들을 가지고 polyline을 그립니다.
    for (Point_<float> &i: imagePoint) {
        vector_pts.push_back(i);
    }

    polylines(matInput, vector_pts, true, Scalar(255.0, 0.0, 0.0), 2);

    Mat rt;
    Rodrigues(rvecs, rt);
    Mat r = rt.inv();
    Mat pos = -r * tvecs;

    double *p = (double *) pos.data;
    __android_log_print(ANDROID_LOG_INFO, "apriltag_jni_pos",
                        "%f %f %f", p[0], p[1], p[2]);

    jdoubleArray mat_arr = (*env).NewDoubleArray(3);

    (*env).SetDoubleArrayRegion(mat_arr, 0, 3, p);

    (*env).ReleaseDoubleArrayElements(arr, jni_arr, 0);
    (*env).ReleaseIntArrayElements(image_size, image_arr, 0);
    return mat_arr;
}
extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_apriltag_OpenCVNative_find_1camera_1focal_1length(JNIEnv *env, jclass clazz, jdoubleArray arr,
                                                       jintArray image_size) {
    jdouble *jni_arr = (*env).GetDoubleArrayElements(arr, NULL);
    jint *image_arr = (*env).GetIntArrayElements(image_size, NULL);

    // Creating vector to store vectors of 3D points for each apriltag image
    std::vector<std::vector<cv::Point3f> > objpoints;

    // Creating vector to store vectors of 2D points for each apriltag image
    std::vector<std::vector<cv::Point2f> > imgpoints;

    vector<Point2f> imagePoint;
    imagePoint.emplace_back((float) jni_arr[0], (float) jni_arr[1]);
    imagePoint.emplace_back((float) jni_arr[2], (float) jni_arr[3]);
    imagePoint.emplace_back((float) jni_arr[4], (float) jni_arr[5]);
    imagePoint.emplace_back((float) jni_arr[6], (float) jni_arr[7]);

    vector<Point3f> objectPoint;
    objectPoint.emplace_back(-SQUARE_LENGTH / 2, SQUARE_LENGTH / 2, 0);
    objectPoint.emplace_back(SQUARE_LENGTH / 2, SQUARE_LENGTH / 2, 0);
    objectPoint.emplace_back(SQUARE_LENGTH / 2, -SQUARE_LENGTH / 2, 0);
    objectPoint.emplace_back(-SQUARE_LENGTH / 2, -SQUARE_LENGTH / 2, 0);

    objpoints.push_back(objectPoint);
    imgpoints.push_back(imagePoint);

    Mat cameraM;
    Mat distortionC = Mat::zeros(5, 1, CV_64FC1); // 왜곡 계수
    cameraM = initCameraMatrix2D(objpoints, imgpoints,cv::Size(image_arr[0], image_arr[1]));

    (*env).ReleaseDoubleArrayElements(arr, jni_arr, 0);
    (*env).ReleaseIntArrayElements(image_size, image_arr, 0);

    double focal_center[2] = {cameraM.at<double>(0, 2), cameraM.at<double>(1, 2)};
    jdoubleArray focal_center_arr = (*env).NewDoubleArray(2);

    (*env).SetDoubleArrayRegion(focal_center_arr, 0, 2, focal_center);

    return focal_center_arr;
}