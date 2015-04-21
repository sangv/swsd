package com.sanglabs.swsd

import junit.framework.TestCase
import org.junit.Test

/**
 *
 * The DbpediaNamedEntityIT
 *
 * @author Sang Venkatraman
 *
 */
class DbpediaNamedEntityIT extends TestCase {

  val testSentences = Array("I go to school at Stanford University, which is located in California.",
    "Schooled at the Philippines",
    "Where does Toyota have its factories?",
    "What does Mary produce?",
    "What does GM produce?",
    "is GM moving its jobs to Atlanta.",
    "work at Chevy.",
    "work at chevy.",
    "fixing a General Motors car",
    "You told me I was like the Dead Sea")

  @Test
  def testFMeasureScore() {
    val result = FMeasureCalculator.calculate(Array("Concept1"),Array("Concept1","Concept2"))
    assert(result.fMeasure > 0.5)
  }

  @Test
  def testIsBrand() {
    val isBrand = DbpediaSpotlightService.isBrand("http://dbpedia.org/resource/Fendi")
    assert(isBrand)
  }





}
