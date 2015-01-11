// Copyright (c) 2012 Kenzie Lane Mosaic, LLC. All rights reserved.
package com.sanglabs.swsd

import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph
import com.tinkerpop.blueprints.{Edge, Vertex}
import com.tinkerpop.gremlin.scala.{ScalaGraph, ScalaPipeFunction, ScalaVertex}
import com.tinkerpop.pipes.branch.LoopPipe.LoopBundle
import grizzled.slf4j.Logger
import net.sf.extjwnl.data.{POS, Synset}

import scala.collection.JavaConverters._

/**
 *
 * The WordnetGremlinService 
 *
 * @author Sang Venkatraman
 *
 */
object GremlinGraphService {

  private val logger = Logger[this.type]

  private val graphDb: Neo4jGraph = new Neo4jGraph("data/wordnetgraph.db")

  def gs = ScalaGraph(graphDb)
  def v(i: Int) = gs.v(i:Integer).get
  def e(i: Int) = gs.e(i:Integer).get


  def preprocessLemma (lemma: String):String = lemma.trim.toLowerCase

  def getSynset(synsetName: String): Synset = {
    val synsetNameParts: Array[String] = synsetName.split("#")
    val synsetWord: String = preprocessLemma(synsetNameParts(0))
    val synsetPOS: POS = POS.getPOSForKey(synsetNameParts(1))
    val synsetIndex: Int = Integer.valueOf(synsetNameParts(2))

    WordnetDictionaryService.indexWord(synsetPOS, synsetWord).getSenses.get(synsetIndex - 1)
  }

  def shortestPath(synsetName1: String, synsetName2: String, maxDepth: Int = 5): List[String] = {

    val synset1: Synset = getSynset(synsetName1)
    val synset2: Synset = getSynset(synsetName2)

    val v1: ScalaVertex = gs.V.has("pos",synset1.getPOS.getKey).has("offset",synset1.getOffset).iterator().next() //TODO guard against multiple (or no) matches
    val v2: ScalaVertex = gs.V.has("pos",synset2.getPOS.getKey).has("offset",synset2.getOffset).iterator().next()

    val pipe = v1.->.as("synset").outE("Synset").inV.loop("synset",(loopBundle: LoopBundle[Vertex]) => {
      loopBundle.getLoops() < maxDepth &&
        loopBundle.getObject.getProperty[Long]("offset") != v2.getProperty[Long]("offset")
    },
      (loopBundle: LoopBundle[Vertex]) => {
        loopBundle.getObject.getProperty[Long]("offset") == v2.getProperty[Long]("offset")
      }).path(new ScalaPipeFunction[Any, Any]({
      case (v: Vertex) => v.getProperty[java.util.List[String]]("synsetNames").get(0)//v.getProperty[Long]("offset")//v.getProperty[Long]("offset")
      case (e:Edge) => "pointerType" + e.getProperty[String]("pointer_type")
    }
    ))


    if(pipe.hasNext) {
      val list: List[String] = pipe.next().asScala.toList.asInstanceOf[List[String]]
      logger.debug(list mkString(" -> "))
      list.filterNot(_.startsWith("pointerType"))
    } else {
      Nil
    }


  }


}
