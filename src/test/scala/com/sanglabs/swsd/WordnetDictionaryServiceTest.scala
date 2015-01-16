package com.sanglabs.swsd

import net.sf.extjwnl.data.POS
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

/**
 *
 * The WordnetDictionaryServiceTest 
 *
 * @author Sang Venkatraman
 *
 */
class WordnetDictionaryServiceTest extends FunSpec with ShouldMatchers {

  it ("should be able to get base form") {
     WordnetDictionaryService.getBaseForm(POS.NOUN,"Hard disk") shouldEqual  "hard disk"
  }

}
