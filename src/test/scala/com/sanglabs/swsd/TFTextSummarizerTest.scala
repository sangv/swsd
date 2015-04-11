package com.sanglabs.swsd

import org.junit.Assert._
import org.junit.Test
import org.scalatest.Matchers
import org.scalatest.junit.{AssertionsForJUnit, JUnitSuite}

/**
 *
 * The TFTextSummarizerTest 
 *
 * @author Sang Venkatraman
 *
 */
class TFTextSummarizerTest extends JUnitSuite with Matchers with AssertionsForJUnit {

  @Test
  def test1() {
    //TODO move this to a test case
    val summarySentences = TFTextSummarizer.summarize("The game of life is a game of everlasting learning. The unexamined life is not worth living. Never stop learning.")
    assertEquals(summarySentences.size, 2)
    assert(summarySentences(0).startsWith("Never "))
    assert(summarySentences(1).startsWith("The game "))
  }

}
