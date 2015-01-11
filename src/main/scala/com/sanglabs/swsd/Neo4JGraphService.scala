package com.sanglabs.swsd

import java.util.Comparator

import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph
import grizzled.slf4j.Logger
import net.sf.extjwnl.data.{POS, Synset}
import org.neo4j.graphdb._
import org.neo4j.graphdb.traversal.{Traverser, _}
import org.neo4j.kernel.{Traversal, Uniqueness}

import scala.collection.JavaConverters._
import scala.collection._

/**
 *
 * The WordnetGraphService provide access to the wordnet graph
 *
 * @author Sang Venkatraman
 *
 */
object Neo4JGraphService {

  private val graphDb: Neo4jGraph = new Neo4jGraph("data/wordnetgraph.db")

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

    var occurences = List[String]()
    val optionValues: List[String] = options.values.toList.flatten
    //TODO account for multiple occurances
    for ( f <- optionValues) {
      for(s <- optionValues) {
        if(!f.equals(s)) {
          logger.info(s"Computing $f and $s")
          occurences ++= shortestPath(f, s)
        }
      }
    }

    //TODO add WordAnalysis to the result
    occurences.groupBy(l => l).map(t => (t._1, t._2.length)).toList.sortBy({_._2}).reverse
  }


  def shortestPath(synsetName1: String, synsetName2: String, maxDepth: Int = 5): List[String] = {

    val startNode = getSynsetNode(synsetName1)
    val endNode = getSynsetNode(synsetName2)


    val paths = getTraverser(startNode,endNode,maxDepth).asScala
    for(path:Path <- paths) {
      println(path.nodes().iterator().asScala.toList map(_.getProperty("offset")) mkString(", "))
    }

    if(paths != null && paths.size > 0)
      paths.head.nodes().asScala.toList.map(_.getProperty("synsetNames").asInstanceOf[Array[String]](0))
    else
      Nil


  }

  protected def getSynsetNode(synsetName: String): Node = {
    val synset: Synset = getSynset(synsetName)
    val synsetNodeIterator: java.util.Iterator[Node] = graphDb.getRawGraph().index.forNodes("synset").get("offset", synset.getOffset).iterator
    
    while (synsetNodeIterator.hasNext) {
      val node: Node = synsetNodeIterator.next
      if (synset.getPOS.getKey == node.getProperty("pos")) {
        return node
      }
    }
    return null
  }

  protected def getTraverser(startNode: Node, endNode: Node, maxDepth: Int): Traverser = {
    val td: TraversalDescription = Traversal.description.relationships(synsetRelationshipType, Direction.OUTGOING).evaluator(new Evaluator {
      override def evaluate(path: Path): Evaluation = {
        val iterator: java.util.Iterator[Relationship] = path.relationships.iterator
        while (iterator.hasNext) {
          val rel: Relationship = iterator.next
          if (!rel.isType(synsetRelationshipType)) {
            return Evaluation.EXCLUDE_AND_PRUNE
          }
        }
        return Evaluation.INCLUDE_AND_CONTINUE
      }
    }).evaluator(Evaluators.includeWhereEndNodeIs(endNode)).evaluator(Evaluators.toDepth(maxDepth)).uniqueness(Uniqueness.NODE_PATH).sort(new Comparator[Path] {
      override def compare(o1: Path, o2: Path): Int = o1.length().compareTo(o2.length())
    })
    td.traverse(startNode)
  }

}
