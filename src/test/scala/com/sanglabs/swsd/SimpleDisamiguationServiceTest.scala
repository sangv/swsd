package com.sanglabs.swsd

import net.sf.extjwnl.data.POS
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfter, FlatSpec}

/**
 *
 * The SimpleDisamiguationServiceTestSpec 
 *
 * @author Sang Venkatraman
 *
 */
class SimpleDisamiguationServiceTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

  "Plethora of fish at sea" should "have synset options for 3 words" in {
    val options = SimpleDisambiguationService.lookupOptions("Plethora of fish at sea")
    options.size shouldEqual(3)
    options.get(WordAnalysis("Plethora","plethora",POS.NOUN)).get shouldEqual List("plethora#n#1")
    options.get(WordAnalysis("fish","fish",POS.NOUN)).get shouldEqual List("fish#n#1", "fish#n#2", "fish#n#3", "fish#n#4")
    options.get(WordAnalysis("sea","sea",POS.NOUN)).get shouldEqual List("sea#n#1", "sea#n#2", "sea#n#3")
  }

  "Lots of fish near the river bank" should "have synset options for 3 words" in {
    val options = SimpleDisambiguationService.lookupOptions("Lots of fish near the river bank")
    options.size shouldEqual(3)
    options.get(WordAnalysis("fish","fish",POS.NOUN)).get shouldEqual List("fish#n#1", "fish#n#2", "fish#n#3", "fish#n#4")
    options.get(WordAnalysis("river","river",POS.NOUN)).get shouldEqual List("river#n#1")
    options.get(WordAnalysis("bank","bank",POS.NOUN)).get shouldEqual List("bank#n#1", "bank#n#2", "bank#n#3", "bank#n#4", "bank#n#5", "bank#n#6", "bank#n#7", "bank#n#8", "bank#n#9", "bank#n#10")
  }

  "Going to deposit some money at the bank" should "have synset options for 3 words" in {
    val options = SimpleDisambiguationService.lookupOptions("Going to deposit some money at the bank")
    options.size shouldEqual(3)
    options.get(WordAnalysis("deposit","deposit",POS.VERB)).get shouldEqual List("deposit#v#1", "deposit#v#2", "deposit#v#3")
    options.get(WordAnalysis("money","money",POS.NOUN)).get shouldEqual List("money#n#1", "money#n#2", "money#n#3")
    options.get(WordAnalysis("bank","bank",POS.NOUN)).get shouldEqual List("bank#n#1", "bank#n#2", "bank#n#3", "bank#n#4", "bank#n#5", "bank#n#6", "bank#n#7", "bank#n#8", "bank#n#9", "bank#n#10")
  }

}
