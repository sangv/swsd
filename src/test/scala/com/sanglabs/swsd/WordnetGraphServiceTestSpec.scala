package com.sanglabs.swsd

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfter, FunSpec}

/**
 *
 * The WordnetGraphServiceTestSpec tests the WordnetGraphService
 *
 * @author Sang Venkatraman
 *
 */
class WordnetGraphServiceTestSpec extends FunSpec with ShouldMatchers with BeforeAndAfter {

  it("Test querying the graph database for hypernyms and synsets") {
    WordnetGraphService.getHypernymTree("swim#v#1") shouldEqual(1835496)
    WordnetGraphService.getHypernymTree("eye#n#1") shouldEqual(1740)
    WordnetGraphService.getHypernymTree("actually#r#1") shouldEqual(149510)
    WordnetGraphService.getHypernymTree("genetic#a#1") shouldEqual(1314537)

    WordnetGraphService.getHypernymTree("yahweh#n#1")  //TODO add assertions
  }

}
