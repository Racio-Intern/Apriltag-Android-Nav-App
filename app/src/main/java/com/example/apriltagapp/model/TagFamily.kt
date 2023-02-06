package com.example.apriltagapp.model

class TagFamily(tags: ArrayList<Tag>) {
    val tagMap: Map<Int, Tag> = tags.associateBy { it.id }
}