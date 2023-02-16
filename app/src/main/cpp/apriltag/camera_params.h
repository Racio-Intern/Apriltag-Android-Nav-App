//
// Created by HyukSu on 2023-02-15.
//

#ifndef APRILTAGAPP_CAMERA_PARAMS_H
#define APRILTAGAPP_CAMERA_PARAMS_H


/*
    cameraMatrix : (3 x 3)   fx, 0,  cx
                             0,  fy, cy
                             0,  0,  1
*/
static double camera_matrix_data[9] = {1005, 0, 720.0,
                                  0, 1005, 540.0,
                                  0, 0, 1};

#endif APRILTAGAPP_CAMERA_PARAMS_H
