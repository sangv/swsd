package com.sanglabs.swsd

import java.io._

import de.tudarmstadt.ukp.dkpro.wsd.UnorderedPair
import de.tudarmstadt.ukp.dkpro.wsd.graphconnectivity.algorithm.JungGraphVisualizer
import de.tudarmstadt.ukp.dkpro.wsd.si.SenseInventoryException
import de.tudarmstadt.ukp.dkpro.wsd.si.wordnet.WordNetSenseKeySenseInventory
import edu.uci.ics.jung.graph.{Graph, UndirectedGraph}
import grizzled.slf4j.Logger
import org.apache.commons.collections15.Transformer

import scala.collection.immutable.Map

/**
 *
 * The JungGraphConnectivityService 
 *
 * @author Sang Venkatraman
 *
 */
trait JungGraphConnectivityService {

  val graph = deserializeGraph("data/DKProWSD_SK_graph.ser")

  val inventory: WordNetSenseKeySenseInventory = new WordNetSenseKeySenseInventory(new FileInputStream("data/file_properties.xml"))
  inventory.setUndirectedGraph(graph)
  var dfsCount: Int = 0


  val searchDepth: Int = 4 //default 3

  // Set up a graph visualizer
  val graphVisualizer: JungGraphVisualizer = null//new JungGraphVisualizer
  //graphVisualizer.setInteractive(true)
  inventory.setSenseDescriptionFormat("<html><b>%w</b><br />%d</html>")

  if(graphVisualizer != null) {
    graphVisualizer.setAnimationDimensions(1000, 700)
    graphVisualizer.setAnimationDelay(0)
    graphVisualizer.setInteractive(true)
  }

  val logger = Logger[this.type]


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
        FileInputStream("data/file_properties.xml"));
    val g = si.getUndirectedGraph();
    val fileOut = new
        FileOutputStream("data/DKProWSD_SK_graph.ser");
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
