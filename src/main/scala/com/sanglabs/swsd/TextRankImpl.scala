package com.sanglabs.swsd

import scala.collection.immutable.ListMap

/**
 *
 * The TextRankImpl
 *
 * @author Sang Venkatraman
 *
 */

object TextRankImpl {



  //Use n*n similarity matrix to calculate similarity across getSentences and aggregate the results
  def topSentences(text: String, numberOfSentences: Int = 2): List[(String,Double)] = {
    val sentences = TextPreprocessor.getSentences(text)

    var sentenceStems = Map[String,List[String]]()
    for(sentence <- sentences){
      sentenceStems += (sentence -> TextPreprocessor.removeStopWordsAndStem(sentence))
    }

    val stemArray = sentenceStems.values.toList
    val scoresMatrix = Array.ofDim[Double](sentenceStems.size, sentenceStems.size)
    var sentenceScores = Map[Int,Double]()
    for(i <- 0 until stemArray.size){
      var score = 0.0
      for(j <- 0 until stemArray.size) {
         if(i == j){
           scoresMatrix(i)(j) = 0.0 //explicitly making a sentence not match itself, duh
         } else {
           scoresMatrix(i)(j) = stemArray(i).intersect(stemArray(j)).length*1.0/stemArray(i).length
           score += scoresMatrix(i)(j)
         }
      }
      sentenceScores += (i -> score)
    }

    val indexes = ListMap(sentenceScores.toList.sortBy{_._2}:_*).toList.reverse take(numberOfSentences)
    val results: List[(String,Double)] = indexes.map(s => (sentences(s._1),s._2)).toList
    results
  }

}
