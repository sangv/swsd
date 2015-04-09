package com.sanglabs.swsd

import java.io._
import java.util

import de.tudarmstadt.ukp.dkpro.wsd.graphconnectivity.algorithm.JungGraphVisualizer
import de.tudarmstadt.ukp.dkpro.wsd.si.wordnet.WordNetSenseKeySenseInventory
import de.tudarmstadt.ukp.dkpro.wsd.si.{POS, SenseInventoryException}
import de.tudarmstadt.ukp.dkpro.wsd.{Pair, UnorderedPair}
import edu.uci.ics.jung.algorithms.scoring.HITS
import edu.uci.ics.jung.graph.{Graph, UndirectedGraph, UndirectedSparseGraph}
import grizzled.slf4j.Logger
import org.apache.commons.collections15.Transformer

import scala.collection.JavaConverters._
import scala.collection.immutable.{ListMap, Map}

/**
 *
 * The JungGraphConnectivityService 
 *
 * @author Sang Venkatraman
 *
 */
trait JungGraphConnectivityService {

  val graph = deserializeGraph("/Users/sang/Temp/swsd/data/DKProWSD_SK_graph.ser")

  val inventory: WordNetSenseKeySenseInventory = new WordNetSenseKeySenseInventory(new FileInputStream("/Users/sang/Temp/swsd/data/file_properties.xml"))
  inventory.setUndirectedGraph(graph)
  var dfsCount: Int = 0


  val searchDepth: Int = 4 //default 3

  // Set up a graph visualizer
  val graphVisualizer: JungGraphVisualizer = null// new JungGraphVisualizer
  inventory.setSenseDescriptionFormat("<html><b>%w</b><br />%d</html>")

  if(graphVisualizer != null) {
    graphVisualizer.setAnimationDimensions(1000, 700)
    graphVisualizer.setAnimationDelay(0)
    graphVisualizer.setInteractive(true)
  }

  val logger = Logger[this.type]

  def ownDisambiguation(sods: java.util.Collection[Pair[String, de.tudarmstadt.ukp.dkpro.wsd.si.POS]], dGraph: Graph[String, UnorderedPair[String]]) : Map[Pair[String, POS], Map[String, Double]]


  def ownDisambiguation(sods: java.util.Collection[Pair[String, POS]]): Map[Pair[String, POS], Map[String, Double]] = {

    val t0 = System.nanoTime()
    val siGraph: Graph[String, UnorderedPair[String]] = inventory.getUndirectedGraph
    val dGraph: Graph[String, UnorderedPair[String]] = new UndirectedSparseGraph[String, UnorderedPair[String]]
    var sodCount: Int = 0

    if (graphVisualizer != null) {
      graphVisualizer.initializeColorMap(sods.size)
      graphVisualizer.setVertexToolTipTransformer(new VertexToolTipTransformer)
    }

    for (wsdItem <- sods.asScala) {
      val senses: List[String] = inventory.getSenses(wsdItem.getFirst, wsdItem.getSecond).asScala.toList
      if (senses.isEmpty) {
        logger.warn("unknown subject of disambiguation " + wsdItem)
      }

      for (sense <- senses) {
        if (graphVisualizer != null) {
          graphVisualizer.setColor(sense, sodCount)
        }
        dGraph.addVertex(sense)
      }
      sodCount += 1
    }
    logger.debug(dGraph.toString)

    if (graphVisualizer != null) {
      graphVisualizer.initialize(dGraph)
    }

    val s: util.Collection[String] = new util.HashSet[String](dGraph.getVertices)

    for (v <- s.asScala) {
      logger.debug("Beginning DFS from " + v)
      val t: util.Collection[String] = new util.HashSet[String](s)
      t.remove(v)
      val synsetPath: util.Stack[String] = new util.Stack[String]
      synsetPath.push(v)
      dfs(v, t, siGraph, dGraph, synsetPath, new util.Stack[UnorderedPair[String]], searchDepth)
    }
    logger.debug(dGraph.toString)
    val solutions: Map[Pair[String, POS], Map[String, Double]] = ownDisambiguation(sods, dGraph)

    val timeElapsed = (System.nanoTime() - t0)/1000000000
    println(s"Disambiguation took ${timeElapsed} secs")

    // Repaint the frame to show the disambiguated senses
    if (graphVisualizer != null) {
      graphVisualizer.refresh
      Thread.sleep(10000) //TODO do an onmouse click
    }


    return solutions
  }

