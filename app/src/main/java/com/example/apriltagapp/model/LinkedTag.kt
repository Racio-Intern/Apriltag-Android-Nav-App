package com.example.apriltagapp.model

enum class Direction {
    LEFT, RIGHT, STRAIT, BACKWARDS;
}

data class LinkedTag(
    val id: Int,
    val distance: Int,
    val direction: Direction
)