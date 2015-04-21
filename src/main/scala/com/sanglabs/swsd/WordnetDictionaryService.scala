package com.sanglabs.swsd

import java.io.FileInputStream

import grizzled.slf4j.Logger
import net.sf.extjwnl.data.{IndexWord, IndexWordSet, POS, Synset}
import net.sf.extjwnl.dictionary.Dictionary

import scala.collection._


/**
 *
 * The WordNetService provides access to WordNet using the extJWNL library
 *
 * @author Sang Venkatraman
 *
 */
object WordNetDictionaryService {

  //connect all possible options (synsets) filtered by pos and maintain a multimap
  //between the words and the possible synsets

  //after all the components are connected, count the connections each one has
  // a) below a certain path length (4)
  // b) without including other of its own synsets



  val dictionary = Dictionary.getInstance(new FileInputStream("data/file_properties.xml"))

  val posmap: Map[POS,Char] = Map(POS.NOUN -> 'n',POS.VERB -> 'v',POS.ADJECTIVE -> 'a', POS.ADVERB -> 'r')

  val logger = Logger[this.type]

  //using stopwords from http://www.ranks.nl/stopwords
  val stopWords: List[String] = scala.io.Source.fromFile("data/stopwords.txt").getLines.toList

  def lookupOptions(words: String): mutable.Map[WordAnalysis,List[String]] = {

    val sentences = StanfordNLPService.analyze(words)

    val compoundedSentences = TextPreprocessor.getCompoundWords(sentences)


    //todo filter out stop words
    val filteredSentences = compoundedSentences map (sentence => Sentence(sentence.words.filterNot( (w:WordAnalysis) => (w.lemma != null && (stopWords.contains(w.lemma.toLowerCase) || stopWords.contains(w.word.toLowerCase))))))
    val mapOfOptions = scala.collection.mutable.Map[WordAnalysis,List[String]]()

    //handle duplicates by putting them into a map
    var options = List[String]()
    for(sentence <- filteredSentences) {
      for (wordAnalysis <- sentence.words filter (_.lemma != null)) {
        val lemma = wordAnalysis.lemma

        val indexWordSet: IndexWordSet = dictionary.lookupAllIndexWords(lemma)
        val iter = indexWordSet.getIndexWordArray.iterator
        while (iter.hasNext) {
          val indexWord: IndexWord = iter.next()
          import scala.collection.JavaConverters._

          var index = 0

          //Keep only the synsets that match the pos defined by stanfordnlp -- TODO make this configurable
          for (sense: Synset <- indexWord.getSenses.asScala.filter(_.getPOS.equals(wordAnalysis.pos))) {
            index += 1
            val synsetForm = s"${indexWord.getLemma}#${posmap.get(indexWord.getPOS).get}#$index"
            logger.debug(s"$synsetForm => ${sense.getGloss}")
            options = options :+ synsetForm
          }

        }
        logger.debug(s"$wordAnalysis => ${options.mkString(", ")}")
        mapOfOptions += (wordAnalysis -> options)
        options = List[String]()
      }
    }
    mapOfOptions
  }

  def getSynsetAt(pos: String, offset: Long): Synset = {
    dictionary.getSynsetAt(POS.getPOSForKey(pos), offset)
  }

  def getSynsetAt(pos: POS, offset: Long): Synset = {
    dictionary.getSynsetAt(pos, offset)
  }

  def indexWord(synsetPOS: POS, synsetWord:String) = {
    dictionary.getIndexWord(synsetPOS, synsetWord)
  }

  def getBaseForm(pos: POS, word: String): String = {
    val indexWord = dictionary.getMorphologicalProcessor.lookupBaseForm(pos, word.toLowerCase)
    if (indexWord != null) indexWord.getLemma else null
  }



}

