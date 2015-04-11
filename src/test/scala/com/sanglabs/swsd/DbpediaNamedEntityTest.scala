package com.sanglabs.swsd

import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

/**
 *
 * The DbpediaNamedEntityTest 
 *
 * @author Sang Venkatraman
 *
 */
class DbpediaNamedEntityTest extends FlatSpec with Matchers with BeforeAndAfter {

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
    Option("""
      |President Obama called Wednesday on Congress to extend a tax break
      |for students included in last year's economic stimulus package, arguing
      |that the policy provides more generous assistance.
    """.stripMargin) foreach {sentence =>
      println(sentence)
      val result = DbpediaSpotlightService.getEntities(sentence)//StanfordNLPService.nerSpots(sentence)
      println(result)
    }
  }

  "Test evaluate " should "return 3 sentences" in {
    val result = FMeasureCalculator.calculate(Array("Concept1"),Array("Concept1","Concept2"))
    println(result)
  }

}
