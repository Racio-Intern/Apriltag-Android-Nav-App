package com.example.apriltagapp.model

/** 그래프의 데이터입니다. 이후에 데이터베이스를 활용해서 가져올 생각입니다. */
val tags_1: Array<Tag> = arrayOf(
    Tag(1, arrayOf(LinkedTag(2, 1, Direction.RIGHT))),
    Tag(2, arrayOf(LinkedTag(1, 1, Direction.LEFT),LinkedTag(3, 3, Direction.RIGHT),LinkedTag(4, 7, Direction.BACKWARDS))),
    Tag(3, arrayOf(LinkedTag(2, 3, Direction.BACKWARDS),LinkedTag(4, 6, Direction.RIGHT))),
    Tag(4, arrayOf(LinkedTag(2, 7, Direction.BACKWARDS), LinkedTag(3, 6, Direction.LEFT))),

)