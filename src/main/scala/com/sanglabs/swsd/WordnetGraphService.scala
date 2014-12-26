package com.sanglabs.swsd

import java.io.FileInputStream

import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph
import net.sf.extjwnl.JWNLException
import net.sf.extjwnl.data.{POS, Synset}
import net.sf.extjwnl.dictionary.Dictionary
import org.neo4j.graphdb._
import org.slf4j.{Logger, LoggerFactory}

/**
 *
 * The WordnetGraphService provide access to the wordnet graph
 *
 * @author Sang Venkatraman
 *
 */
object WordnetGraphService {

  //connect all possible options (synsets) filtered by pos and maintain a multimap
  //between the words and the possible synsets

  //after all the components are connected, count the connections each one has
  // a) below a certain path length (4)
  // b) without including other of its own synsets

  //
  private val graphDb: Neo4jGraph = new Neo4jGraph("data/wordnetgraph.db")

  private val dictionary = Dictionary.getInstance(new FileInputStream("data/file_properties.xml"))

  private val LOGGER: Logger = LoggerFactory.getLogger(WordnetGraphService.getClass)

  private val synsetRelationshipType = new RelationshipType {
    override def name(): String = "Synset"
  }

  private val SYSNET_INDEX = "synset"

  def getSynset(synsetName: String): Synset = {
    val synsetNameParts: Array[String] = synsetName.split("#")
    val synsetWord: String = preprocessLemma(synsetNameParts(0))
    val synsetPOS: POS = POS.getPOSForKey(synsetNameParts(1))
    val synsetIndex: Int = Integer.valueOf(synsetNameParts(2))
    try {
      return dictionary.getIndexWord(synsetPOS, synsetWord).getSenses.get(synsetIndex - 1)
    }
    catch {
      case e: Exception => {
        LOGGER.error(e.getLocalizedMessage, e)
        throw new RuntimeException(e)
      }
    }
  }

  def getSynsetAt(pos: String, offset: Long): Synset = {
    try {
      return dictionary.getSynsetAt(POS.getPOSForKey(pos), offset)
    }
    catch {
      case e: JWNLException => {
        LOGGER.error(e.getLocalizedMessage, e)
      }
    }
    return null
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
    val parentSynset: Synset = getSynsetAt(node.getProperty("pos").asInstanceOf[String], node.getProperty("offset").asInstanceOf[Long])
    import scala.collection.JavaConversions._
    for (word <- parentSynset.getWords) {
      synsetWordsString.append(word.getLemma).append(", ")
    }
    synsetWordsString.append(parentSynset.getOffset + ")")
    LOGGER.debug(synsetWordsString.toString)
    return parentSynset.getOffset
  }

  def preprocessLemma (lemma: String):String = lemma.trim.toLowerCase


}
