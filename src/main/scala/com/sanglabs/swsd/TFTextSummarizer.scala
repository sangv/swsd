package com.sanglabs.swsd

import com.sanglabs.swsd.TFIDFCalculator.Document
import grizzled.slf4j.Logger

/**
 *
 * The TFIDFTextSummarizer 
 *
 * @author Sang Venkatraman
 *
 */
object TFIDFCalculator {

  val logger = Logger[this.type]

  def calculate(documents: List[Document]): Map[Document, List[(String,Double)]] = {

    var map = Map[Document, List[(String,Double)]]()

    val terms = documents flatMap {_.words} toSet
    val documentTermFrequencies = documents map termFrequency

    val idfs: Map[String,Double] = {
      var idfMap = Map[String,Double]()
      terms map (term => {
        val numberOfDocumentsWithTerm = documentTermFrequencies count (_.termFrequencies.get(term).getOrElse(0.0) > 0.0)
        val idf: Double = 1.0 + Math.log(documentTermFrequencies.length * 1.0 / numberOfDocumentsWithTerm)
        idfMap += (term -> idf)
      })
      idfMap
    }
    
    for (documentTermFrequency <- documentTermFrequencies){
      var innerMap = Map[String,Double]()
      for (term <- documentTermFrequency.termFrequencies.keySet) {
        val tf = documentTermFrequency.termFrequencies.get(term).getOrElse(0.0)
        val idf = idfs.get(term).get
        val tfidf = tf * idf
        innerMap += (term -> tfidf)
      }
      val sortedTerms = innerMap.toList sortBy {_._2}
      map += (documentTermFrequency.document -> sortedTerms.reverse)
    }
    map
  }

  def termFrequency(document: Document): DocumentTermFrequency = {
    val countMap: Map[String,Int] = document.words groupBy(_.toString) mapValues(_.size)
    val docLength = document.words.length
    var map = Map[String,Double]()
    for(term <- countMap.keySet) {
      map += (term -> countMap.get(term).getOrElse(0) * 1.0 / docLength)
    }
    val result = DocumentTermFrequency(document,map)

    logger.debug(result)
    result
  }

  case class DocumentTermFrequency(document: Document, termFrequencies: Map[String,Double])
  case class Document(words: List[String])  //terms imply uniqueness while words can be repeated

}

object TFTextSummarizer {

  val stopWords: List[String] = scala.io.Source.fromFile("data/stopwords.txt").getLines.toList

  val logger = Logger[this.type]

  def sortSentencesByTF(sentences: List[List[String]]): Seq[(String,Double)] = {

    val document = Document(sentences.flatten)
    val documentTermFrequency = TFIDFCalculator termFrequency(document)
    var scoreMap = Map[String,Double]() //TODO replace String by sentence because that is what it means

    for(sentence <- sentences) {
      var sentenceScore: Double = 0.0
      for (word <- sentence) {
        sentenceScore += documentTermFrequency.termFrequencies.get(word).getOrElse(0.0)
      }
      //TODO refactor the way sentence is being built
      scoreMap += (sentence.mkString(" ") -> sentenceScore/sentence.mkString(" ").length) // Normalizing the sentence score so that long sentences don't automatically get selected
    }

    val map = scoreMap.toSeq.sortWith(_._2 > _._2)

    logger.debug(map)
    map

  }

  def topSentences(sentences: List[List[String]], numberOfSentence: Int = 2): List[String] ={
     val top = sortSentencesByTF(sentences)
     top map (_._1) take(numberOfSentence) toList
  }

  def summarize(text: String, sentences: Int = 2) = {
    //call the sentence parser and split to sentences (maybe use opennlp sentence detecter)
    val rawSentences = StanfordNLPService.sentences(text)
    val sentences = StanfordNLPService.analyze(text)

    val filteredSentences: List[List[String]] = sentences map(s => s.words filterNot(w => stopWords.contains(w.word)) map (_.word))

    if(rawSentences.length != sentences.length)
      throw new RuntimeException("Sentence Tokenization did not work as expected")

    val summarySentences = topSentences(filteredSentences)
    logger.debug(summarySentences)
    summarySentences
  }

}
