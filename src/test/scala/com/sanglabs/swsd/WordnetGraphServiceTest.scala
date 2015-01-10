package com.sanglabs.swsd

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfter, FlatSpec}

/**
 *
 * The WordnetGraphServiceTestSpec tests the WordnetGraphService
 *
 * @author Sang Venkatraman
 *
 */
@RunWith(classOf[JUnitRunner])
class WordnetGraphServiceTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

  "Querying the graph database for hypernym trees" should "return correct parent offsets" in {
    WordnetGraphService.getHypernymTree("travel#v#1") shouldEqual(1835496)
    WordnetGraphService.getHypernymTree("bank#n#1") shouldEqual(1740)
    WordnetGraphService.getHypernymTree("actually#r#1") shouldEqual(149510)
    WordnetGraphService.getHypernymTree("genetic#a#1") shouldEqual(1314537)
  }


  "Test traversal of adjacent synsets" should "result" in {
    val shortestPath = WordnetGraphService.shortestPath("swim#v#1","travel#v#1")
    println(shortestPath.mkString(" -> "))
    shortestPath should not be Nil
    shortestPath.size shouldEqual(2)
  }

  "Test traversal of non-adjacent synsets" should "result" in {
    val shortestPath = WordnetGraphService.shortestPath("fish#n#1","sea#n#1")
    println(shortestPath.mkString(" -> "))
    shortestPath should not be Nil
    shortestPath.size shouldEqual(8)
  }

  "Test get synsets" should "result" in {
    val synset = WordnetGraphService.getSynset("new_york#n#1")
    assert(synset != null)
  }


}
