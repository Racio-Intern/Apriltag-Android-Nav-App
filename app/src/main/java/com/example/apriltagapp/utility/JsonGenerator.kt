package com.example.apriltagapp.utility

import com.example.apriltagapp.model.TagFamily
import com.google.gson.Gson

class JsonGenerator {
    fun tagFamilyToJson(tagFamily: TagFamily) {
        val gson = Gson()
        println("result : ${gson.toJson(tagFamily)}")
    }
}