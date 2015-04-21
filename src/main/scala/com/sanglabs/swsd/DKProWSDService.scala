package com.sanglabs.swsd

import de.tudarmstadt.ukp.dkpro.wsd.graphconnectivity.algorithm.DegreeCentralityWSD
import de.tudarmstadt.ukp.dkpro.wsd.si.POS
import de.tudarmstadt.ukp.dkpro.wsd.{Pair, UnorderedPair}
import edu.uci.ics.jung.graph.Graph

import scala.collection.JavaConverters._
import scala.collection.immutable.Map

/**
 *
 * The DKProWSDService 
 *
 * @author Sang Venkatraman
 *
 */
object DKProWSDService extends JungGraphConnectivityService {

  val wsdAlgorithm: DegreeCentralityWSD = new DegreeCentralityWSD(inventory)

  val SenseIdRegex =  "([0-9]+)([n|v|a|r])".r

  val minDegree = 1

  val posConverter = Map[net.sf.extjwnl.data.POS,de.tudarmstadt.ukp.dkpro.wsd.si.POS](
    net.sf.extjwnl.data.POS.ADJECTIVE -> de.tudarmstadt.ukp.dkpro.wsd.si.POS.ADJ,
    net.sf.extjwnl.data.POS.NOUN -> de.tudarmstadt.ukp.dkpro.wsd.si.POS.NOUN,
    net.sf.extjwnl.data.POS.VERB -> de.tudarmstadt.ukp.dkpro.wsd.si.POS.VERB,
    net.sf.extjwnl.data.POS.ADVERB -> de.tudarmstadt.ukp.dkpro.wsd.si.POS.ADV
  )

  def synsetFormatForSenseId(senseId: String): String = {
    val result = WordNetDictionaryService.dictionary.getWordBySenseKey(senseId)
    val word = result.getLemma + "#" + result.getPOS.getKey + "#" + result.getIndex
    word.replaceAll(" ","_").toLowerCase
  }

  def synsetFormatForSenseIdWithGloss(senseId: String): (String,String) = {
    val SenseIdRegex(offset,pos) = inventory.getWordNetSynsetAndPos(senseId)

    val synset = WordNetDictionaryService.getSynsetAt(pos,offset.toLong)

    val result = WordNetDictionaryService.dictionary.getWordBySenseKey(senseId)
    val word = result.getLemma + "#" + result.getPOS.getKey + "#" + result.getIndex
    (word.replaceAll(" ","_").toLowerCase,synset.getGloss)
  }


  // Bind the visualizer to the algorithm
  wsdAlgorithm.setGraphVisualizer(graphVisualizer)
  wsdAlgorithm.setSearchDepth(4)

  def rawDisambiguate(text: List[WordAnalysis], useOwn: Boolean = false): Map[String,String] = {


    val sentence: java.util.Collection[Pair[String, de.tudarmstadt.ukp.dkpro.wsd.si.POS]] = new java.util.ArrayList[Pair[String, de.tudarmstadt.ukp.dkpro.wsd.si.POS]]

    //val baseFormText = text map ( w => WordAnalysis(w.word,WordNetDictionaryService.getBaseForm(w.pos,w.lemma),w.pos,w.stanfordPOS))
    for(w <- text) {
        sentence.add(new Pair[String, POS](WordNetDictionaryService.getBaseForm(w.pos,w.word), posConverter.get(w.pos).get))
        //TODO reset lemma in WordAnalysis as well
    }

    val dabMap: Map[Pair[String, POS], Map[String, Double]] = useOwn match {
      case true => ownDisambiguation(sentence)
      case false => wsdAlgorithm.getDisambiguation(sentence).asScala.toMap mapValues(_.asScala.toMap mapValues(_.toDouble))
    }

    var result = Map[String,String]()
    var resultWithPOS = Map[(POS,String),Int]()

    dabMap foreach(a => {
      val synset = a._2.maxBy(_._2)._1
      result += (a._1.getFirst -> synset)
      resultWithPOS += ((a._1.getSecond,a._1.getFirst) -> a._2.size)  //a._2.size
    })

    //run graph algorithm on i) only nouns and ii) with 6 search depth and iii) only hypernyms
    //val conceptOptions = resultWithPOS.filterKeys(_._1 == POS.NOUN).values map (synsetFormatForSenseId)
    //println(conceptOptions)
    result
  }

  def disambiguate(text: List[WordAnalysis], useOwn: Boolean = false): Map[String,String] = {

    val result = rawDisambiguate(text,useOwn)
    result mapValues (synsetFormatForSenseId)
  }

  def disambiguateWithSenseId(text: List[WordAnalysis], useOwn: Boolean = false): Map[String,String] = {

    val result = rawDisambiguate(text,useOwn)
    result
  }

  def disambiguateWithGloss(text: List[WordAnalysis], useOwn: Boolean = false): Map[String,(String,String)] = {

    val result = rawDisambiguate(text,useOwn)
    result mapValues (synsetFormatForSenseIdWithGloss)
  }

  def ownDisambiguation(sods: java.util.Collection[Pair[String, POS]], dGraph: Graph[String, UnorderedPair[String]]) : Map[Pair[String, POS], Map[String, Double]] = {

    var solutions: Map[Pair[String, POS], Map[String, Double]] = Map[Pair[String, POS], Map[String, Double]]()
    var disambiguatedCount: Int = 0


    for (wsdItem <- sods.asScala) {
      val senses: List[String] = inventory.getSenses(wsdItem.getFirst, wsdItem.getSecond).asScala.toList
      var highestDegree: String = null

      for (sense <- senses) {

        (dGraph.degree(sense) < minDegree) match {
          case false => {
            if (highestDegree == null || (dGraph.degree(sense) > dGraph.degree(highestDegree))) {
              highestDegree = sense
            }
          }
          case true =>
        }

      }
      if (highestDegree == null) {
        logger.error("Failed to disambiguate " + wsdItem)
      }

      // Note that instead of returning a single mapping to the sense
      // with the highest score, we could instead return a map of all
      // senses weighted by their degree (normalized to the maximum
      // degree). In this case falling back to the most frequent sense
      // would not be necessary.
      disambiguatedCount += 1
      var senseScores: Map[String, Double] = Map[String, Double]()
      senseScores += (highestDegree -> 1.0)
      solutions += (wsdItem -> senseScores)
      logger.debug("\"" + wsdItem.getFirst + "\" = " + highestDegree + ": " + inventory.getSenseDescription(highestDegree))

    }
    logger.info("Disambiguated " + (disambiguatedCount) + " of " + sods.size + " items")
    return solutions
  }
  ///

  def getCenters(wsds: List[String], dGraph: Graph[String, UnorderedPair[String]]) : Map[String, Double] = {

    var solutions: Map[String, Double] = Map[String, Double]()
    var disambiguatedCount: Int = 0
    var highestDegree: String = null

      for (sense <- wsds) {
        solutions += (sense -> 0.0)
        (dGraph.degree(sense) < minDegree) match {
          case false => {
            if (highestDegree == null || (dGraph.degree(sense) > dGraph.degree(highestDegree))) {
              highestDegree = sense
              solutions += (sense -> dGraph.degree(sense))
            }
          }
          case true =>
        }

      }

    return solutions
  }



}
