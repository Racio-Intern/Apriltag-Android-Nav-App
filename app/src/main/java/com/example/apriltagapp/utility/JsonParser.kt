package com.example.apriltagapp.utility

import com.example.apriltagapp.model.*
import com.google.firebase.database.DataSnapshot

class JsonParser {
    fun snapshotToTagFamily(snapshot: DataSnapshot): ArrayList<Tag> {
        val tagList = arrayListOf<Tag>()
        for(child in snapshot.children) {
            val spots = child.child("spots").children
            val linkedTags = child.child("linkedTags").children
            val id = child.child("id").value as Long

            val resultLinkedTags: HashMap<Int, LinkedTag> = hashMapOf()

            for(linkedTag in linkedTags) {
                val _id = linkedTag.child("id").value as Long
                val _distance = linkedTag.child("distance").value as Long
                val _direction = linkedTag.child("direction").value as String

                resultLinkedTags[id.toInt()] = LinkedTag(_id.toInt(), _distance.toInt(), Direction.valueOf(_direction))
            }

            val resultTag = Tag(id.toInt(), resultLinkedTags)

            for(spot in spots) {
                val spotName = spot.value as String
                resultTag.addSpot(Spot(spotName, id.toInt()))
            }
            tagList.add(resultTag)

        }
        return tagList

    }
}