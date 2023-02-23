package com.example.apriltagapp.utility

import android.util.Log
import com.example.apriltagapp.model.*
import com.google.firebase.database.DataSnapshot

class JsonParser {
    fun snapshotToTagFamily(snapshot: DataSnapshot): ArrayList<Tag> {
        val tagList = arrayListOf<Tag>()
        for(child in snapshot.children) {
            val linkedTags = child.child("linkedTags").children
            val id = child.child("id").value as Long
            val x = child.child("x").value as Any
            val y = child.child("y").value as Any
            val rot = child.child("rot").value as Any

            val resultLinkedTags: ArrayList<LinkedTag> = arrayListOf()

            for(linkedTag in linkedTags) {
                val _id = (linkedTag.child("id").value as Long).toInt()
                val _distance = (linkedTag.child("distance").value as Long).toInt()
                val _direction = linkedTag.child("direction").value as String

                resultLinkedTags.add(LinkedTag(_id, _distance, Direction.valueOf(_direction)))
            }
            val resultTag = Tag(id.toInt(), resultLinkedTags)

            //type casting
            if(x is Long) {
                resultTag.x = x.toDouble()
            }
            else if(x is Double){
                resultTag.x = x.toDouble()
            }

            if(y is Long) {
                resultTag.y = y.toDouble()
            }
            else if (y is Double) {
                resultTag.y = y.toDouble()
            }

            if(rot is Double) {
                resultTag.rot = rot.toDouble()
            }
            else if(rot is Long) {
                resultTag.rot = rot.toDouble()
            }

            tagList.add(resultTag)
            Log.d("json", "새로운 태그 생성 : id : $id, x = $x, y = $y, rotation = $rot")
        }
        return tagList
    }

    fun snapshotToSpot(snapshot: DataSnapshot): HashMap<String, Int> {
        val spots = hashMapOf<String, Int>()
        for(child in snapshot.children) {
            val tagId = (child.child("tagId").value as Long).toInt()
            val name = child.child("name").value as String
            spots[name] =  tagId
        }

        return spots
    }
}