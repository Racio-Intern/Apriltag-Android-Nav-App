package com.example.apriltagapp.model.baseModel

data class Pos(
    /** 좌표계 차원 */
    val dimension: Int,

    /** 정점 개수 */
    val nPoints: Int,

    /** 각 정점들의 homogeneous coordinates*/
    val points: FloatArray,
)