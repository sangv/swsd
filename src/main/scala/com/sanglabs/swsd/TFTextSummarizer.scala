package com.sanglabs.swsd

import com.sanglabs.swsd.TextPreprocessor.Document
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

    val terms = documents flatMap {d => TextPreprocessor.removeStopwordsAndStemDocument(d).map(_._2).flatten} toSet
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
    val terms = TextPreprocessor.removeStopwordsAndStemDocument(document).map(_._2).flatten
    val countMap: Map[String,Int] = terms groupBy(_.toString) mapValues(_.size)
    val docLength = terms.length
    var map = Map[String,Double]()
    for(term <- countMap.keySet) {
      map += (term -> countMap.get(term).getOrElse(0) * 1.0 / docLength)
    }
    val result = DocumentTermFrequency(document,map)

    logger.debug(result)
    result
  }

  case class DocumentTermFrequency(document: Document, termFrequencies: Map[String,Double])

}

object TFTextSummarizer {

  val stopWords: List[String] = scala.io.Source.fromFile("data/stopwords.txt").getLines.toList

  val logger = Logger[this.type]

  def sortSentencesByTF(document: Document): Seq[(String,Double)] = {

    val sentenceStems = TextPreprocessor.removeStopwordsAndStemDocument(document)
    val sentenceStemsMap = sentenceStems.toMap
    val documentTermFrequency = TFIDFCalculator termFrequency(document)
    var scoreMap = Map[String,Double]() //TODO replace String by sentence because that is what it means

    for(sentence <- sentenceStemsMap.keySet) {
      var sentenceScore: Double = 0.0
      for (term <- sentenceStemsMap.get(sentence).get) {
        sentenceScore += documentTermFrequency.termFrequencies.get(term).getOrElse(0.0)
      }
      //TODO refactor the way sentence is being built
      scoreMap += (sentence -> sentenceScore/sentence.mkString(" ").length) // Normalizing the sentence score so that long getSentences don't automatically get selected
    }

    val map = scoreMap.toSeq.sortWith(_._2 > _._2)

    logger.debug(map)
    map

  }

  def topSentences(document: Document, numberOfSentence: Int = 2): List[String] ={
     val top = sortSentencesByTF(document)
     top map (_._1) take(numberOfSentence) toList
  }
}