  def getCenters(wsds: List[String]): List[(String,Double)] = {

    val t0 = System.nanoTime()
    val siGraph: Graph[String, UnorderedPair[String]] = inventory.getUndirectedGraph
    val dGraph: Graph[String, UnorderedPair[String]] = new UndirectedSparseGraph[String, UnorderedPair[String]]

      for (sense <- wsds) {
        dGraph.addVertex(sense)
      }


    val s: util.Collection[String] = new util.HashSet[String](dGraph.getVertices)

    for (v <- s.asScala) {
      logger.debug("Beginning DFS from " + v)
      val t: util.Collection[String] = new util.HashSet[String](s)
      t.remove(v)
      val synsetPath: util.Stack[String] = new util.Stack[String]
      synsetPath.push(v)
      dfs(v, t, siGraph, dGraph, synsetPath, new util.Stack[UnorderedPair[String]], searchDepth)
    }
    logger.debug(dGraph.toString)


    val hitsRanker: HITS[String,UnorderedPair[String]] = new HITS(dGraph)
    hitsRanker.evaluate();
    var scoresMap = Map[String,Double]()
    wsds foreach { v => {scoresMap += (v -> hitsRanker.getVertexScore(v).hub)}}


    //println(scoresMap)

    return ListMap(scoresMap.toList.sortBy(_._2): _*).toList.reverse
  }

  def dfs (startVertex: String, goalVertices: util.Collection[String], siGraph: Graph[String, UnorderedPair[String]], dGraph: Graph[String, UnorderedPair[String]], vertexPath: util.Stack[String], edgePath: util.Stack[UnorderedPair[String]], maxDepth: Int) : Boolean = {

    logger.debug("count=" + ({
      dfsCount += 1; dfsCount - 1
    }) + " depth=" + (searchDepth - maxDepth) + " synset=" + startVertex)

    // We have found a goal
    if (goalVertices.contains(startVertex)) {
      logger.debug("Found goal at " + startVertex)
      for (p <- edgePath.asScala) {
        logger.debug(p.toString)
      }
      return true
    }

    // We have reached the maximum depth
    if (maxDepth == 0) {
      logger.debug("Reached maximum depth at " + startVertex)
      return false
    }

    // Visit all neighbours of this vertex
    for (edge <- siGraph.getOutEdges(startVertex).asScala) {
      val neighbour: String = siGraph.getOpposite(startVertex, edge)
      (vertexPath.contains(neighbour)) match {
        case true => logger.debug("Encountered loop at " + neighbour)
        case false => {
          (dGraph.containsEdge(edge)) match {
            case true => logger.debug("Path already in graph at " + edge)
            case false => {
              edgePath.push(edge)
              vertexPath.push(neighbour)
              logger.debug("Recursing to " + edge)
              if (dfs(neighbour, goalVertices, siGraph, dGraph, vertexPath, edgePath, maxDepth - 1) == true) {
                logger.debug("Adding " + edge)
                addPath(dGraph, edgePath)
              }
              else {
                logger.debug("Not adding " + edge)
              }
              edgePath.pop
              vertexPath.pop
            }
          }
        }
      }
    }

    logger.debug("Reached dead end at " + startVertex)
    return false
  }


  def addPath (graph: Graph[String, UnorderedPair[String]], edgeStack: util.Stack[UnorderedPair[String]]) {

    for (edge <- edgeStack.asScala) {
      if (graph.containsEdge(edge) == false) {
        graph.addEdge(edge, edge.getFirst, edge.getSecond)
        if (graphVisualizer != null) {
          //graphVisualizer.animate(graph, edge, edge.getFirst, edge.getSecond)
        }
      }
    }
  }

  def deserializeGraph (serializedGraphFilename: String): UndirectedGraph[String, UnorderedPair[String]] = {

    val t0 = System.nanoTime()
    println("Reading graph...") //TODO use logger
    val graphfile: File = new File(serializedGraphFilename)
    if (graphfile.exists == false) {
      return null
    }
    val fileIn: FileInputStream = new FileInputStream(graphfile)
    val in: ObjectInputStream = new ObjectInputStream(fileIn)
    var g: UndirectedGraph[String, UnorderedPair[String]] = null
    g = in.readObject.asInstanceOf[UndirectedGraph[String, UnorderedPair[String]]]
    in.close
    fileIn.close
    println("Read a graph with " + g.getEdgeCount + " edges and " + g.getVertexCount + " vertices")
    val timeElapsed = (System.nanoTime() - t0)/1000000000
    println(s"Reading graph took ${timeElapsed} secs")
    g
  }

  def produceSerVersion(): Unit ={
    val si = new WordNetSenseKeySenseInventory(new
        FileInputStream("/Users/sang/Temp/swsd/data/file_properties.xml"));
    val g = si.getUndirectedGraph();
    val fileOut = new
        FileOutputStream("/Users/sang/Temp/swsd/data/DKProWSD_SK_graph.ser");
    val out = new ObjectOutputStream(fileOut);
    out.writeObject(g);
    out.close();
    fileOut.close();
  }

  class VertexToolTipTransformer extends Transformer[String, String] {
    def transform(s: String): String = {
      try {
        return inventory.getSenseDescription(s)
      }
      catch {
        case e: SenseInventoryException => {
          return e.toString
        }
      }
    }
  }

  def getCenters(wsds: List[String], dGraph: Graph[String, UnorderedPair[String]]) : Map[String, Double]
}
