package com.sanglabs.swsd

import net.sf.extjwnl.data.{IndexWord, IndexWordSet, Synset}


/**
 *
 * The SimpleDisambiguationService 
 *
 * @author Sang Venkatraman
 *
 */
object SimpleDisambiguationService {

  //connect all possible options (synsets) filtered by pos and maintain a multimap
  //between the words and the possible synsets

  //after all the components are connected, count the connections each one has
  // a) below a certain path length (4)
  // b) without including other of its own synsets

  val dictionary = WordnetDictionaryService.dictionary

  def lookup(words: String) = {

    //todo filter out stop words
    val analyzedWords = StanfordNLPService.analyze(words)

    //handle duplicates by putting them into a map
    for (wordAnalysis <- analyzedWords) {
      val lemma = wordAnalysis.lemma
      println(wordAnalysis)
      val indexWordSet: IndexWordSet = dictionary.lookupAllIndexWords(lemma)
      val iter = indexWordSet.getIndexWordArray.iterator
      while (iter.hasNext) {
        val indexWord: IndexWord = iter.next()
        import scala.collection.JavaConverters._
        for (sense: Synset <- indexWord.getSenses.asScala.filter(_.getPOS.equals(wordAnalysis.pos)))//Keep only the synsets that match the pos defined by stanfordnlp
          println(s"${sense.getPOS} => ${sense.getGloss}")
      }
      println("----------------------------------------")
    }

  }

}

