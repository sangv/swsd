package com.sanglabs.swsd

import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph
import grizzled.slf4j.Logger
import net.sf.extjwnl.data.{POS, Synset}
import org.neo4j.graphdb._

/**
 *
 * The WordnetGraphService provide access to the wordnet graph
 *
 * @author Sang Venkatraman
 *
 */
object WordnetGraphService {

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


}
