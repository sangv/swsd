package com.sanglabs.swsd

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfter, FlatSpec}

/**
 *
 * The StanfordNLPSentenceTest 
 *
 * @author Sang Venkatraman
 *
 */
@RunWith(classOf[JUnitRunner])
class StanfordNLPSentenceTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

  "Test sentence splitting" should "return 3 sentences" in {
    val sentences = StanfordNLPService.sentences("There are multiple sentences in this text. The first one, is this. And this is the second one.")
    sentences.size shouldEqual(3)
    sentences(0) shouldEqual "There are multiple sentences in this text."
    sentences(1) shouldEqual "The first one, is this."
    sentences(2) shouldEqual "And this is the second one."
  }

}
