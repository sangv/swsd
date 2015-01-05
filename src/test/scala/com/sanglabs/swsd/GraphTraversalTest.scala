package com.sanglabs.swsd

import com.tinkerpop.blueprints.{Edge, Vertex}
import com.tinkerpop.gremlin.scala.{ScalaGraph, ScalaPipeFunction, ScalaVertex}
import com.tinkerpop.pipes.branch.LoopPipe.LoopBundle
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSpec}

import scala.collection.JavaConverters._

/**
 *
 * The GraphTraversalTest is there to exercise the gremlin traversal api on the wordnetgraph data
 *
 * @author Sang Venkatraman
 *
 */
class GraphTraversalTest  extends FunSpec with ShouldMatchers with BeforeAndAfter with BeforeAndAfterAll {

  val gs: ScalaGraph = WordnetGraphService.gs

  it("should be able to find vertices based on synset offsets") {

    val synset1 = WordnetGraphService.getSynset("swim#v#1")
    val synset2 = WordnetGraphService.getSynset("travel#v#1")

    val v1: ScalaVertex = gs.V.has("pos",synset1.getPOS.getKey).has("offset",synset1.getOffset).iterator().next() //TODO guard against multiple (or no) matches
    val v2: ScalaVertex = gs.V.has("pos",synset2.getPOS.getKey).has("offset",synset2.getOffset).iterator().next()

    v1 should not be null
    v2 should not be null

    v1.getProperty[Long]("offset") shouldEqual 1960911
    v2.getProperty[Long]("offset") shouldEqual 1835496

    v1.getProperty[String]("gloss") shouldEqual "travel through water; \"We had to swim for 20 minutes to reach the shore\"; \"a big fish was swimming in the tank\""
    v2.getProperty[String]("gloss") shouldEqual "change location; move, travel, or proceed, also metaphorically; \"How fast does your new car go?\"; \"We travelled from Rome to Naples by bus\"; \"The policemen went from door to door looking for the suspect\"; \"The soldiers moved towards the city in an attempt to take it before night fell\"; \"news travelled fast\""

  }

  it("should be able to find paths between synsets") {

    val synset1 = WordnetGraphService.getSynset("swim#v#1")
    val synset2 = WordnetGraphService.getSynset("travel#v#1")

    val v1: ScalaVertex = gs.V.has("pos",synset1.getPOS.getKey).has("offset",synset1.getOffset).iterator().next() //TODO guard against multiple (or no) matches
    val v2: ScalaVertex = gs.V.has("pos",synset2.getPOS.getKey).has("offset",synset2.getOffset).iterator().next()

    val pipe = v1.->.as("synset").out.loop("synset",(loopBundle: LoopBundle[Vertex]) => {
      loopBundle.getLoops() < 3 &&
        loopBundle.getObject.getProperty[Long]("offset") != v2.getProperty[Long]("offset")
    },
      (loopBundle: LoopBundle[Vertex]) => {
        loopBundle.getObject.getProperty[Long]("offset") == v2.getProperty[Long]("offset")
      }).path(new ScalaPipeFunction[Vertex, Long](
      (v: Vertex) => v.getProperty[Long]("offset")
    ))


    val shortestPath: List[_] = pipe.next().asScala.toList
    println(shortestPath.mkString(" -> "))
    shortestPath.size should be(2)
    shortestPath(0) should be(1960911)
    shortestPath(1) should be(1835496)

  }

