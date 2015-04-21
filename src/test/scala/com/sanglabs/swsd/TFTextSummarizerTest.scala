package com.sanglabs.swsd

import com.sanglabs.swsd.TextPreprocessor.Document
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
  def testSummary(): Unit = {
    val tfTopSentences = TFTextSummarizer.topSentences(Document(TestText.apache_cassandra_wikipedia),3)
    assert(tfTopSentences.head == "Cassandra also places a high value on performance.")
    val textRankTopSentences = TextRankImpl.topSentences(TestText.apache_cassandra_wikipedia,3)
    assert(textRankTopSentences.head == "Cassandra offers robust support for clusters spanning multiple datacenters, with asynchronous masterless replication allowing low latency operations for all clients.")
  }

}
