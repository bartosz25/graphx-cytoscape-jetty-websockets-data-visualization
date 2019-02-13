package com.waitingforcode

import java.net.URI
import javax.websocket.{ClientEndpoint, ContainerProvider, Session}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._

@ClientEndpoint
class PushEndpoint {
}
object IterativeProcessing {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Spark Pregel real-time visualization").setMaster("local[*]")
    val sparkContext = SparkContext.getOrCreate(conf)
    val vertices = sparkContext.parallelize(
      Seq((1L, ""), (2L, ""), (3L, ""), (4L, ""), (5L, ""))
    )
    val edges = sparkContext.parallelize(
      Seq(Edge(1L, 2L, 10), Edge(2L, 3L, 15), Edge(3L, 5L, 0), Edge(4L, 1L, 3))
    )
    val graph = Graph(vertices, edges)

    graph.edges.foreachPartition(vertexIdIntIterator => {
      val endpoint = URI.create("ws://localhost:7711/push")
      val websocketContainer = ContainerProvider.getWebSocketContainer
      val websocketSession = websocketContainer.connectToServer(classOf[PushEndpoint], endpoint)
      vertexIdIntIterator.foreach {
        case edge => {
          val triplet = Triplet(Vertex(edge.srcId, ""), Some(Vertex(edge.dstId, "")), Some(edge.attr))
          websocketSession.getBasicRemote.sendText(PregelFunctions.objectMapper.writeValueAsString(triplet))
          // slow down for the illustration purposes
          Thread.sleep(1000)
        }
      }
    })

    Pregel(graph, "", 12,
      EdgeDirection.In)(PregelFunctions.mergeMessageBeginningSuperstep, PregelFunctions.createMessages,
      PregelFunctions.mergeMessagePartitionLevel)
  }


}


object PregelFunctions {

  val endpoint = URI.create("ws://localhost:7711/push")
  val objectMapper = new ObjectMapper()
  objectMapper.registerModule(DefaultScalaModule)

  val localWebscketSession = new ThreadLocal[Session]
  val IteratedPaths = new scala.collection.mutable.ListBuffer[String]()

  def mergeMessageBeginningSuperstep(vertexId: Long, currentValue: String, message: String): String = {
    if (localWebscketSession.get() == null) {
      localWebscketSession.set({
        val websocketContainer = ContainerProvider.getWebSocketContainer
        websocketContainer.connectToServer(classOf[PushEndpoint], endpoint)
      })

    }
    val triplet = Triplet(Vertex(vertexId, s"${vertexId} (${message})"))
    localWebscketSession.get().getBasicRemote.sendText(objectMapper.writeValueAsString(triplet))
    message
  }

  def mergeMessagePartitionLevel(message1: String, message2: String) = s"${message1} + ${message2}"

  def createMessages(edgeTriplet: EdgeTriplet[String, Int]): Iterator[(VertexId, String)] = {
    val resolvedMessage = if (edgeTriplet.srcAttr.trim.isEmpty) {
      edgeTriplet.attr.toString
    } else {
      edgeTriplet.srcAttr + " + " + edgeTriplet.attr.toString
    }
    IteratedPaths.synchronized {
      IteratedPaths.append(edgeTriplet.toString())
    }
    Iterator((edgeTriplet.dstId, resolvedMessage))
  }

}


case class Triplet(source: Vertex, target: Option[Vertex] = None, edgeValue: Option[Int] = None)
case class Vertex(id: Long, data: String) {
  // Copy data into label, otherwise cytoscape complains about
  // "Do not assign mappings to elements without corresponding data (i.e. ele `2` has no mapping for property
  // `label` with data field `data`); try a `[data]` selector to limit scope to elements with `data` defined"
  val label = data
}