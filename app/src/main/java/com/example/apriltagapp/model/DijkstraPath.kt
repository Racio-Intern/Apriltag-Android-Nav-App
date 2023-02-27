package com.example.apriltagapp.model

import java.util.PriorityQueue

/**
 * 다익스트라 알고리즘이 구현되어 있는 추상 클래스입니다.
 * Node와 Edge class를 상황에 맞게 구현해주고,
 * 구현한 클래스를 활용해 다익스트라를 사용할 수 있습니다.
 */
abstract class DijkstraPath {

    abstract class Node() {
        abstract val id: Int
    }

    abstract class Edge() {
        abstract val startNode: Node
        abstract val endNode: Node
        abstract val weight: Int

    }

    class ResultPath() {
        var distance: Int = 0
            private set

        val path: ArrayList<Node> = arrayListOf()

        /**
         * 최종 경로에 새로운 엣지를 경로로써 추가합니다.
         */
        fun addPath(newEdge: Edge) {
            if(path.last().id != newEdge.startNode.id) {// 경로상 마지막 노드가 새로 추가될 경로의 시작 노드와 다르다면 에러를 출력합니다.
                throw IllegalArgumentException("새로 추가될 노드와 기존 path의 마지막 노드가 불일치합니다.")
            }
            path.add(newEdge.endNode)
            distance += newEdge.weight
        }

    }

    abstract val startNode: Node
    val resultPath: ResultPath = ResultPath()

    /**
     * 시작 노드와 도착 노드를 입력받아서 최적의 경로를 찾아 리턴합니다.
      */
    fun shortestPath(start: Node, destination: Node): ResultPath {
        return ResultPath()
    }


}