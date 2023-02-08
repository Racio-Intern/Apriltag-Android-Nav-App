#include "opencv2/opencv.hpp"
#include <jni.h>

using namespace cv;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_apriltagapp_MainActivity_00024Companion_convertRGBtoGray(JNIEnv *env, jobject thiz,
                                                                          jlong mat_addr_input,
                                                                          jlong mat_addr_result) {
    Mat &matInput = *(Mat *)mat_addr_input;
    Mat &matResult = *(Mat *)mat_addr_result;

    cvtColor(matInput, matResult, COLOR_RGBA2GRAY);
}

// 미래에 개선할 것
extern "C"
JNIEXPORT void JNICALL
Java_com_example_apriltagapp_MainActivity_00024Companion_drawRectangle(JNIEnv *env, jobject thiz,
                                                                       jlong mat_addr_input,
                                                                       jlong mat_addr_result) {
    Mat &matInput = *(Mat *)mat_addr_input;

    jdouble* arr2 = (*env).GetDoubleArrayElements( arr, NULL);
    Point_ <int> pts1[4] = {Point((int)arr2[0], (int)arr2[1]), Point((int)arr2[2], (int)arr2[3]),
                            Point((int)arr2[4], (int)arr2[5]), Point((int)arr2[6], (int)arr2[7])};

    const Point* pts = pts1;
    int npts = 4;

    cv::polylines(matInput, &pts, &npts, 1, true, Scalar(255.0, 0.0, 0.0), 2);

    //line(matInput, Point((int)arr2[0], (int)arr2[1]), Point((int)arr2[2], (int)arr2[3]), Scalar(255.0, 0.0, 0.0), 20);
}