package com.sanglabs.swsd

import com.dpdearing.nlp.opennlp.OpenNlpToolkit
import grizzled.slf4j.Logger
import org.tartarus.snowball.ext.englishStemmer

import scala.collection.mutable.ListBuffer

/**
 *
 * The TextPreprocessor 
 *
 * @author Sang Venkatraman
 *
 */
object TextPreprocessor {

  val stopWords: List[String] = scala.io.Source.fromFile("data/stopwords.txt").getLines.toList

  val stemmer_en = new englishStemmer

  //FIXME make StanfordNLP and OpenNLP configurable
  val openNlpToolkit = new OpenNlpToolkit

  val logger = Logger[this.type]

  case class Document(text: String)  //terms imply uniqueness while words can be repeated

  def preprocess(text: String): String = {
     //preprocessing new lines

      val paragraphs = splitParagraphs(text)
      val lines: Array[String] = paragraphs map {_.split("\n")} flatten
      val processedLines = lines.filterNot(l => {(l.startsWith("[") && l.endsWith("]")) || l.length < 1}) map {line => {if(!line.matches(".*\\p{P}$")) line + "." else line}}
      processedLines.mkString(" ")
  }

  def splitParagraphs(text: String): Array[String] = {
    val paragraphs = text.split("\n\n")
    paragraphs
  }

  def stemToken(token: String): Option[String] = {
    token.matches("(?i)^[a-z0-9]+(?:[ -]?[a-z0-9]+)*$") match {
      case (true) => {
        stemmer_en.setCurrent(token)
        stemmer_en.stem
        Option(stemmer_en.getCurrent)
      }
      case (false) => None
    }
  }

  def removeStopWordsAndStem(sentence: String) = {
    var stems = List[String]()
    openNlpToolkit.tokenize(sentence).filterNot(stopWords.contains) map {stemToken} foreach {f =>
      f match {
        case Some(x:String) => {stems :+= x}
        case None =>
      }
    }
    stems
  }

  def removeStopwordsAndStemDocument(document: Document) : List[(String,List[String])] = {
    val sentences = getSentences(document.text)
    val sentenceStems = sentences.map(s => (s -> removeStopWordsAndStem(s))).toList
    sentenceStems
  }

  def getSentences(text: String) ={
    openNlpToolkit.detectSentences(preprocess(text)).toList
  }

  def getCompoundWords(sentences: List[Sentence]): List[Sentence] = {
    //compoundify words (nouns) currently 2 at a time -- TODO add support for differently sized compound words
    val compoundedSentences = sentences.map(getCompoundWords(_))
    compoundedSentences
  }

  def getCompoundWords(sentence: Sentence) = {
    val compoundedWords = ListBuffer[WordAnalysis]()
    var index = 0
    for ( (f,s) <- sentence.words zip sentence.words.drop(1) ) {
      logger.trace(f.word + "  " + s.word)
      index+=1
      if (f.pos != null && s.pos != null && f.stanfordPOS.startsWith("NN") && s.stanfordPOS.startsWith("NN") && f.stanfordPOS.equals(s.stanfordPOS)) {
        val compoundWord = StringBuilder.newBuilder.append(f.word).append(" ").append(s.word).toString()
        if (WordNetDictionaryService.getBaseForm(f.pos,compoundWord) != null) {
          compoundedWords += WordAnalysis(compoundWord, WordNetDictionaryService.getBaseForm(f.pos, compoundWord), f.pos, f.stanfordPOS)
        } else {
          compoundedWords += f; if (index == sentence.words.length - 1) compoundedWords += s
        }
      }  else {
        compoundedWords+=f; if(index == sentence.words.length-1) compoundedWords+=s
      }
    }
    Sentence(compoundedWords.toList)
  }
}
