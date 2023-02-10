package com.example.apriltagapp.model

import android.util.Log
import java.util.*

class TagGraph(private val tags: ArrayList<Tag>) {

    private val LOGTAG = "TagGraph"
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

        // 최단 거리를 기록하는 배열
        val minDistance: Array<Int> = Array (tagFamily.tagMap.size) {Int.MAX_VALUE}

        // 역추적을 위한 HashMap
        val path: HashMap<Int, Int> = HashMap()

        minDistance[start] = 0
        activeVertices.add(Pair(0, start))
        var where = 0

        while(!activeVertices.isEmpty()) {
            // 새로운 정점 선택
            where = activeVertices.peek()?.second ?: run{
                Log.e(LOGTAG, "Error : active vertices empty!!")
                return null
            }

            if (where == destination) {
                println("min_distance : ${minDistance[where]}")
                break
            }

            activeVertices.remove(activeVertices.peek())

            // 새로운 정점을 통해 갈 때 더 짧은 경로가 발견 된다면 그 정보 갱신
            tagFamily.tagMap[where]?.let{ newTag ->
                for(tag in newTag.linkedTags) {
                    if(minDistance[tag.id] > minDistance[where] + tag.distance) {
                        activeVertices.remove(Pair(minDistance[tag.id], tag.id))
                        minDistance[tag.id] = minDistance[where] + tag.distance
                        activeVertices.add(Pair(minDistance[tag.id], tag.id))

                        path[tag.id] = where
                    }
                }
            } ?: run{
                Log.e(LOGTAG, "Error : tagMap[$where] is empty!!")
                return null
            }
        }

        var nextTagId: Int = where

        while (path[nextTagId] != start) {
            nextTagId = path[nextTagId] ?: run{
                Log.e(LOGTAG, "Error : path[$nextTagId] is empty!!")
                return null
            }
        }

        println("finally next tag id : $nextTagId")

        return tagFamily.tagMap[nextTagId] ?: run{
            Log.e(LOGTAG, "Error : tagMap[$nextTagId] is empty!!")
            null
        }
    }
}