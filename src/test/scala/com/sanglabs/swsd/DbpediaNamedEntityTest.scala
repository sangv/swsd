package com.sanglabs.swsd

import junit.framework.TestCase

/**
 *
 * The DbpediaNamedEntityTest 
 *
 * @author Sang Venkatraman
 *
 */
class DbpediaNamedEntityTest extends TestCase {

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

  /*@Test
  def testNerSpots() {
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

  @Test
  def testFMeasureScore() {
    val result = FMeasureCalculator.calculate(Array("Concept1"),Array("Concept1","Concept2"))
    println(result)
  } */

  /*@Test
  def testIsBrand() {
    val isBrand = DbpediaSpotlightService.isBrand("http://dbpedia.org/resource/Fendi")
    assert(isBrand)
  }

  @Test
  def testSong(): Unit = {
    DbpediaSpotlightService.getEntities(TestText.beautifulDayLyrics)
    println("Done")
  }

  @Test
  def testNERSong2(): Unit = {
    DbpediaSpotlightService.getEntities(NERTest.Fashion_LadyGaga)
    println("Done")
  }

  @Test
  def testNERSong3(): Unit = {
    DbpediaSpotlightService.getEntities(NERTest.ItsAllAboutThePentiums_WeirdAl)
    println("Done")
  }*/

  def testNERSongs() = {
    val results = DbpediaSpotlightService.getEntities(TextPreprocessor.preprocess(NERTest.ItsAllAboutThePentiums_WeirdAl))
    println(results)
  }

}
