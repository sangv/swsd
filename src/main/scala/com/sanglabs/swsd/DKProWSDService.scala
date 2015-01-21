package com.sanglabs.swsd

import java.io._

import de.tudarmstadt.ukp.dkpro.wsd.graphconnectivity.algorithm.{DegreeCentralityWSD, JungGraphVisualizer}
import de.tudarmstadt.ukp.dkpro.wsd.si.wordnet.WordNetSenseKeySenseInventory
import de.tudarmstadt.ukp.dkpro.wsd.{Pair, UnorderedPair}
import edu.uci.ics.jung.graph.UndirectedGraph

/**
 *
 * The DKProWSDService 
 *
 * @author Sang Venkatraman
 *
 */
object DKProWSDService {

  lazy val graph = deserializeGraph("/Users/sang/Temp/swsd/data/DKProWSD_SK_graph.ser")

  val inventory: WordNetSenseKeySenseInventory = new WordNetSenseKeySenseInventory(new FileInputStream("/Users/sang/Temp/swsd/data/file_properties.xml"))
  inventory.setUndirectedGraph(graph)

  val wsdAlgorithm: DegreeCentralityWSD = new DegreeCentralityWSD(inventory)

  val posConverter = Map[net.sf.extjwnl.data.POS,de.tudarmstadt.ukp.dkpro.wsd.si.POS](
    net.sf.extjwnl.data.POS.ADJECTIVE -> de.tudarmstadt.ukp.dkpro.wsd.si.POS.ADJ,
    net.sf.extjwnl.data.POS.NOUN -> de.tudarmstadt.ukp.dkpro.wsd.si.POS.NOUN,
    net.sf.extjwnl.data.POS.VERB -> de.tudarmstadt.ukp.dkpro.wsd.si.POS.VERB,
    net.sf.extjwnl.data.POS.ADVERB -> de.tudarmstadt.ukp.dkpro.wsd.si.POS.ADV
  )

  // Set up a graph visualizer
  val g: JungGraphVisualizer = new JungGraphVisualizer
  inventory.setSenseDescriptionFormat("<html><b>%w</b><br />%d</html>")
  g.setAnimationDimensions(1000, 700)
  g.setAnimationDelay(0)
  g.setInteractive(false)

  // Bind the visualizer to the algorithm
  //wsdAlgorithm.setGraphVisualizer(g)
  wsdAlgorithm.setSearchDepth(4)

  def disambiguate(text: List[WordAnalysis]) = {


    val sentence: java.util.Collection[Pair[String, de.tudarmstadt.ukp.dkpro.wsd.si.POS]] = new java.util.ArrayList[Pair[String, de.tudarmstadt.ukp.dkpro.wsd.si.POS]]

    for(wordAnalysis <- text) {
        sentence.add(new Pair[String, de.tudarmstadt.ukp.dkpro.wsd.si.POS](wordAnalysis.word, posConverter.get(wordAnalysis.pos).get))
    }


    val dabMap = wsdAlgorithm.getDisambiguation(sentence);
    dabMap
  }


  def deserializeGraph (serializedGraphFilename: String): UndirectedGraph[String, UnorderedPair[String]] = {

    println("Reading graph...")
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

}
