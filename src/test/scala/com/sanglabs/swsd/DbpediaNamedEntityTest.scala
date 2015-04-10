package com.sanglabs.swsd

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers

/**
 *
 * The DbpediaNamedEntityTest 
 *
 * @author Sang Venkatraman
 *
 */
class DbpediaNamedEntityTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

  val testSentences = Array("I go to school at Stanford University, which is located in California.",
    "schooled at the Philippines",
    "Where does Toyota have its factories?",
    "What does Mary produce?",
    "What does GM produce?",
    "is GM moving its jobs to Atlanta.",
    "work at Chevy.",
    "work at chevy.",
    "fixing a General Motors car",
    "You told me I was like the Dead Sea")

  "Test ner spots" should "return 3 sentences" in {
    testSentences foreach {sentence =>
      println(sentence)
      val result = DbpediaSpotlightService.getDbpediaEntities(sentence)//StanfordNLPService.nerSpots(sentence)
      println(result)
    }
  }

}
