package com.example.apriltagapp.model.repository

import androidx.lifecycle.MutableLiveData
import com.example.apriltagapp.model.Direction
import com.example.apriltagapp.model.LinkedTag
import com.example.apriltagapp.model.Tag
import com.example.apriltagapp.model.TagFamily
import com.example.apriltagapp.utility.JsonParser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class TagFamilyRepository() {
    val jsonParser = JsonParser()

    val database = Firebase.database

    fun observeTagFamily(tagFamily: MutableLiveData<TagFamily>) {
        database.getReference("tagMap")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val parseResult = jsonParser.snapshotToTagFamily(snapshot)

                    tagFamily.postValue(TagFamily(parseResult))
                    println(tagFamily.value?.tagMap)
                }

                override fun onCancelled(error: DatabaseError) {
                    println("cancelled : $error")
                }

            })
    }



    suspend fun postTag(tag: Tag) {

        withContext(Dispatchers.IO) {

        }
    }
}