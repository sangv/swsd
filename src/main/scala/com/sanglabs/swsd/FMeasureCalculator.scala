package com.sanglabs.swsd

import opennlp.tools.util.eval.FMeasure

/**
 *
 * The FMeasureCalculator 
 *
 * @author Sang Venkatraman
 *
 */

case class FMeasureScore(precision: Double, recall: Double, fMeasure: Double)
object FMeasureCalculator {

  def calculate(expected: Array[AnyRef], predictions: Array[AnyRef]) = {
    val fMeasure = new FMeasure()
    fMeasure.updateScores(expected,predictions)
    FMeasureScore(fMeasure.getPrecisionScore ,fMeasure.getRecallScore,if(fMeasure.getPrecisionScore == 0.0 && fMeasure.getRecallScore == 0) 0.0 else fMeasure.getFMeasure)
  }

  def avgFMeasure(expectedAndPredicted: List[(Array[AnyRef],Array[AnyRef])]) = {
    val fMeasureScores = expectedAndPredicted map {f => calculate(f._1,f._2)}
    var fMeasure = 0.0
    var precision = 0.0
    var recall = 0.0
    fMeasureScores foreach {f => {println(f); fMeasure += f.fMeasure; precision += f.precision; recall += f.recall}}
    val instances = expectedAndPredicted.length
    FMeasureScore(precision/instances,recall/instances,fMeasure/instances)
  }


}
