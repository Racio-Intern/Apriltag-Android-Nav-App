package com.example.apriltagapp.model

import android.util.Log
import java.util.PriorityQueue

class TagGraph(private val tags: ArrayList<Tag>) {

    //    val tagFamily: Map<Int, Tag> = tags.associateBy { it.id }
    val tagFamily: TagFamily = TagFamily(tags)

    fun printGraph() {
        for (tag in tags) {
            println("[${tag.id}번 태그]")
            println("Spots : ${tag.spots}")
            for (linkedTag in tag.linkedTags) {
                println("${linkedTag.id}번 태그로의 거리 : ${linkedTag.distance}, 방향 : ${linkedTag.direction}")
            }
            println("----------")
        }
    }

    fun insertTag() {

    }

    fun deleteTag() {

    }

    /** MinHeap을 이용한 다익스트라 알고리즘으로 태그 간의 최단거리를 구합니다.
     *  시간 복잡도 : O((|E| + |V| ) * log|V|)
     * */

    fun shortestPath(start: Int, destination: Int): Tag? {
        val activeVertices = PriorityQueue<Pair<Int, Int>> { a, b -> a.first - b.first }
        val startTag = tagFamily.tagMap[start]
        val minDistance: Array<Int> = Array<Int>(tagFamily.tagMap.size) { Int.MAX_VALUE }
        val tagSelected: Array<Boolean> = Array<Boolean>(tagFamily.tagMap.size) { false }


        val path: HashMap<Int, Int> = HashMap()

        minDistance[start] = 0
        activeVertices.add(Pair(0, start))
        var where: Int = start

        while (!activeVertices.isEmpty()) {
            where = activeVertices.peek()?.second?: run {
                Log.d("ERROR", "Error : active vertices empty!!")
                return null
            }


            if (where == destination) {
                println("min_distance : ${minDistance[where]}")
                break
            }

            activeVertices.remove(activeVertices.peek())

            tagFamily.tagMap[where]?.let { tag ->
                for (linkedTag in tag.linkedTags) {
                    if (minDistance[linkedTag.id] > minDistance[where] + linkedTag.distance) {
                        activeVertices.remove(Pair(minDistance[linkedTag.id], linkedTag.id))
                        minDistance[linkedTag.id] = minDistance[where] + linkedTag.distance
                        activeVertices.add(Pair(minDistance[linkedTag.id], linkedTag.id))

                        path[linkedTag.id] = where
                    }
                }
            }
        }

        var nextTagId: Int? = path[where]

        while (nextTagId != start && nextTagId != null) {
            nextTagId = path[nextTagId]
        }

        return nextTagId?.let {
            tagFamily.tagMap[it]
        }
    }
}