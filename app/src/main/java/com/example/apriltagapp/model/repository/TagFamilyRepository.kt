package com.example.apriltagapp.model.repository

import androidx.lifecycle.MutableLiveData
import com.example.apriltagapp.model.*
import com.example.apriltagapp.utility.JsonParser
import com.example.apriltagapp.utility.NonNullMutableLiveData
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

    fun observeTagFamily(tagGraph: NonNullMutableLiveData<TagGraph>) {
        database.getReference("tagMap")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    println("스냅샷 : ${snapshot}")
                    val parseResult = jsonParser.snapshotToTagFamily(snapshot)

                    tagGraph.postValue(TagGraph(parseResult))
                }

                override fun onCancelled(error: DatabaseError) {
                    println("cancelled : $error")
                }

            })
    }

    fun observeSpots(spots: MutableLiveData<HashMap<String, Int>>) {
        database.getReference("spots")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val parseResult = jsonParser.snapshotToSpot(snapshot)
                    spots.postValue(parseResult)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }



    suspend fun postTag(tag: Tag) {

        withContext(Dispatchers.IO) {

        }
    }
}