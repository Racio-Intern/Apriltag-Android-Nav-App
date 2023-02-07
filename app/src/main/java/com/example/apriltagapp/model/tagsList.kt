package com.example.apriltagapp.model

/** 그래프의 데이터입니다. 이후에 데이터베이스를 활용해서 가져올 생각입니다. */
val tags_1: ArrayList<Tag> = arrayListOf(
    Tag(1, hashMapOf(Pair<Int, LinkedTag> (2,LinkedTag(2, 1, Direction.RIGHT)))),
    Tag(2, hashMapOf(Pair(1, LinkedTag(1, 1, Direction.LEFT)),Pair(3,LinkedTag(3, 3, Direction.RIGHT)),Pair(4,LinkedTag(4, 7, Direction.BACKWARDS)))),
    Tag(3, hashMapOf(Pair(2, LinkedTag(2, 3, Direction.LEFT)),Pair(4, LinkedTag(4, 6, Direction.RIGHT)))),
    Tag(4, hashMapOf(Pair(2, LinkedTag(2, 7, Direction.LEFT)), Pair(3, LinkedTag(3, 6, Direction.LEFT)))),
)