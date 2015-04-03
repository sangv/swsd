package com.sanglabs.swsd

import grizzled.slf4j.Logger
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.graphdb.{Node, RelationshipType, Transaction}

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

  val DEST: String = "data/conceptnetgraph.db"

  val graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DEST)

  val executionEngine = new ExecutionEngine(graphDb)

  val relRelationshipType = new RelationshipType {
    override def name(): String = "rel"
  }


  var cacheSet = Set[(String,String,String)]()
  loadData
  //uri, rel, start, end, context, weight, sources, id, dataset, surfaceText

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


    def saveTriple(src: String,rel: String, dest: String, weight: Double, source: String) = {
      val tx: Transaction = graphDb.beginTx

      try {
        val srcNode = getOrCreateConceptNetNode(src,source)
        val destNode = getOrCreateConceptNetNode(dest,source)

        val relRelation = srcNode.createRelationshipTo(destNode,relRelationshipType)
        relRelation.setProperty("name",rel)
        relRelation.setProperty("weight",weight)
        logger.debug(s"Done persisting ${src} => ${rel} => ${dest}")

        //srcNode.getRelationships(Direction.OUTGOING,relRelationshipType).asScala find {_.getEndNode == destNode}
        /*import scala.collection.JavaConverters._
        srcNode.getRelationships(Direction.OUTGOING,relRelationshipType).asScala find {_.getEndNode == destNode} match {
          case None => {
            val rel = srcNode.createRelationshipTo(destNode,relRelationshipType)
            rel.setProperty("name",rel)
            rel.setProperty("weight",weight)
            logger.debug(s"Done persisting ${src} => ${rel} => ${dest}")
          }
          case _ => {
            logger.debug(s"${src} => ${rel} => ${dest} already exists")
          }
        }*/
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
      } yield (parts(8), parts(2), parts(1), parts(3), parts(5))

      val englishLinesList = List() ++ englishLines
      println(englishLinesList.length)

      val (conceptNetEnglishLines, otherEnglishLines) = englishLinesList partition (l => {
        l._1.startsWith("/d/conceptnet/")
      })


      val (wordNetEnglishLines, wikiEnglishLines) = otherEnglishLines partition (l => {
        l._1.startsWith("/d/wordnet/")
      })

      println(s"${conceptNetEnglishLines.length}+${wordNetEnglishLines.length}+${wikiEnglishLines.length}=${englishLinesList.length}")

      wordNetEnglishLines foreach { l => {saveTriple(l._2,l._3,l._4,l._5.toDouble,l._1)} }

      //wordNetEnglishLines foreach { l => {saveTriple(l._2,l._3,l._4)} }

      println(f)
    }
  }


}