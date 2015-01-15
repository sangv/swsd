// Copyright (c) 2012 Kenzie Lane Mosaic, LLC. All rights reserved.
package com.sanglabs.swsd

import java.net.URL

import de.tudarmstadt.ukp.dkpro.wsd.algorithm.{MostFrequentSenseBaseline, WSDAlgorithmIndividualBasic, WSDAlgorithmIndividualPOS}
import de.tudarmstadt.ukp.dkpro.wsd.si.POS
import de.tudarmstadt.ukp.dkpro.wsd.si.wordnet.WordNetSynsetSenseInventory
import org.junit.Assert._
import org.junit.Test
import org.scalatest.junit.{JUnitSuite, ShouldMatchersForJUnit}

/**
 *
 * The MostFrequentWordNetSenseTest 
 *
 * @author Sang Venkatraman
 *
 */
class MostFrequentWordNetSenseTest extends JUnitSuite with ShouldMatchersForJUnit {


  val extJWNLPropertiesFile: String = "file:///Users/sang/Temp/swsd/data/file_properties.xml"

  val inventory: WordNetSynsetSenseInventory = new WordNetSynsetSenseInventory(new URL(extJWNLPropertiesFile))


  @Test
  def mfsBaselineTest() {
    val wsdAlgo: WSDAlgorithmIndividualBasic = new MostFrequentSenseBaseline(inventory)
    assertEquals(1, wsdAlgo.getDisambiguation("bank").size)
    assertEquals(1, wsdAlgo.getDisambiguation("bat").size)
    assertEquals(1, wsdAlgo.getDisambiguation("test").size)
    assertEquals(null, wsdAlgo.getDisambiguation("humpelgrpf"))

    assertEquals(1.0, wsdAlgo.getDisambiguation("bank").get("09213565n"), 0.000001)
    assertEquals(1.0, wsdAlgo.getDisambiguation("bat").get("01412912v"), 0.000001)
    assertEquals(1.0, wsdAlgo.getDisambiguation("test").get("02531625v"), 0.000001)
  }

  @Test
  def mfsWithPOSBaselineTest {

    val wsdAlgo: WSDAlgorithmIndividualPOS = new MostFrequentSenseBaseline(inventory)

    assertEquals(1, wsdAlgo.getDisambiguation("bank", POS.NOUN).size)
    assertEquals(1, wsdAlgo.getDisambiguation("bat", POS.NOUN).size)
    assertEquals(1, wsdAlgo.getDisambiguation("test", POS.NOUN).size)
    assertEquals(null, wsdAlgo.getDisambiguation("humpelgrpf", POS.NOUN))
    assertEquals(1.0, wsdAlgo.getDisambiguation("bank", POS.NOUN).get("09213565n"), 0.000001)
    assertEquals(1.0, wsdAlgo.getDisambiguation("bat", POS.NOUN).get("02139199n"), 0.000001)
    assertEquals(1.0, wsdAlgo.getDisambiguation("test", POS.NOUN).get("05799212n"), 0.000001)
  }
}
