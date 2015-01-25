package com.sanglabs.swsd

import net.sf.extjwnl.data.POS
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfter, FlatSpec}

/**
 *
 * The SimpleDisamiguationServiceTestSpec 
 *
 * @author Sang Venkatraman
 *
 */
@RunWith(classOf[JUnitRunner])
class DKProWSDServiceTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

  "Going to deposit some money at the bank" should "disambiguate correctly" in {
    var moneybanklist: List[WordAnalysis] = List[WordAnalysis]()
    moneybanklist :+= WordAnalysis("acquirer","acquirer",POS.NOUN,"NN")
    moneybanklist :+= WordAnalysis("bank","bank",POS.NOUN,"NN")
    val result1 = DKProWSDService.disambiguate(moneybanklist)

    result1.get("acquirer").get shouldEqual "acquirer#n#2"
    result1.get("bank").get shouldEqual "bank#n#2"

    var riverbanklist: List[WordAnalysis] = List[WordAnalysis]()
    riverbanklist :+= WordAnalysis("river","river",POS.NOUN,"NN")
    riverbanklist :+= WordAnalysis("bank","bank",POS.NOUN,"NN")
    val result2 = DKProWSDService.disambiguate(riverbanklist)

    result2.get("river").get shouldEqual "river#n#1"
    result2.get("bank").get shouldEqual "bank#n#1"

  }

  "Test Fake Plastic tree" should "return " in {
    val options = SimpleDisambiguationService.lookupOptions("Her green plastic watering can\nFor her fake Chinese rubber plant\nIn the fake plastic earth\nThat she bought from a rubber man\nIn a town full of rubber plans\nTo get rid of itself")

    val result = DKProWSDService.disambiguate(options.keys.toList)

    println(result)

    result.keys.size shouldEqual 11

    result.get("plant").get shouldEqual "plant#n#1"
    result.get("rubber").get shouldEqual "rubber#n#1"
    result.get("full").get shouldEqual "full#a#1"
    result.get("man").get shouldEqual "man#n#1"
    result.get("watering").get shouldEqual "watering#n#4"
    result.get("buy").get shouldEqual "buy#v#1"
    result.get("fake").get shouldEqual "fake#a#2"
    result.get("plan").get shouldEqual "plan#n#1"
    result.get("earth").get shouldEqual "Earth#n#1"
    result.get("chinese").get shouldEqual "Chinese#a#1"

  }

}
