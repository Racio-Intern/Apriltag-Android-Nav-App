package com.example.apriltagapp.model

enum class Direction {
    DEFAULT, LEFT, RIGHT, STRAIT, BACKWARDS;
}

data class LinkedTag(
    val id: Int,
    val distance: Int,
    val direction: Direction
)