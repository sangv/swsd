package com.sanglabs.swsd

import org.junit.Assert._
import org.junit.Test
import org.scalatest.junit.{JUnitSuite, ShouldMatchersForJUnit}

import scala.collection.immutable.ListMap

/**
 *
 * The TextRankImplText 
 *
 * @author Sang Venkatraman
 *
 */
class TextRankImplTest extends JUnitSuite with ShouldMatchersForJUnit {


  @Test
  def test1() {
    val result: ListMap[WordAnalysis,Double] = TextRankImpl.calculate("Cassandra is a NoSQL database that provides extremely fast writes (and reads with the correct design). It also has availability and partitioning built into its design with tunable consistency. To achieve distribution as a fundamental construct, Cassandra recommends and imposes certain querying limitations. Cassandra like other NoSQL databases also encourages denormalization (allowing data duplication) and enforces a schema design that is in keeping with how the data would be queried.\n\nWhen building a system of record, it is not always possible to foresee all kinds of queries that need to be executed on the data. But, because we want to leverage all the built in distribution and scaling capabilities of Cassandra, it makes sense for the system of record data to live in Cassandra (especially as the volume of data is high or bound to increase).")

    println(result.values.foldLeft(0.0)(_ + _))

    val topWords = result.take(3).map(_._1.word).toList
    assertEquals("Cassandra", topWords(0))
    assertEquals("data", topWords(1))   //0.10759560207132277
    assertEquals("design", topWords(2))  //0.07028082988453872
     // 0.06832425956935244


  }

  @Test
  def test2() {
    val result: ListMap[WordAnalysis,Double] = TextRankImpl.calculate(TestText.beautifulDayLyrics)

    println(result.values.foldLeft(0.0)(_ + _))

    val topWords = result.take(5).map(_._1.word).toList
    println(topWords)


  }

}
