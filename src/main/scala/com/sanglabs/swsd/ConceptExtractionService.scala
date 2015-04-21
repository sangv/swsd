package com.sanglabs.swsd

import net.sf.extjwnl.data.list.PointerTargetNodeList
import net.sf.extjwnl.data.{PointerUtils, Synset}

import scala.collection.immutable.ListMap
import scala.io.Source

/**
 *
 * The ConceptExtractionService 
 *
 * @author Sang Venkatraman
 *
 */
object ConceptExtractionService {

  val conceptMap: Map[String,String] = (for {
    line <- Source.fromFile("data/WordNetConceptMapping.txt").getLines()
  } yield (line.split(":")(0) -> line.split(":")(1))) toMap


  def extract(text: String): List[(String,Set[String])] = {

    var synsetFormatMap = Map[String,String]()
    var conceptResultMap = Map[String,Set[String]]();
    conceptMap.values foreach {c => { conceptResultMap += (c -> Set[String]())}}
    val options = WordNetDictionaryService.lookupOptions(text)
    val wsds = DKProWSDService.disambiguateWithSenseId(options.keys.toList)
    var conceptScores = Map[String, Int]()
    wsds.values foreach { synset => {
      val result = conceptMap.getOrElse(synset, "") //println(s"${synset} => ${result}")
    }
    }

    wsds.values foreach { senseId =>

      var activatedSynsets = Set[String]()
      val synset: Synset = WordNetDictionaryService.dictionary.getWordBySenseKey(senseId).getSynset

      import scala.collection.JavaConverters._
      val hypernymList = PointerUtils.getHypernymTree(synset, if(synset.getPOS.getKey == "n") 4 else 3).toList.asScala //val iter = PointerUtils.getDirectHypernyms(synset).iterator()

      //assert(hypernymList.size == 1)
      for (hypernym: PointerTargetNodeList <- hypernymList) {
        val iter = hypernym.iterator()
        var level = 0
        while (iter.hasNext) {
          level += 1
          val target = iter.next().getSynset

          val synsetFormat = DKProWSDService.synsetFormatForSenseId(target.getWords.get(0).getSenseKey)
          synsetFormatMap += (senseId -> synsetFormat)
          activatedSynsets += synsetFormat
        }
      }
      val conceptSynsetKeys = activatedSynsets.intersect(conceptMap.keySet)
      val concepts: Map[String,String] = conceptSynsetKeys map {cs => {conceptMap.get(cs).get -> cs}} toMap

      if(concepts.size > 0){
        val conceptsString = concepts.mkString(", ")
        concepts.keySet.foreach{c =>
          conceptResultMap += (c -> {conceptResultMap.get(c).get + concepts.get(c).get})
        }

      }
    }

    val result = ListMap(conceptResultMap.toList.sortBy(_._2.size): _*).toList.reverse
    result.filter(_._2.size > 0)
  }

}
