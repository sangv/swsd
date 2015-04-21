package com.sanglabs.swsd

import org.junit.Test
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.{AssertionsForJUnit, JUnitRunner, JUnitSuite}

/**
 *
 * The StanfordNLPSentenceTest 
 *
 * @author Sang Venkatraman
 *
 */
@RunWith(classOf[JUnitRunner])
class StanfordNLPSentenceTest extends JUnitSuite with Matchers with AssertionsForJUnit {

  @Test
  def testSentenceSplitting() {
    val sentences = StanfordNLPService.getSentences("There are multiple getSentences in this text. The first one, is this. And this is the second one.")
    sentences.size shouldEqual(3)
    sentences(0) shouldEqual "There are multiple getSentences in this text."
    sentences(1) shouldEqual "The first one, is this."
    sentences(2) shouldEqual "And this is the second one."
  }

  val testSentences = Array("I go to school at Stanford University, which is located in California.",
    "schooled at the Philippines",
    "Where does Toyota have its factories?",
    "What does Mary produce?",
    "What does GM produce?",
    "is GM moving some of its jobs to Atlanta.",
    "work at Chevy.",
    "work at chevy.",
    "fixing a General Motors car",
    "You told me I was like the Dead Sea")

  @Test
  def testNERSpots() {
    testSentences foreach {sentence =>
      println(sentence)
      val result = StanfordNLPService.nerSpots(sentence)
      println(result)
    }
  }

  @Test
  def testSentiment(): Unit = {
    val options = WordNetDictionaryService.lookupOptions("They are really happy to be here.")
    println(options)
    val wsds = DKProWSDService.disambiguate(options.keys.toList)
    println(wsds)
    /*val sentiWordNetService = new SentiWordNetService()
    val results = wsds map {s => sentiWordNetService.extract(s._2)}
    results foreach println
    println("Done")*/
  }



}
