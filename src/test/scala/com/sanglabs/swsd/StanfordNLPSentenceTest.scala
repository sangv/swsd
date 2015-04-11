package com.sanglabs.swsd

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, BeforeAndAfter, FlatSpec}

/**
 *
 * The StanfordNLPSentenceTest 
 *
 * @author Sang Venkatraman
 *
 */
@RunWith(classOf[JUnitRunner])
class StanfordNLPSentenceTest extends FlatSpec with Matchers with BeforeAndAfter {

  "Test sentence splitting" should "return 3 sentences" in {
    val sentences = StanfordNLPService.getSentences("There are multiple sentences in this text. The first one, is this. And this is the second one.")
    sentences.size shouldEqual(3)
    sentences(0) shouldEqual "There are multiple sentences in this text."
    sentences(1) shouldEqual "The first one, is this."
    sentences(2) shouldEqual "And this is the second one."
  }

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
      val result = StanfordNLPService.nerSpots(sentence)
      println(result)
    }
  }

}
