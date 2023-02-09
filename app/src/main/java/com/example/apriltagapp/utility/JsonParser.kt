package com.example.apriltagapp.utility

import com.example.apriltagapp.model.*
import com.google.firebase.database.DataSnapshot

class JsonParser {
    fun snapshotToTagFamily(snapshot: DataSnapshot): ArrayList<Tag> {
        val tagList = arrayListOf<Tag>()
        for(child in snapshot.children) {
            val linkedTags = child.child("linkedTags").children
            val id = child.child("id").value as Long

            val resultLinkedTags: ArrayList<LinkedTag> = arrayListOf()

            for(linkedTag in linkedTags) {
                val _id = (linkedTag.child("id").value as Long).toInt()
                val _distance = (linkedTag.child("distance").value as Long).toInt()
                val _direction = linkedTag.child("direction").value as String

                resultLinkedTags.add(LinkedTag(_id, _distance, Direction.valueOf(_direction)))
            }
            val resultTag = Tag(id.toInt(), resultLinkedTags)

            tagList.add(resultTag)

        }
        return tagList
    }

    fun snapshotToSpot(snapshot: DataSnapshot): HashMap<String, Int> {
        val spots = hashMapOf<String, Int>()
        for(child in snapshot.children) {
            val tagId = (child.child("tagId").value as Long).toInt()
            val name = child.child("name").value as String
            println("Tag id : $tagId , name : $name")
            spots[name] =  tagId
        }

        return spots
    }
}