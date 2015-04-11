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
    FMeasureScore(fMeasure.getPrecisionScore,fMeasure.getRecallScore,fMeasure.getFMeasure)
  }


}
