package com.sanglabs.swsd

import net.sf.extjwnl.data.POS
import org.scalatest.{Matchers, FunSpec}

/**
 *
 * The WordnetDictionaryServiceTest 
 *
 * @author Sang Venkatraman
 *
 */
class WordnetDictionaryServiceTest extends FunSpec with Matchers {

  it ("should be able to get base form") {
     WordnetDictionaryService.getBaseForm(POS.NOUN,"Hard disk") shouldEqual  "hard disk"
  }

}
