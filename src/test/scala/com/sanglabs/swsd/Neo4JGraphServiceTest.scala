package com.sanglabs.swsd

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfter, FlatSpec}

import scala.collection.immutable.ListMap

/**
 *
 * The WordnetGraphServiceTestSpec tests the WordnetGraphService
 *
 * @author Sang Venkatraman
 *
 */
@RunWith(classOf[JUnitRunner])
class Neo4JGraphServiceTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

  /*"Querying the graph database for hypernym trees" should "return correct parent offsets" in {
    Neo4JGraphService.getHypernymTree("travel#v#1") shouldEqual(1835496)
    Neo4JGraphService.getHypernymTree("bank#n#1") shouldEqual(1740)
    Neo4JGraphService.getHypernymTree("actually#r#1") shouldEqual(149510)
    Neo4JGraphService.getHypernymTree("genetic#a#1") shouldEqual(1314537)
  }


  "Test traversal of adjacent synsets" should "result" in {
    val shortestPath = Neo4JGraphService.shortestPath("swim#v#1","travel#v#1")
    shortestPath should not be Nil
    shortestPath.size shouldEqual(2)
  }

  "Test traversal of non-adjacent synsets" should "result" in {
    val shortestPath = Neo4JGraphService.shortestPath("fish#n#1","sea#n#1",5)
    shortestPath shouldEqual Nil
  }

  "Test get synsets" should "result" in {
    val synset = Neo4JGraphService.getSynset("new_york#n#1")
    assert(synset != null)
  }*/

  "Test getting hypernym synset name" should "result" in {

    val wsds = List("bank#n#1","acquirer#n#2")
    val listOfLists: List[List[String]] = wsds map {Neo4JGraphService.getHypernymSynsetNodes(_) map {_._2.head}}

    //scala it linearly from 1 to 0 and recurse without a search depth - divide 1 by length of list and keep adding
    var scores = Map[String,Double]()

    //results foreach {r => {println(r._1,r._2 mkString(", "))} }
    //results.indices.foreach(i => {scores += (results(i) -> (scores.getOrElse(results(i),0.0) + scores(i)))})
    listOfLists foreach { results =>
      results.indices.foreach(i => {
        scores += (results(i) -> (scores.getOrElse(results(i), 0.0) + ((results.length - i) * 1.0 / results.length)))
      })
    }

    val sortedScores = ListMap(scores.toList.sortBy(_._2): _*).toList.reverse
    println(sortedScores)
    println("======================================")
    println(sortedScores.take(10))
    println("Done")
  }


}
