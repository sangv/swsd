package com.sanglabs.swsd

import com.sanglabs.swsd.SentiWordNetService.Sentiment._
import org.junit.Test

/**
 *
 * The ConceptExtractionServiceTest
 *
 * @author Sang Venkatraman
 *
 */
class ConceptExtractionServiceTest {

  val sentiWordNetService = new SentiWordNetService


  @Test
  def testConceptPolarity(): Unit = {
      val text = "They are really happy to be here."
      val options = WordNetDictionaryService.lookupOptions(text)
      println(options)
      val wsds = DKProWSDService.disambiguate(options.keys.toList)
      println(wsds)
      val result = wsds.values map {sentiWordNetService.extract}
      assert(result.size == 1)
      assert(result.toList.contains(STRONG_POSITIVE))
  }

  @Test
  def testConceptExtraction1(): Unit = {
    val cnnMoneyExcerpt = "The junk bond market will soon face a double whammy: higher interest rates and a \"wall\" of maturities. " +
      "The Fed is expected to begin raising rates later this year or early next. " +
      "That's not very long before investors will start focusing on the combined $600 billion of junk bonds set to mature in 2018 and 2019."

    val bankCreditUnion = "What is the difference between a bank and a credit union?"

    val cnnLoveExcerpt = "We tend to think of love in the same breath as loved ones. When you take these to be only your innermost circle of family and friends, you inadvertently and severely constrain your opportunities for health, growth and well-being. " +
      "\n\nIn reality, you can experience micro-moments of connection with anyone -- whether your soul mate or a stranger. " +
      "So long as you feel safe and can forge the right kind of connection, the conditions for experiencing the emotion of love are in place."


    val cnnMoneyExcerptConcepts: List[String] = ConceptExtractionService.extract(cnnMoneyExcerpt).map(_._1).toList
    assert(cnnMoneyExcerptConcepts.size == 1)
    assert(cnnMoneyExcerptConcepts.contains("money"))
  }


}
