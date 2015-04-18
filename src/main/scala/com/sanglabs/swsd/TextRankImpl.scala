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

case class TRNode(index: Int, id:String, size: Int = 5, URI: String = "", color: String = "gray")
case class TREdge(source: Int, target: Int, color: String = "black")
case class TRGraph(directed: Boolean = true, nodes: List[TRNode] = List[TRNode](), links: List[TREdge])

object TextRankImpl {

  def calculate(text: String): ListMap[WordAnalysis,Double] =
  {

    //switch between directed and undirected
    val graph: Graph[WordAnalysis, String] = new UndirectedSparseGraph[WordAnalysis, String]()

    var outerEdgeMap = Map[WordAnalysis, WordAnalysis]()

    var edgeIndex = -1

    //no compoundification considered and filterd by POS
    val sentences = StanfordNLPService.analyze(text)

    var nodes = List[TRNode]()
    var edges = List[TREdge]()

    def checkAndInsertNode(elem:WordAnalysis): TRNode = {
      graph.addVertex(elem)
      nodes.find(node => {node.id == elem.word}) match {
        case Some(x: TRNode) => x
        case None => {
            val node = TRNode(nodes.size,elem.word)
            nodes :+= node
            node
          }
        }
      }

    for (sentence <- sentences) {

      val words = sentence.words filter (p => (POS.NOUN.equals(p.pos) || POS.ADJECTIVE.equals(p.pos)))


      //window size 2
      for (List(first, second, third) <- words.sliding(3)) {
        val firstNode = checkAndInsertNode(first)
        val secondNode = checkAndInsertNode(second)
        val thirdNode = checkAndInsertNode(third)

        //We are ignoring multiple same edges between same nodes
        edgeIndex += 1
        outerEdgeMap += (first -> second)
        graph.addEdge("links_to_" + edgeIndex, first, second)

        edges :+= TREdge(firstNode.index,secondNode.index)

        //if(graph.findEdge(first,third) != null)
        edgeIndex += 1
        graph.addEdge("links_to_" + edgeIndex, first, third)
        outerEdgeMap += (first -> third)
        edges :+= TREdge(firstNode.index,thirdNode.index)
      }

    }

    val ranker = new PageRank(graph, 0.15);
    ranker.evaluate();

    var scores = Map[WordAnalysis, Double]()
    for (word <- outerEdgeMap.keys) {
      scores += (word -> ranker.getVertexScore(word))
      //println(s"${word.word} + ${word.pos} => ${ranker.getVertexScore(word)}")
    }

    /*val d3Graph = TRGraph(true,nodes,edges)
    import org.json4s.JsonDSL._
    import org.json4s.jackson.JsonMethods._
    import org.json4s.jackson.Serialization.write
    println(pretty(write(d3Graph)(org.json4s.DefaultFormats))) */

    val result = ListMap(scores.toList.sortBy(_._2).reverse: _*)
    result  foreach (p => println(s"${p._1} => ${p._2}"))
    result
  }

  //Use n*n similarity matrix to calculate similarity across getSentences and aggregate the results
  def topSentences(text: String, numberOfSentences: Int = 2): List[(String,Double)] = {
    val sentences = TextPreprocessor.getSentences(text)

    var sentenceStems = Map[String,List[String]]()
    for(sentence <- sentences){
      sentenceStems += (sentence -> TextPreprocessor.removeStopWordsAndStem(sentence))
    }

    val stemArray = sentenceStems.values.toList
    val scoresMatrix = Array.ofDim[Double](sentenceStems.size, sentenceStems.size)
    var sentenceScores = Map[Int,Double]()
    for(i <- 0 until stemArray.size){
      var score = 0.0
      for(j <- 0 until stemArray.size) {
         if(i == j){
           scoresMatrix(i)(j) = 0.0 //explicitly making a sentence not match itself, duh
         } else {
           scoresMatrix(i)(j) = stemArray(i).intersect(stemArray(j)).length*1.0/Math.max(stemArray(i).length,stemArray(j).length)
           score += scoresMatrix(i)(j)
         }
      }
      sentenceScores += (i -> score)
    }
    //Math.round(0.3F*sentences.length)
    val indexes = ListMap(sentenceScores.toList.sortBy{_._2}:_*).toList.reverse take(numberOfSentences)
    val results: List[(String,Double)] = indexes.map(s => (sentences(s._1),s._2)).toList
    results
  }

}
