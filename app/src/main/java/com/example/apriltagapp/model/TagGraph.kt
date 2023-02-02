package com.example.apriltagapp.model

class TagGraph(val tags: Array<Tag>) {

    fun findTagById(id: Int): Tag?{
        for(tag in tags) {
            if(tag.id == id) {
                return tag
            }
        }
        return null
    }

    fun printGraph() {
        for(tag in tags) {
            println("[${tag.id}번 태그]")
            println("Spots : ${tag.spots}")
            for(linkedTag in tag.linkedTags) {
                println("${linkedTag.value.id}번 태그로의 거리 : ${linkedTag.value.distance}, 방향 : ${linkedTag.value.direction}")
            }
            println("----------")
        }
    }

    fun insertTag() {

    }

    fun deleteTag() {

    }

    /** 다익스트라 알고리즘으로 태그 간의 최단거리를 구합니다. */
    fun shortestPath(start: Int, destination: Int): Tag{
        findTagById(2)?.let {
            return it
        }
        throw Exception("경로 탐색 결과, 최단 거리가 없습니다.")
    }
}