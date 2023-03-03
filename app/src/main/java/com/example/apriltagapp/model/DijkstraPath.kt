package com.example.apriltagapp.model

import java.util.PriorityQueue

/**
 * 다익스트라 알고리즘이 구현되어 있는 클래스입니다.
 * Node와 Edge class를 상황에 맞게 구현해주고,
 * 구현한 클래스를 활용해 다익스트라를 사용할 수 있습니다.
 */
open class DijkstraPath {

    open class Node(val id: Int)

    open class Edge(
        val startNode: Node,
        val endNode: Node,
        val weight: Int
    )

    /**
     * shortest path 알고리즘을 적용시켜 나오는 결과 class 입니다.
     */
    class ResultPath(val distance: Int, val path: ArrayList<Int>) {
        /**
         * 최종 경로에 새로운 엣지를 경로로써 추가합니다.
         */
//        fun addPath(newEdge: Edge) {
//            if(path.last()!= newEdge.startNode.id) {// 경로상 마지막 노드가 새로 추가될 경로의 시작 노드와 다르다면 에러를 출력합니다.
//                throw IllegalArgumentException("새로 추가될 노드와 기존 path의 마지막 노드가 불일치합니다.")
//            }
//            path.add(newEdge.endNode.id)
//            distance += newEdge.weight
//        }
    }

    var resultPath: ResultPath? = null
    val graph: MutableMap<Int, ArrayList<Pair<Node, Int>>> = mutableMapOf()

    /**
     * 총 노드의 리스트와, 엣지의 리스트를 받아, 그래프를 초기화하는 함수입니다.
     */
    fun initGraph(nodes: ArrayList<Node>, edges: ArrayList<Edge>): MutableMap<Int, ArrayList<Pair<Node, Int>>> {
        if(graph.isNotEmpty()) throw Exception("초기화 하려는 그래프가 비어있지 않습니다.")

        for(node in nodes) {
            graph[node.id] = arrayListOf()
        }

        addEdge(edges)
        return graph
    }

    private fun addEdge(edges: ArrayList<Edge>) {
        for(edge in edges) {
            graph[edge.startNode.id]?.add(Pair(Node(edge.endNode.id), edge.weight))
                ?: throw NullPointerException("${edge.startNode.id} 노드가 그래프에 없습니다.")
        }
    }

    /**
     * 시작 노드와 도착 노드를 입력받아서 최적의 경로를 찾아 리턴합니다.
      */
    fun shortestPath(start: Int, destination: Int): ResultPath {
        val activeVertices = PriorityQueue<Pair<Int, Int>> { a, b -> a.first - b.first}

        //최단 거리를 기록하는 배열
        val minDistance: Array<Int> = Array(graph.size) {Int.MAX_VALUE}

        //역추적을 위한 HashMap
        val backtracking: HashMap<Int, Int> = HashMap() // (목적지, 목적지 직전 거쳐야 하는 노드)

        minDistance[start] = 0
        activeVertices.add(Pair(0, start))
        var curNode = 0

        while(!activeVertices.isEmpty()) {
            curNode = activeVertices.peek()?.second ?: run {
                throw NullPointerException("active vertices is empty")
            }

            if(curNode == destination) {
                break
            }

            activeVertices.remove(activeVertices.peek())

            //새로운 정점을 통해 갈 때 더 짧은 경로가 발견된다면 그 정보 갱신
            graph[curNode]?.let { pairs ->
                for(pair in pairs) {
                    if(minDistance[pair.first.id] > minDistance[curNode] + pair.second) {
                        activeVertices.remove(Pair(minDistance[pair.first.id], pair.first.id))
                        minDistance[pair.first.id] = minDistance[curNode] + pair.second
                        activeVertices.add(Pair(minDistance[pair.first.id], pair.first.id))

                        backtracking[pair.first.id] = curNode
                    }
                }
            } ?: throw NullPointerException("tagmap[$curNode] is empty")
        }

        var nextNode: Int = curNode

        //여기부터 추가된 코드
        println("path : $backtracking")

        val distance = minDistance[destination]
        val path = arrayListOf<Int>()
        var nextTagId = backtracking[destination]

        //최종 경로
        path.add(destination)
        while(nextTagId != start) {
            path.add(0, nextTagId?:throw NullPointerException("next tag id가 null입니다."))
            nextTagId = backtracking[nextTagId]
        }

        return ResultPath(distance, path)
    }


}