package com.sanglabs.swsd

import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.util.CoreMap
import net.sf.extjwnl.data.POS

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 *
 * The StanfordNLPService provides access to certain StanfordNLP functionality like lemmatization, named entity recognition etc.
 *
 * @author Sang Venkatraman
 *
 */
object StanfordNLPService {


  val props: Properties = new Properties()
  props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref")


  val stanfordCoreNLPPipeline: StanfordCoreNLP = new StanfordCoreNLP(props)

  //using stopwords from http://www.ranks.nl/stopwords
  val stopWords: List[String] = scala.io.Source.fromFile("data/stopwords.txt").getLines.toList


  def analyze(documentText: String): List[WordAnalysis] =
  {

    def convertPOS(pos: String): POS = pos match {
      case(pos) if pos.startsWith("NN") => POS.NOUN
      case(pos) if pos.startsWith("VB") => POS.VERB
      case(pos) if pos.startsWith("JJ") => POS.ADJECTIVE
      case(pos) if pos.startsWith("RB") => POS.ADVERB
      case _ => null
    }

    var lemmas: List[WordAnalysis] = List[WordAnalysis]()

    // create an empty Annotation just with the given text
    val document: Annotation = new Annotation(documentText)

    // run all Annotators on this text
    stanfordCoreNLPPipeline.annotate(document)

    // Iterate over all of the sentences found
    val sentences: mutable.Buffer[CoreMap] = document.get(classOf[SentencesAnnotation]).asScala
    for(sentence: CoreMap <- sentences) {
      // Iterate over all tokens in a sentence
      for (token: CoreLabel <- sentence.get(classOf[TokensAnnotation]).asScala) {
        // Retrieve and add the lemma for each word into the list of lemmas
        if(!stopWords.contains(token.get(classOf[LemmaAnnotation]).toLowerCase)) {
          lemmas = lemmas :+ WordAnalysis(token.get(classOf[LemmaAnnotation]), token.get(classOf[LemmaAnnotation]), convertPOS(token.get(classOf[PartOfSpeechAnnotation])))
        }
      }
  }
    lemmas
  }




}

case class WordAnalysis(word: String, lemma: String, pos: POS)
