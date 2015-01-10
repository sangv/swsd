package com.sanglabs.swsd

import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph
import com.tinkerpop.blueprints.{Edge, Vertex}
import com.tinkerpop.gremlin.scala._
import com.tinkerpop.pipes.branch.LoopPipe.LoopBundle
import grizzled.slf4j.Logger
import net.sf.extjwnl.data.{POS, Synset}
import org.neo4j.graphdb._

import scala.collection.JavaConverters._
import scala.collection._

/**
 *
 * The WordnetGraphService provide access to the wordnet graph
 *
 * @author Sang Venkatraman
 *
 */
object WordnetGraphService {

  private val graphDb: Neo4jGraph = new Neo4jGraph("data/wordnetgraph.db")

  def gs = ScalaGraph(graphDb)
  def v(i: Int) = gs.v(i:Integer).get
  def e(i: Int) = gs.e(i:Integer).get

  private val logger = Logger[this.type]

  private val synsetRelationshipType = new RelationshipType {
    override def name(): String = "Synset"
  }

  private val SYSNET_INDEX = "synset"

  private val INDEXWORD_INDEX = "indexword"

  def getSynset(synsetName: String): Synset = {
    val synsetNameParts: Array[String] = synsetName.split("#")
    val synsetWord: String = preprocessLemma(synsetNameParts(0))
    val synsetPOS: POS = POS.getPOSForKey(synsetNameParts(1))
    val synsetIndex: Int = Integer.valueOf(synsetNameParts(2))
    /*val iter: java.util.Iterator[Node] = graphDb.getRawGraph.index().forNodes(INDEXWORD_INDEX).get("lemma",synsetWord).iterator()

      while (iter.hasNext) {
        val currentNode = iter.next()
        if (synsetPOS.equals(currentNode.getProperty("pos"))) {
          val indexWord: IndexWord = new IndexWord(WordnetDictionaryService.dictionary,synsetWord,synsetPOS, List(1000L).toArray)//FIXME
          return indexWord.getSenses.get(synsetIndex - 1)
        }
      }*/
    WordnetDictionaryService.indexWord(synsetPOS, synsetWord).getSenses.get(synsetIndex - 1)
  }

  def getHypernymTree(synsetName: String): Long = {
    val synset: Synset = getSynset(synsetName)
    var node: Node = graphDb.getRawGraph().index.forNodes("synset").get("offset", synset.getOffset).getSingle
    var parentNode: Node = null
    while ((({
      parentNode = getHypernymNode(node); parentNode
    })) != null) {
      node = parentNode
    }
    return if (parentNode != null) parentNode.getProperty("offset").asInstanceOf[Long] else node.getProperty("offset").asInstanceOf[Long]
  }

  def getHypernymNode(node: Node): Node = {
    printSynsetNode(node)
    if (node.hasRelationship(synsetRelationshipType)) {
      val iter: java.util.Iterator[Relationship] = node.getRelationships(synsetRelationshipType, Direction.OUTGOING).iterator
      while (iter.hasNext) {
        val rel: Relationship = iter.next
        if (("Hypernym" == rel.getProperty("pointer_type")) || ("Instance_Hypernym" == rel.getProperty("pointer_type"))) {
          return rel.getEndNode
        }
      }
    }
    return null
  }

  protected def printSynsetNode(node: Node): Long = {
    val synsetWordsString: StringBuilder = new StringBuilder("(")
    val parentSynset: Synset = WordnetDictionaryService.getSynsetAt(node.getProperty("pos").asInstanceOf[String], node.getProperty("offset").asInstanceOf[Long])
    import scala.collection.JavaConversions._
    for (word <- parentSynset.getWords) {
      synsetWordsString.append(word.getLemma).append(", ")
    }
    synsetWordsString.append(parentSynset.getOffset + ")")
    logger.info(synsetWordsString.toString)
    return parentSynset.getOffset
  }

  def preprocessLemma (lemma: String):String = lemma.trim.toLowerCase

  def disambiguate(options: mutable.Map[WordAnalysis,List[String]]): List[(String,Int)] = {
    val result:scala.collection.mutable.Map[WordAnalysis,String] = scala.collection.mutable.Map[WordAnalysis,String]()
    //options.keySet.filter( (x:WordAnalysis) => options.get(x).get.size == 1) foreach ((x:WordAnalysis) => { result.put(x,options.get(x).get.head) })

    /*val unresolvedMap: mutable.Map[WordAnalysis,List[String]] = mutable.Map[WordAnalysis,List[String]]()
    options.keySet.foreach { key =>
      options.get(key) match {
        case(Some(List(value:String))) => result.put(key,value)
        case Some(x :: xs) => unresolvedMap.put(key,x::xs)
        case Some(List()) => logger.error("$key cannot be resolved")
      }
    }*/

    var occurences = List[String]()
    val optionValues: List[String] = options.values.toList.flatten
    //TODO account for multiple occurances
    for ( (f:String,s:String) <- optionValues zip optionValues.drop(1) ) {
      occurences ++= shortestPath(f,s)
    }

    //TODO add WordAnalysis to the result
    occurences.groupBy(l => l).map(t => (t._1, t._2.length)).toList.sortBy({_._2}).reverse
  }


  def shortestPath(synsetName1: String, synsetName2: String): List[String] = {

    val synset1: Synset = getSynset(synsetName1)
    val synset2: Synset = getSynset(synsetName2)

    val v1: ScalaVertex = gs.V.has("pos",synset1.getPOS.getKey).has("offset",synset1.getOffset).iterator().next() //TODO guard against multiple (or no) matches
    val v2: ScalaVertex = gs.V.has("pos",synset2.getPOS.getKey).has("offset",synset2.getOffset).iterator().next()

    val pipe = v1.->.as("synset").outE("Synset").inV.loop("synset",(loopBundle: LoopBundle[Vertex]) => {
      loopBundle.getLoops() < 8 &&
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
