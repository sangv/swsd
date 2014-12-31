package com.sanglabs.swsd

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfter, FlatSpec}

/**
 *
 * The WordnetGraphServiceTestSpec tests the WordnetGraphService
 *
 * @author Sang Venkatraman
 *
 */
class WordnetGraphServiceTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

  "Querying the graph database for hypernym trees" should "return correct parent offsets" in {
    WordnetGraphService.getHypernymTree("travel#v#1") shouldEqual(1835496)
    WordnetGraphService.getHypernymTree("bank#n#1") shouldEqual(1740)
    WordnetGraphService.getHypernymTree("actually#r#1") shouldEqual(149510)
    WordnetGraphService.getHypernymTree("genetic#a#1") shouldEqual(1314537)
  }

}