  it("should be able to find paths between synsets filter relationships pass") {

    val synset1 = WordnetGraphService.getSynset("swim#v#1")
    val synset2 = WordnetGraphService.getSynset("travel#v#1")

    val v1: ScalaVertex = gs.V.has("pos",synset1.getPOS.getKey).has("offset",synset1.getOffset).iterator().next() //TODO guard against multiple (or no) matches
    val v2: ScalaVertex = gs.V.has("pos",synset2.getPOS.getKey).has("offset",synset2.getOffset).iterator().next()

    val pipe = v1.->.as("synset").outE().filter({(e:Edge) => "Hypernym".equals(e.getProperty[String]("pointer_type"))}).inV().loop("synset",(loopBundle: LoopBundle[Vertex]) => {
      loopBundle.getLoops() < 3 &&
        loopBundle.getObject.getProperty[Long]("offset") != v2.getProperty[Long]("offset")
    },
      (loopBundle: LoopBundle[Vertex]) => {
        loopBundle.getObject.getProperty[Long]("offset") == v2.getProperty[Long]("offset")
      }).path(new ScalaPipeFunction[Any, Any]({
      case (v: Vertex) => v.getProperty[Long]("offset")
      case (e:Edge) => e.getProperty[String]("pointer_type")
    }
    ))


    val shortestPath: List[_] = pipe.next().asScala.toList
    println(shortestPath.mkString(" -> "))
    shortestPath.size should be(3)
    shortestPath(0) should be(1960911)
    shortestPath(1) should be("Hypernym")
    shortestPath(2) should be(1835496)

  }

  it("should be able to find paths between synsets filter relationships fail") {

    val synset1 = WordnetGraphService.getSynset("fish#n#1")
    val synset2 = WordnetGraphService.getSynset("sea#n#1")

    val v1: ScalaVertex = gs.V.has("pos",synset1.getPOS.getKey).has("offset",synset1.getOffset).iterator().next() //TODO guard against multiple (or no) matches
    val v2: ScalaVertex = gs.V.has("pos",synset2.getPOS.getKey).has("offset",synset2.getOffset).iterator().next()

    val pipe = v1.->.as("synset").outE().filter({(e:Edge) => "Hypernym".equals(e.getProperty[String]("pointer_type"))}).inV().loop("synset",(loopBundle: LoopBundle[Vertex]) => {
      loopBundle.getLoops() < 8 &&
        loopBundle.getObject.getProperty[Long]("offset") != v2.getProperty[Long]("offset")
    },
      (loopBundle: LoopBundle[Vertex]) => {
        loopBundle.getObject.getProperty[Long]("offset") == v2.getProperty[Long]("offset")
      }).path(new ScalaPipeFunction[Any, Any]({
      case (v: Vertex) => v.getProperty[Long]("offset")
      case (e:Edge) => e.getProperty[String]("pointer_type")
    }
    ))


    pipe.hasNext shouldEqual(false)

  }

  it("should be able to find paths between synsets filter relationships non-adjacent pass") {

    val synset1 = WordnetGraphService.getSynset("king#n#1")
    val synset2 = WordnetGraphService.getSynset("ruler#n#1")

    val v1: ScalaVertex = gs.V.has("pos",synset1.getPOS.getKey).has("offset",synset1.getOffset).iterator().next() //TODO guard against multiple (or no) matches
    val v2: ScalaVertex = gs.V.has("pos",synset2.getPOS.getKey).has("offset",synset2.getOffset).iterator().next()

    val pipe = v1.->.as("synset").outE().filter({(e:Edge) => "pointer_type".equals(e.getLabel) && "Hypernym".equals(e.getProperty[String]("pointer_type"))}).inV().loop("synset",(loopBundle: LoopBundle[Vertex]) => {
      loopBundle.getLoops() < 3 &&
        loopBundle.getObject.getProperty[Long]("offset") != v2.getProperty[Long]("offset")
    },
      (loopBundle: LoopBundle[Vertex]) => {
        loopBundle.getObject.getProperty[Long]("offset") == v2.getProperty[Long]("offset")
      }).path(new ScalaPipeFunction[Any, Any]({
      case (v: Vertex) => v.getProperty[Long]("offset")
      case (e:Edge) => e.getProperty[String]("pointer_type")
    }
    ))


    val shortestPath: List[_] = pipe.next().asScala.toList
    println(shortestPath.mkString(" -> "))
    shortestPath.size should be(5)
    shortestPath(0) should be(10231515)
    shortestPath(1) should be("Hypernym")
    shortestPath(2) should be(10628644)
    shortestPath(3) should be("Hypernym")
    shortestPath(4) should be(10541229)

  }

  override def afterAll(configMap: Map[String, Any]) {
    gs.shutdown()
  }

}
