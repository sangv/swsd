package com.sanglabs.swsd

import grizzled.slf4j.Logger
import net.sf.extjwnl.data.{IndexWord, IndexWordSet, POS, Synset}

import scala.collection.mutable.ListBuffer


/**
 *
 * The SimpleDisambiguationService 
 *
 * @author Sang Venkatraman
 *
 */
object SimpleDisambiguationService {

  //Nice to have: perform word compoundification and remove stop words
  //connect all possible options (synsets) filtered by pos and maintain a multimap
  //between the words and the possible synsets

  //after all the components are connected, count the connections each one has
  // a) below a certain path length (4)
  // b) without including other of its own synsets

  val dictionary = WordnetDictionaryService.dictionary

  val posmap: Map[POS,Char] = Map(POS.NOUN -> 'n',POS.VERB -> 'v',POS.ADJECTIVE -> 'a', POS.ADVERB -> 'r')

  val logger = Logger[this.type]

  //using stopwords from http://www.ranks.nl/stopwords
  val stopWords: List[String] = scala.io.Source.fromFile("data/stopwords.txt").getLines.toList

  def lookupOptions(words: String): scala.collection.mutable.Map[WordAnalysis,List[String]] = {

    val sentences = StanfordNLPService.analyze(words)

    //compoundify words (nouns) currently 2 at a time
    //for ( (f,s) <- latlong zip latlong.drop(1) ) println (s"f: $f, s: $s")
    val compoundedSentences = sentences map (sentence => {
      val compoundedWords = ListBuffer[WordAnalysis]()
      var index = 0
      for ( (f,s) <- sentence.words zip sentence.words.drop(1) ) {
        println(f.word + "  " + s.word)
        index+=1
        if (f.pos != null && s.pos != null && f.stanfordPOS.startsWith("NN") && s.stanfordPOS.startsWith("NN") && f.stanfordPOS.equals(s.stanfordPOS)) {
          val compoundWord = StringBuilder.newBuilder.append(f.word).append(" ").append(s.word).toString()
          if (WordnetDictionaryService.getBaseForm(f.pos,compoundWord) != null) {
            compoundedWords += WordAnalysis(compoundWord, WordnetDictionaryService.getBaseForm(f.pos, compoundWord), f.pos, f.stanfordPOS)
          } else {
            //TODO refactor
            compoundedWords += f;
            if (index == sentence.words.length - 1) compoundedWords += s
          }
        }  else {
          compoundedWords+=f; if(index == sentence.words.length-1) compoundedWords+=s
        }
      }
      Sentence(compoundedWords.toList)
    })


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
        logger.info(s"$wordAnalysis => ${options.mkString(", ")}")
        mapOfOptions += (wordAnalysis -> options)
        options = List[String]()
      }
    }
    mapOfOptions
  }


}

