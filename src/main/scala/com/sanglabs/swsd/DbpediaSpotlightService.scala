package com.sanglabs.swsd


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import grizzled.slf4j.Logger
import org.apache.commons.lang.StringUtils
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.client.utils.URIBuilder
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
  val sparqlHost = "dbpedia.org"
  val DEFAULT_SUPPORT: String = "20"
  val DEFAULT_CONFIDENCE: String = "0.2"

  val logger = Logger[this.type]

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def getDbpediaEntities(text: String, options: Map[String, String] = Map[String, String]()): Set[NamedEntity] = {
    getDbpediaServiceResponse(text, options) match {
      case Some(response: String) => getEntitiesFromResponse(response, options.getOrElse("support", DEFAULT_SUPPORT).toDouble)
      case None => Set[NamedEntity]()
    }
  }

  def getEntitiesFromResponse(response: String, supportThreshold: Double): Set[NamedEntity] = {
    val responseMap = mapper.readValue(response, classOf[Map[String,Any]])
    var namedEntities = Set[NamedEntity]()
    println(responseMap)
    val resources: List[Map[String,String]] = responseMap.getOrElse("Resources",Nil).asInstanceOf[List[Map[String,String]]]
    for (resource <- resources) {
      val uri: String = resource.getOrElse("@URI","")
      val support: Int = resource.getOrElse("@support","0").toInt
      val offset: Int = resource.getOrElse("@offset","-1").toInt
      val similarityScore: Double = resource.getOrElse("@similarityScore","0.0").toDouble
      val percentageOfSecondRank: Double = resource.getOrElse("@percentageOfSecondRank","0.0").toDouble
      val surfaceForm: String = resource.getOrElse("@surfaceForm","")
      //val types: Nothing = Arrays.asList(resource.get("@types").split(","))
      //if (support >= Double.valueOf(supportThreshold)) {
      //  namedEntities.add(new DbpediaNamedEntity(surfaceForm, null, uri, support, types, offset, similarityScore, percentageOfSecondRank))
      //}
      namedEntities += NamedEntity(surfaceForm,uri,support,Nil,offset,similarityScore,percentageOfSecondRank)
    }

    namedEntities
  }


  def getEntities(text: String): Set[NamedEntity] = {

    val spots: Set[String] = StanfordNLPService.nerSpots(text).keySet
    var options: Map[String, String] = Map[String, String]()
    options += ("spotter" -> "SpotXmlParser")
    var namedEntities = Set[NamedEntity]()
    try {
      namedEntities ++= getDbpediaEntities(buildSpotXmlParserPayload(text, spots), options)
    }
    catch {
      case (e: Exception) => {
        logger.info(s"Unable to use NLP algorithms locally for named entity spotting. Using only Dbpedia spotlight + ${e.getLocalizedMessage}")
      }
    }
    namedEntities = namedEntities ++ getDbpediaEntities(text) //defaults to using new map - not the one with SpotterXML

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
    nvps.add(new BasicNameValuePair("confidence", options.getOrElse("confidence", DEFAULT_CONFIDENCE)))
    nvps.add(new BasicNameValuePair("support", options.getOrElse("support", DEFAULT_SUPPORT)))
    nvps.add(new BasicNameValuePair("text", options.getOrElse("text", text)))
    nvps.add(new BasicNameValuePair("spotter", options.getOrElse("spotter", "Default")))
    nvps.add(new BasicNameValuePair("sparql", options.getOrElse("sparql", SPARQL_QUERY)))
    //nvps.add(new BasicNameValuePair("types", options.getOrElse("types", "Place,Person,Organization")))
    val formEntity = new UrlEncodedFormEntity(nvps, "UTF-8")
    httpPost.setEntity(formEntity)


    val httpResponse = httpClient.execute(httpPost)
    val entity = httpResponse.getEntity()
    val code = httpResponse.getStatusLine().getStatusCode()
    logger.info(s"Response code ${code}")
    var content = ""
    if (entity != null) {
      val inputStream = entity.getContent()
      content = scala.io.Source.fromInputStream(inputStream).getLines.mkString
      inputStream.close
    }
    httpClient.getConnectionManager.shutdown()
    return Option(content)
  }

  def getRestContent(url:String): Option[String] = {
    val httpClient = HttpClientBuilder.create().build()
    val httpResponse = httpClient.execute(new HttpGet(url))
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

  def isBrand(dbpediaUri: String) : Boolean = {

    val distributorOfQuery = s"SELECT ?distributor  WHERE { ?distributor dbpprop:distributor <${dbpediaUri}> }"


    val builder = new URIBuilder();
    builder.setScheme("http").setHost("sparqlHost").setPath("/sparql")
      .setParameter("default-graph-uri", "http://dbpedia.org")
      .setParameter("format", "application/json")
      .setParameter("query", distributorOfQuery)

    val result = getRestContent(builder.build().toString) match {
      case Some(response:String) => {
        val jsonObject = mapper.readValue(response, classOf[Map[String,Any]])
        jsonObject.get("results") match {
          case Some(x:Any) => {
            /*if (jsonObject.getJSONObject("results") != null && jsonObject.getJSONObject("results").getJSONArray("bindings") != null && !jsonObject.getJSONObject("results").getJSONArray("bindings").isEmpty()) {
              String distributor = jsonObject.getJSONObject("results").getJSONArray("bindings").getJSONObject(0).getJSONObject("distributor").getString("value");
              if (StringUtils.isNotBlank(distributor)) {
                return true;
              }
            }*/
            return false
          }
        }

      }
      case None => false
    }
    false
  }

}
