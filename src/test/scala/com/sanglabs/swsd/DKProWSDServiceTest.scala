package com.sanglabs.swsd

import net.sf.extjwnl.data.POS
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

/**
 *
 * The SimpleDisamiguationServiceTestSpec 
 *
 * @author Sang Venkatraman
 *
 */
@RunWith(classOf[JUnitRunner])
class DKProWSDServiceTest extends FlatSpec with Matchers with BeforeAndAfter {

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
    val result1 = DKProWSDService.disambiguate(options.keys.toList)
    println(result)

    result.keys.size shouldEqual 11

    result.get("plant").get shouldEqual "plant#n#1"
    result.get("rubber").get shouldEqual "rubber#n#1"
    result.get("full").get shouldEqual "full#a#1"
    result.get("man").get shouldEqual "man#n#1"
    result.get("watering").get shouldEqual "watering#n#4"
    result.get("buy").get shouldEqual "buy#v#1"
    result.get("fake").get shouldEqual "fake#a#2"
    result.get("town").get shouldEqual "town#n#2"
    result.get("plan").get shouldEqual "plan#n#1"
    result.get("earth").get shouldEqual "Earth#n#1"
    result.get("chinese").get shouldEqual "Chinese#a#1"

    assert(result === result1)

  }

  "Beautiful Day" should "return " in {

    val options = SimpleDisambiguationService.lookupOptions(TestText.beautifulDayLyrics)

    val result = DKProWSDService.disambiguate(options.keys.toList)
    println(result.size)
    println(result)

  }

  "Dead Sea" should "return " in {

    val options = SimpleDisambiguationService.lookupOptions(
      """
        |I stood alone, upon the platform in vain
        |The Puerto Ricans they were playing me salsa in the rain
        |With open doors and manual locks
        |In fast food parking lots
        |
        |I headed West, I was a man on the move
        |New York had lied to me, I needed the truth
        |Oh, I need somebody, needed someone I could trust
        |I don't gamble, but if I did I would bet on us
        |
        |Like the Dead Sea
        |You told me I was like the Dead Sea
        |You'll never sink when you are with me
        |Oh, Lord, like the Dead Sea
        |
        |Whoa, I'm like the Dead Sea
        |The finest words you ever said to me
        |Honey can't you see,
        |I was born to be, be your Dead Sea
        |
        |You told me you were good at running away
        |Domestic life, it never suited you like a suitcase
        |You left with just the clothes on your back
        |You took the rest when you took the map
        |
        |Yes, there are times we live for somebody else
        |Your father died and you decided to live
        |It for yourself you felt, you just felt it was time
        |And I'm glad, cause you with cats, that's just not right
        |
        |Like the Dead Sea
        |You told me I was like the Dead Sea
        |You'll never sink when you are with me
        |Oh, Lord, I'm your Dead Sea
        |
        |Whoa, I'm like the Dead Sea
        |The nicest words you ever said to me
        |Honey can't you see
        |I was born to be, be your dead sea
        |
        |I've been down, I've been defeated
        |You're the message, I will heed it.
        |Would you stay,
        |Would you stay the night?
        |
        |Dead Sea,
        |You told me I was like the Dead Sea
        |I never sink when you are with me
        |Oh, Lord, I'm your Dead Sea
        |
        |Whoa, I'm like the Dead Sea
        |The nicest words you ever said to me
        |Honey can't you see
        |I was born to be, be your Dead Sea
        |
        |
      """.stripMargin)

    val result = DKProWSDService.disambiguate(options.keys.toList)
    println(result.size)
    println(result)
  }

  "Fake plastic tree full lyrics" should "return " in {

    val options = SimpleDisambiguationService.lookupOptions(TestText.fakePlasticTrees)

    val result = DKProWSDService.disambiguate(options.keys.toList)
    println(result.size)
    println(result)
  }

}
