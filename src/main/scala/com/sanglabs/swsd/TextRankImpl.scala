package com.sanglabs.swsd

import edu.uci.ics.jung.algorithms.scoring.PageRank
import edu.uci.ics.jung.graph.{Graph, UndirectedSparseGraph}
import net.sf.extjwnl.data.POS

import scala.collection.immutable.ListMap

/**
 *
 * The TextRankImpl
 *
 * @author Sang Venkatraman
 *
 */
object TextRankImpl {

  def calculate(text: String): ListMap[WordAnalysis,Double] =
  {

    //switch between directed and undirected
    val graph: Graph[WordAnalysis, String] = new UndirectedSparseGraph[WordAnalysis, String]()

    var outerEdgeMap = Map[WordAnalysis, WordAnalysis]()

    var edgeIndex = -1

    //no compoundification considered and filterd by POS
    val sentences = StanfordNLPService.analyze(text)

    for (sentence <- sentences) {

      val words = sentence.words filter (p => (POS.NOUN.equals(p.pos) || POS.ADJECTIVE.equals(p.pos)))


      //window size 2
      for (List(first, second, third) <- words.sliding(3)) {
        graph.addVertex(first)
        graph.addVertex(second)
        graph.addVertex(third)

        //We are ignoring multiple same edges between same nodes
        edgeIndex += 1
        outerEdgeMap += (first -> second)
        graph.addEdge("links_to_" + edgeIndex, first, second)

        //if(graph.findEdge(first,third) != null)
        edgeIndex += 1
        graph.addEdge("links_to_" + edgeIndex, first, third)
        outerEdgeMap += (first -> third)
      }

    }

    val ranker = new PageRank(graph, 0.15);
    ranker.evaluate();

    var scores = Map[WordAnalysis, Double]()
    for (word <- outerEdgeMap.keys) {
      scores += (word -> ranker.getVertexScore(word))
      //println(s"${word.word} + ${word.pos} => ${ranker.getVertexScore(word)}")
    }

    val result = ListMap(scores.toList.sortBy(_._2).reverse: _*)
    result  foreach (p => println(s"${p._1} => ${p._2}"))
    result
  }

}
