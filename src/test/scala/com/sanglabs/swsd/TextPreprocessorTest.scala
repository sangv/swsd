package com.sanglabs.swsd

import org.junit.Test
import org.scalatest.Matchers
import org.scalatest.junit.{AssertionsForJUnit, JUnitSuite}

/**
 *
 * The TextPreprocessorTest 
 *
 * @author Sang Venkatraman
 *
 */
class TextPreprocessorTest extends JUnitSuite with Matchers with AssertionsForJUnit {

  @Test
  def testStraightShooter() = {
     val sentences = StanfordNLPService.analyze("I am a straight shooter. I am William Kane. I am like the Dead sea.")
     println(sentences.length)
     val compoundWords = TextPreprocessor.getCompoundWords(sentences)
     println(compoundWords)
  }

}
