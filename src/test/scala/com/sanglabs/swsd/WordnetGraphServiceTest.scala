package com.sanglabs.swsd

import net.sf.extjwnl.data.POS
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfter, FlatSpec}

/**
 *
 * The WordnetGraphServiceTestSpec tests the WordnetGraphService
 *
 * @author Sang Venkatraman
 *
 */
class WordnetGraphServiceTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

  "Querying the graph database for hypernym trees" should "return correct parent offsets" in {
    WordnetGraphService.getHypernymTree("travel#v#1") shouldEqual(1835496)
    WordnetGraphService.getHypernymTree("bank#n#1") shouldEqual(1740)
    WordnetGraphService.getHypernymTree("actually#r#1") shouldEqual(149510)
    WordnetGraphService.getHypernymTree("genetic#a#1") shouldEqual(1314537)
  }

  "Plethora of fish at sea" should "have 1 simple disambiguation" in {
    val options = scala.collection.mutable.Map(WordAnalysis("Plethora","plethora",POS.NOUN) -> List("plethora#n#1"),
    WordAnalysis("fish","fish",POS.NOUN) -> List("fish#n#1", "fish#n#2", "fish#n#3", "fish#n#4"),
    WordAnalysis("sea","sea",POS.NOUN) -> List("sea#n#1", "sea#n#2", "sea#n#3"))
    val result =  WordnetGraphService.disambiguate(options)
    result.size shouldEqual(1)
    result.get(WordAnalysis("Plethora","plethora",POS.NOUN)).get shouldEqual("plethora#n#1")
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
    shortestPath.size shouldEqual(6)
  }


}
