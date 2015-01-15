package com.sanglabs.swsd

import java.util.Comparator

import grizzled.slf4j.Logger
import net.sf.extjwnl.data.{POS, Synset}
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.graphdb._
import org.neo4j.graphdb.factory.GraphDatabaseFactory
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

  private val graphDb = new GraphDatabaseFactory().newEmbeddedDatabase("data/wordnetgraph.db")

  private val logger = Logger[this.type]

  val acceptedRelationships = List("Hypernym","Holonym")

  val executionEngine = new ExecutionEngine(graphDb)

  var cacheMap = Map[String,Int]() //FIXME bad idea to put a shared mutable variable

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
    val tx = graphDb.beginTx()
    try {
      val synset: Synset = getSynset(synsetName)
      var node: Node = graphDb.index.forNodes("synset").get("offset", synset.getOffset).getSingle
      var parentNode: Node = null
      while ((({
        parentNode = getHypernymNode(node);
        parentNode
      })) != null) {
        node = parentNode
      }
      return if (parentNode != null) parentNode.getProperty("offset").asInstanceOf[Long] else node.getProperty("offset").asInstanceOf[Long]
    }
    finally {
      tx.close()
    }

  }

  def getHypernymNode(node: Node): Node = {

    val tx = graphDb.beginTx()
    if (node.hasRelationship(synsetRelationshipType)) {
      val iter: java.util.Iterator[Relationship] = node.getRelationships(synsetRelationshipType, Direction.OUTGOING).iterator
      while (iter.hasNext) {
        val rel: Relationship = iter.next
        if (("Hypernym" == rel.getProperty("pointer_type")) || ("Instance_Hypernym" == rel.getProperty("pointer_type"))) {
          return rel.getEndNode
        }
      }
    }
    tx.close()
    return null
  }

  protected def printSynsetNode(node: Node): Long = {
    val tx = graphDb.beginTx()
    val synsetWordsString: StringBuilder = new StringBuilder("(")
    val parentSynset: Synset = WordnetDictionaryService.getSynsetAt(node.getProperty("pos").asInstanceOf[String], node.getProperty("offset").asInstanceOf[Long])
    import scala.collection.JavaConversions._
    for (word <- parentSynset.getWords) {
      synsetWordsString.append(word.getLemma).append(", ")
    }
    synsetWordsString.append(parentSynset.getOffset + ")")
    logger.info(synsetWordsString.toString)
    tx.close()
    return parentSynset.getOffset
  }

  def preprocessLemma (lemma: String):String = lemma.trim.toLowerCase

  def disambiguate(options: mutable.Map[WordAnalysis,List[String]]): Map[String,String] = {

    var result = Map[String,String]()
    var (resolvedMap, unresolvedMap) = options.partition(x => x._2.length == 1)

    var revMap = Map[String,WordAnalysis]()
    options.keys foreach{ k => {val values = options.get(k).get; values foreach {(v:String) => revMap += (v -> k)}}}

    while (unresolvedMap != null && unresolvedMap.size > 0) {
      val synsetScores = findNodes(resolvedMap,unresolvedMap)
      val topSynsetScoreMap = unresolvedMap.filter(_._2.contains(synsetScores.head._1))


      val scoresOfTopSynset: List[Int] = synsetScores.filter(y =>  { topSynsetScoreMap.values.flatten.toList.contains(y._1) }).map(_._2)

      logger.info(s"${scoresOfTopSynset.mkString(", ")} for ${revMap.get(synsetScores.head._1).get}")
      if(scoresOfTopSynset.min == scoresOfTopSynset.max) {
        //if max is equal to min, use most frequent usage because there is nothing to be determined from the map
        logger.warn(s"Max is same as min") //FIXME
      } else {
        resolvedMap += (revMap.get(synsetScores.head._1).get -> List(synsetScores.head._1))
      }

      unresolvedMap = unresolvedMap.filterNot(_._2.contains(synsetScores.head._1))

      logger.debug(s"Done resolving ${synsetScores.head} for ${revMap.get(synsetScores.head._1).get}")
    }
    //Add a step that puts a minimum threshold on the score and use that to just get the most frequent usage
    resolvedMap foreach (x => { result += (x._1.word -> x._2.head) } )
    result
  }

  def findNodes(resolvedMap: mutable.Map[WordAnalysis,List[String]], options: mutable.Map[WordAnalysis,List[String]]): List[(String,Int)] = {

    val tx = graphDb.beginTx()
    val synsetNames = options.values.flatten.toSet
    val synsetsOffsets = synsetNames map ((s: String) => getSynset(s).getOffset) mkString(",")

    val nodes = synsetNames map ((s: String) => getSynsetNode(s))
    val resolvedNodes = resolvedMap.values.flatten.toSet map ((s: String) => getSynsetNode(s))


    var mapOfSynsetOffset = Map[Long,String]()

    (nodes zip synsetNames) foreach (e =>  { mapOfSynsetOffset+=(e._1.getProperty("offset").asInstanceOf[Long] -> e._2) })

    val tm = scala.collection.mutable.Map[Long,Int]()

    //filter out nodes that have only 1 synset
    for(x <- nodes) {
      tm(x.getProperty("offset").asInstanceOf[Long]) = 0
      for(y <- nodes ++ resolvedNodes) {
        //don't connect node to self and also don't bother comparing different pos (performance optimization)
         val xoffset = x.getProperty("offset").asInstanceOf[Long]
         val yoffset = y.getProperty("offset").asInstanceOf[Long]
         if( (xoffset != yoffset ) && (x.getProperty("pos").asInstanceOf[String].equals(y.getProperty("pos").asInstanceOf[String]))){
           val cacheKey = xoffset + "_" + yoffset
           if(cacheMap.contains(cacheKey) && cacheMap.get(cacheKey).get >= 0) {
             tm(xoffset) += 1
           } else {
             val paths = getTraverser(x, y, 5).asScala
             if (paths.size > 0) {
               tm(xoffset) += 1
               logger.debug(s"${x.getProperty("synsetNames").asInstanceOf[Array[String]](0)} -- ${paths.head.length()} -> ${y.getProperty("synsetNames").asInstanceOf[Array[String]](0)}")
               cacheMap += (cacheKey -> paths.head.length())
             } else {
               cacheMap += (cacheKey -> -1)
             }
           }
         }
      }
    }
    tx.close()

    val result = tm.map(t => (mapOfSynsetOffset.get(t._1).get, t._2)).toList.sortBy({_._2}).reverse

    result

  }


  def shortestPath(synsetName1: String, synsetName2: String, maxDepth: Int = 5): List[String] = {

    val startNode = getSynsetNode(synsetName1)
    val endNode = getSynsetNode(synsetName2)


    val paths = getTraverser(startNode,endNode,maxDepth).asScala
    for(path:Path <- paths) {
      logger.debug(path.nodes().iterator().asScala.toList map(_.getProperty("synsetNames").asInstanceOf[Array[String]](0)) mkString(", "))
    }

    if(paths != null && paths.size > 0)
      paths.head.nodes().asScala.toList.map(_.getProperty("synsetNames").asInstanceOf[Array[String]](0))
    else
      Nil


  }

  protected def getSynsetNode(synsetName: String): Node = {
    val tx = graphDb.beginTx()
    val synset: Synset = getSynset(synsetName)
    val synsetNodeIterator: java.util.Iterator[Node] = graphDb.index.forNodes("synset").get("offset", synset.getOffset).iterator
    
    while (synsetNodeIterator.hasNext) {
      val node: Node = synsetNodeIterator.next
      if (synset.getPOS.getKey == node.getProperty("pos")) {
        return node
      }
    }
    tx.close()
    return null
  }

  protected def getTraverser(startNode: Node, endNode: Node, maxDepth: Int): Traverser = {
    val td: TraversalDescription = Traversal.description.relationships(synsetRelationshipType, Direction.OUTGOING).evaluator(new Evaluator {
      override def evaluate(path: Path): Evaluation = {
        val iterator: java.util.Iterator[Relationship] = path.relationships.iterator
        while (iterator.hasNext) {
          val rel: Relationship = iterator.next
          if (!rel.isType(synsetRelationshipType)){ // && acceptedRelationships.contains(rel.getProperty("pointer_type"))) {
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
