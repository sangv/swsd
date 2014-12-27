package com.sanglabs.swsd

import java.io.FileInputStream

import net.sf.extjwnl.data.{POS, Synset}
import net.sf.extjwnl.dictionary.Dictionary
import org.slf4j.{Logger, LoggerFactory}

/**
 *
 * The WordnetDictionaryService uses the extjwnl library to lookup wordnet relations
 *
 * @author Sang Venkatraman
 *
 */
object WordnetDictionaryService {

  val dictionary = Dictionary.getInstance(new FileInputStream("data/file_properties.xml"))

  private val LOGGER: Logger = LoggerFactory.getLogger(WordnetDictionaryService.getClass)

  def getSynsetAt(pos: String, offset: Long): Synset = {
    dictionary.getSynsetAt(POS.getPOSForKey(pos), offset)
  }

  def indexWord(synsetPOS: POS, synsetWord:String ) = {
    dictionary.getIndexWord(synsetPOS, synsetWord)
  }

}
