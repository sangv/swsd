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
class SimpleDisamiguationServiceTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

  /*"Plethora of fish at sea" should "have synset options for 3 words" in {
    val options = SimpleDisambiguationService.lookupOptions("Plethora of fish at sea")
    options.size shouldEqual(3)
    options.get(WordAnalysis("Plethora","plethora",POS.NOUN,"NN")).get shouldEqual List("plethora#n#1")
    options.get(WordAnalysis("fish","fish",POS.NOUN,"NN")).get shouldEqual List("fish#n#1", "fish#n#2", "fish#n#3", "fish#n#4")
    options.get(WordAnalysis("sea","sea",POS.NOUN,"NN")).get shouldEqual List("sea#n#1", "sea#n#2", "sea#n#3")
  }

  "Lots of fish near the river bank" should "have synset options for 2 words after compoundification" in {
    val options = SimpleDisambiguationService.lookupOptions("Lots of fish near the river bank")
    options.size shouldEqual(2)
    options.get(WordAnalysis("fish","fish",POS.NOUN,"NN")).get shouldEqual List("fish#n#1", "fish#n#2", "fish#n#3", "fish#n#4")
    options.get(WordAnalysis("river bank","riverbank",POS.NOUN,"NN")).get shouldEqual List("riverbank#n#1")
  }

  "Going to deposit some money at the bank" should "have synset options for 3 words" in {
    val options = SimpleDisambiguationService.lookupOptions("Going to deposit some money at the bank")
    options.size shouldEqual(3)
    options.get(WordAnalysis("deposit","deposit",POS.VERB,"VB")).get shouldEqual List("deposit#v#1", "deposit#v#2", "deposit#v#3")
    options.get(WordAnalysis("money","money",POS.NOUN,"NN")).get shouldEqual List("money#n#1", "money#n#2", "money#n#3")
    options.get(WordAnalysis("bank","bank",POS.NOUN,"NN")).get shouldEqual List("bank#n#1", "bank#n#2", "bank#n#3", "bank#n#4", "bank#n#5", "bank#n#6", "bank#n#7", "bank#n#8", "bank#n#9", "bank#n#10")
  }

  "New York" should "be compounded" in {
    val options = SimpleDisambiguationService.lookupOptions("You live in New York")
    options.size shouldEqual(2)
    options.get(WordAnalysis("live","live",POS.VERB,"VBP")).get shouldEqual List("live#v#1", "live#v#2", "live#v#3", "live#v#4", "live#v#5", "live#v#6", "live#v#7")
    options.get(WordAnalysis("New York","new york",POS.NOUN,"NNP")).get shouldEqual List("new york#n#1", "new york#n#2", "new york#n#3")
    //options.get(WordAnalysis("bank","bank",POS.NOUN)).get shouldEqual List("bank#n#1", "bank#n#2", "bank#n#3", "bank#n#4", "bank#n#5", "bank#n#6", "bank#n#7", "bank#n#8", "bank#n#9", "bank#n#10")
  }*/

  "Plethora of fish at sea" should "have 1 simple disambiguation" in {
    val options = scala.collection.mutable.Map(WordAnalysis("Plethora","plethora",POS.NOUN,"NN") -> List("plethora#n#1"),
      WordAnalysis("fish","fish",POS.NOUN,"NN") -> List("fish#n#1", "fish#n#2", "fish#n#3", "fish#n#4"),
      WordAnalysis("sea","sea",POS.NOUN,"NN") -> List("sea#n#1", "sea#n#2", "sea#n#3"))
    val result =  Neo4JGraphService.disambiguate(options)
    println(result.mkString(", "))
    result.size shouldEqual(26)
    result.take(5) foreach (println)
    //result.get(WordAnalysis("Plethora","plethora",POS.NOUN,"NN")).get shouldEqual("plethora#n#1")
  }

  /*"Test Fake Plastic tree" should "return " in {
    val options = SimpleDisambiguationService.lookupOptions("Her green plastic watering can\nFor her fake Chinese rubber plant\nIn the fake plastic earth\nThat she bought from a rubber man\nIn a town full of rubber plans\nTo get rid of itself")
    val result = WordnetGraphService.disambiguate(options)
    println(result.mkString(", "))
  } */

}
