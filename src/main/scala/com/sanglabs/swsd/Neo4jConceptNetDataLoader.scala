package com.sanglabs.swsd

import grizzled.slf4j.Logger
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.graphdb.{Direction, Node, RelationshipType, Transaction}

import scala.io.Source

/**
 *
 * The ConceptNetNeo4jLoader 
 *
 * @author Sang Venkatraman
 *
 */
object Neo4jConceptNetDataLoader extends App {

  val filenames = Array("part_00.csv","part_01.csv","part_02.csv","part_03.csv","part_04.csv","part_05.csv","part_06.csv","part_07.csv")

  val directory = "/Users/sang/Downloads/data/assertions/"

  val logger = Logger[this.type]

  val DEST: String = "data/conceptnetlightgraph.db"

  val graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DEST)

  val executionEngine = new ExecutionEngine(graphDb)

  val relRelationshipType = new RelationshipType {
    override def name(): String = "rel"
  }


  var cacheSet = Set[(String,String,String)]()
  loadData
  //uri, rel, start, end, context, weight, sources, id, dataset, surfaceText

  case class Relation(source: String, start: String, rel: String, end: String, weight: Double)

  graphDb.shutdown()

  def loadData = {

    def getOrCreateConceptNetNode(name: String, source: String): Node = {
      val tx = graphDb.beginTx()
      var node:Node = null
      val conceptNetIndex = graphDb.index.forNodes("conceptnet")
      try {
        val nodeOpt = Option(conceptNetIndex.get("name", name).getSingle)
        node = nodeOpt match {
          case Some(x:Node) => {
            logger.debug(s"${x.getProperty("name")} already exists")
            x
          }
          case None => {
            val newNode = graphDb.createNode()
            newNode.setProperty("name",name)
            newNode.setProperty("source",source)
            conceptNetIndex.add(newNode,"name",name)
            conceptNetIndex.add(newNode,"source",source)
            logger.debug(s"${newNode.getProperty("name")} created")
            newNode
          }
        }
        tx.success()
      }
      catch {
        case e: Exception => {
          tx.failure
          logger.error(e.getLocalizedMessage, e)
        }
      } finally {
        tx.close
      }
      node
    }


    def saveTriple(relation: Relation) = {
      val tx: Transaction = graphDb.beginTx

      try {
        val srcNode = getOrCreateConceptNetNode(relation.start,relation.source)
        val destNode = getOrCreateConceptNetNode(relation.end,relation.source)

        //srcNode.getRelationships(Direction.OUTGOING,relRelationshipType).asScala find {_.getEndNode == destNode}
        import scala.collection.JavaConverters._
        srcNode.getRelationships(Direction.OUTGOING,relRelationshipType).asScala find {_.getEndNode == destNode} match {
          case None => {
            val relRelationship = srcNode.createRelationshipTo(destNode,relRelationshipType)
            relRelationship.setProperty("name",relation.rel)
            relRelationship.setProperty("weight",relation.weight)
            logger.debug(s"Done persisting ${relation.start} => ${relation.rel} => ${relation.end}")
          }
          case _ => {
            logger.debug(s"${relation.start} => ${relation.rel} => ${relation.end} already exists")
          }
        }
        tx.success()
      }
      catch {
        case e: Exception => {
          tx.failure
          logger.error(e.getLocalizedMessage, e)
        }
      } finally {
        tx.close
      }

    }



    filenames foreach { f =>
      val englishLines = for {
        line <- Source.fromFile(directory + f).getLines()
        parts = line.split("\t")
        if (parts.length >= 4 && parts(2).startsWith("/c/en/") && parts(3).startsWith("/c/en/"))
      } yield Relation(parts(8), parts(2), parts(1), parts(3), parts(5).toDouble)

      val englishLinesList = List() ++ englishLines
      println(englishLinesList.length)

      val (conceptNetEnglishLines, otherEnglishLines) = englishLinesList partition (l => {
        l.source.startsWith("/d/conceptnet/")
      })


      val (wordNetEnglishLines, wikiAndOtherEnglishLines) = otherEnglishLines partition (l => {
        l.source.startsWith("/d/wordnet/")
      })

      val dbpediaEnglishLines = wikiAndOtherEnglishLines filter (l => {
        l.source.startsWith("/d/dbpedia/")
      })

      val wiktionaryEnglishLines = wikiAndOtherEnglishLines filter (l => {
        l.source.startsWith("/d/wiktionary/en/")
      })

      println(s"${conceptNetEnglishLines.length}+${wordNetEnglishLines.length}+${dbpediaEnglishLines.length}+${wiktionaryEnglishLines.length}+${wikiAndOtherEnglishLines.length}=${englishLinesList.length}")

      wordNetEnglishLines foreach { saveTriple }

      conceptNetEnglishLines foreach { saveTriple }

      //wordNetEnglishLines.take(50) foreach println
      //conceptNetEnglishLines.take(50) foreach println

      wiktionaryEnglishLines foreach { saveTriple }

      println(f)
    }
  }


}