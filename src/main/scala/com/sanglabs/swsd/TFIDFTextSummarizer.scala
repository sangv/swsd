package com.sanglabs.swsd

/**
 *
 * The TFIDFTextSummarizer 
 *
 * @author Sang Venkatraman
 *
 */
object TFIDFTextSummarizer extends App {

  def summarize(text: String, sentences: Int = 2) = {
    //call the sentence parser and split to sentences (maybe use opennlp sentence detecter)
    val sentences = StanfordNLPService.analyze(text)
    val sentenceTermFrequencies = sentences.map(s => Document(s.words map(_.word))).map(termFrequency).toList

  }

  //Treat each sentence as a document.

  def calculate(documents: List[Document]): Map[Document, List[(String,Double)]] = {

    var map = Map[Document, List[(String,Double)]]()

    val terms = documents flatMap {_.words} toSet
    val documentTermFrequencies = documents map termFrequency

    val idfs: Map[String,Double] = {
      var idfMap = Map[String,Double]()
      terms map (term => {
        val numberOfDocumentsWithTerm = documentTermFrequencies count (_.termFrequencies.get(term).getOrElse(0.0) > 0.0)
        val idf: Double = 1.0 + Math.log(documentTermFrequencies.length * 1.0 / numberOfDocumentsWithTerm)
        idfMap += (term -> idf)
      })
      idfMap
    }
    
    for (documentTermFrequency <- documentTermFrequencies){
      var innerMap = Map[String,Double]()
      for (term <- documentTermFrequency.termFrequencies.keySet) {
        val tf = documentTermFrequency.termFrequencies.get(term).getOrElse(0.0)
        val idf = idfs.get(term).get
        val tfidf = tf * idf
        innerMap += (term -> tfidf)
      }
      val sortedTerms = innerMap.toList sortBy {_._2}
      map += (documentTermFrequency.document -> sortedTerms.reverse)
    }
    map
  }

  def termFrequency(document: Document): DocumentTermFrequency = {
    val countMap: Map[String,Int] = document.words groupBy(_.toString) mapValues(_.size)
    val docLength = document.words.length
    var map = Map[String,Double]()
    for(term <- countMap.keySet) {
      map += (term -> countMap.get(term).getOrElse(0) * 1.0 / docLength)
    }
    val result = DocumentTermFrequency(document,map)
    result
  }

  case class DocumentTermFrequency(document: Document, termFrequencies: Map[String,Double])
  case class Document(words: List[String])  //terms imply uniqueness while words can be repeated

  val doc1 = Document("The game of life is a game of everlasting learning".split(" ").map(_.toLowerCase).toList)
  val doc2 = Document("The unexamined life is not worth living".split(" ").map(_.toLowerCase).toList)
  val doc3 = Document("Never stop learning".split(" ").map(_.toLowerCase).toList)//TODO convert to lowercase as part of pre-processing
  val result = calculate(List(doc1,doc2,doc3))
 result foreach(println)



}
