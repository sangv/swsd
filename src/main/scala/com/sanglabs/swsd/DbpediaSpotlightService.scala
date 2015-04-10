package com.sanglabs.swsd


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import grizzled.slf4j.Logger
import org.apache.commons.lang.StringUtils
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair

/**
 *
 * The DbpediaSpotlightService
 *
 * http://www.wiwiss.fu-berlin.de/en/institute/pwo/bizer/research/publications/Mendes-Jakob-GarciaSilva-Bizer-DBpediaSpotlight-ISEM2011.pdf
 *
 * @author Sang Venkatraman
 *
 */

case class NamedEntity(surfaceForm: String, uri: String, support: Int, types: List[String], offset: Int, similarityScore: Double, percentageOfSecondRank: Double)

object DbpediaSpotlightService {

  val dbpediaSpotlightServiceUrl = "http://spotlight.dbpedia.org/rest/"
  val SPARQL_QUERY: String = ""
  val DEFAULT_SUPPORT: String = "100"

  val logger = Logger[this.type]

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def getDbpediaEntities(text: String, options: Map[String, String] = Map[String, String]()): List[NamedEntity] = {
    getDbpediaServiceResponse(text, options) match {
      case Some(response: String) => getEntitiesFromResponse(response, options.getOrElse("support", DEFAULT_SUPPORT).toDouble)
      case None => Nil
    }
  }

  def getEntitiesFromResponse(response: String, supportThreshold: Double): List[NamedEntity] = {
    val responseMap = mapper.readValue(response, classOf[Map])
    println(responseMap)
    //val resources: Map = responseMap.get("Resources")
    /*if (resources != null) {
      for (resource <- resources) {
        val uri: Nothing = resource.get("@URI")
        val support: Nothing = Double.parseDouble(resource.get("@support"))
        val offset: Nothing = Integer.parseInt(resource.get("@offset"))
        val similarityScore: Nothing = Double.parseDouble(resource.get("@similarityScore"))
        val percentageOfSecondRank: Nothing = Double.parseDouble(resource.get("@percentageOfSecondRank"))
        val surfaceForm: Nothing = resource.get("@surfaceForm")
        val types: Nothing = Arrays.asList(resource.get("@types").split(","))
        if (support >= Double.valueOf(supportThreshold)) {
          namedEntities.add(new DbpediaNamedEntity(surfaceForm, null, uri, support, types, offset, similarityScore, percentageOfSecondRank))
        }
      }
    }*/
    List[NamedEntity]()
  }


  def getEntities(text: String): List[NamedEntity] = {

    val spots: Set[String] = StanfordNLPService.nerSpots(text).keySet
    var options: Map[String, String] = Map[String, String]()
    options += ("spotter" -> "SpotXmlParser")
    var namedEntities = List[NamedEntity]()
    try {
      namedEntities ++= getDbpediaEntities(buildSpotXmlParserPayload(text, spots), options)
    }
    catch {
      case (e: Exception) => {
        logger.info(s"Unable to use NLP algorithms locally for named entity spotting. Using only Dbpedia spotlight + ${e.getLocalizedMessage}")
      }
    }
    namedEntities = namedEntities ++ getDbpediaEntities(text)

    return namedEntities
  }

  def buildSpotXmlParserPayload(text: String, namedEntities: Set[String]): String = {
    val textBuilder: StringBuilder = new StringBuilder("<annotation text=\"")
    textBuilder.append(text).append("\">")
    for (namedEntity <- namedEntities) {
      textBuilder.append("<surfaceForm name=\"").append(namedEntity).append("\"").append(" offset=\"").append(StringUtils.indexOf(text, namedEntity)).append("\"").append("/>")
    }
    textBuilder.append("</annotation>")
    return textBuilder.toString
  }

  def getDbpediaServiceResponse(text: String, options: Map[String, String]): Option[String] = {

    val api: String = options.getOrElse("api", "annotate")
    val completeUrl = dbpediaSpotlightServiceUrl + api
    postRestContent(completeUrl, text, options)
  }

  def postRestContent(url: String, text: String, options: Map[String, String]): Option[String] = {
    val httpClient = HttpClientBuilder.create().build()

    val httpPost = new HttpPost(url)
    httpPost.setHeader("Access-Control-Allow-Origin", "*")
    httpPost.setHeader("ContentType", "application/x-www-form-urlencoded;charset=UTF-8")
    httpPost.setHeader("Accept", options.getOrElse("acceptHeader", "application/json"))

    val nvps: java.util.List[NameValuePair] = new java.util.ArrayList[NameValuePair]()
    nvps.add(new BasicNameValuePair("disambiguator", options.getOrElse("disambiguator", "Default")))
    nvps.add(new BasicNameValuePair("confidence", options.getOrElse("confidence", "0.7")))
    nvps.add(new BasicNameValuePair("support", options.getOrElse("support", DEFAULT_SUPPORT)))
    nvps.add(new BasicNameValuePair("text", options.getOrElse("text", text)))
    nvps.add(new BasicNameValuePair("spotter", options.getOrElse("spotter", "Default")))
    nvps.add(new BasicNameValuePair("sparql", options.getOrElse("sparql", SPARQL_QUERY)))
    nvps.add(new BasicNameValuePair("types", options.getOrElse("types", "Place,Person,Organization")))
    val formEntity = new UrlEncodedFormEntity(nvps, "UTF-8")
    httpPost.setEntity(formEntity)


    val httpResponse = httpClient.execute(httpPost)
    val entity = httpResponse.getEntity()
    var content = ""
    if (entity != null) {
      val inputStream = entity.getContent()
      content = scala.io.Source.fromInputStream(inputStream).getLines.mkString
      inputStream.close
    }
    httpClient.getConnectionManager.shutdown()
    return Option(content)
  }

}
