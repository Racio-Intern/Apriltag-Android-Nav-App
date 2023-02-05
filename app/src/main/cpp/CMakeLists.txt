# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("apriltagapp")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        apriltag

        # Sets the library as a shared library.
        SHARED
        # C:\Users\HyukSu\ApriltagApp\app\src\main\cpp\apriltag
        # Provides a relative path to your source file(s).
        apriltag/apriltag.c
        #apriltag/apriltag_jni.c (sample)
        apriltag/apriltag_quad_thresh.c
        apriltag/apriltag_math.h
        #apriltag/apriltag_pose.c (not in sample)
        #apriltag/apriltag_pywrap.c (not in sample)
        apriltag/common/g2d.c
        apriltag/common/getopt.c
        apriltag/common/homography.c
        #apriltag/common/image_f32.c (sample)
        apriltag/common/image_u8.c
        apriltag/common/image_u8x3.c
        apriltag/common/matd.c
        apriltag/common/pnm.c
        apriltag/common/string_util.c
        apriltag/common/svd22.c
        apriltag/common/time_util.c
        apriltag/common/unionfind.c
        apriltag/common/workerpool.c
        apriltag/common/zarray.c
        apriltag/common/zhash.c
        apriltag/common/zmaxheap.c
        apriltag/tag16h5.c
        #apriltag/tag25h7.c (sample)
        apriltag/tag25h9.c
        #apriltag/tag36artoolkit.c # (sample)
        apriltag/tag36h10.c
        apriltag/tag36h11.c
        apriltag/tagCircle21h7.c
        apriltag/tagCircle49h12.c
        apriltag/tagCustom48h12.c
        apriltag/tagStandard41h12.c
        apriltag/tagStandard52h13.c
        native-lib.c)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

include_directories(apriltag/)

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)
find_library(jnigraphics-lib jnigraphics)
# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        apriltag

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib} ${jnigraphics-lib})