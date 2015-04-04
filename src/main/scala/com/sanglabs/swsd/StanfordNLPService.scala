package com.sanglabs.swsd

import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.util.CoreMap
import grizzled.slf4j.Logger
import net.sf.extjwnl.data.POS

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 *
 * The StanfordNLPService provides access to certain StanfordNLP functionality like lemmatization, part of speech tagging etc.
 *
 * @author Sang Venkatraman
 *
 */
object StanfordNLPService {


  val props: Properties = new Properties()
  props.put("annotators", "tokenize, ssplit, pos")//lemma, ner, parse, dcoref


  val stanfordCoreNLPPipeline: StanfordCoreNLP = new StanfordCoreNLP(props)

  val logger = Logger[this.type]

  def sentences(text: String): List[String] = {

    val document: Annotation = new Annotation(text)

    // run all Annotators on this text
    stanfordCoreNLPPipeline.annotate(document)

    //document.get(classOf[SentencesAnnotation]).asScala map (p => {p.get(classOf[TokensAnnotation]).asScala.flatten.mkString(" ")})

    document.get(classOf[SentencesAnnotation]).asScala map (_.toString) toList
  }


  def analyze(documentText: String): List[Sentence] =
  {

    def convertPOS(pos: String): POS = pos match {
      case(pos) if pos.startsWith("NN") => POS.NOUN
      case(pos) if pos.startsWith("VB") => POS.VERB
      case(pos) if pos.startsWith("JJ") => POS.ADJECTIVE
      case(pos) if pos.startsWith("RB") => POS.ADVERB
      case _ => null
    }

    var sentences = ListBuffer[Sentence]()

    // create an empty Annotation just with the given text
    val document: Annotation = new Annotation(documentText)

    // run all Annotators on this text
    stanfordCoreNLPPipeline.annotate(document)

    val stanfordSentences: mutable.Buffer[CoreMap] = document.get(classOf[SentencesAnnotation]).asScala
    for(stanfordSentence: CoreMap <- stanfordSentences) {

      val sentence = mutable.ListBuffer[WordAnalysis]()

      for (token: CoreLabel <- stanfordSentence.get(classOf[TokensAnnotation]).asScala) {

        val pos = convertPOS(token.get(classOf[PartOfSpeechAnnotation]))
        val surfaceForm = token.get(classOf[OriginalTextAnnotation])
        sentence += WordAnalysis(surfaceForm, WordnetDictionaryService.getBaseForm(pos,surfaceForm), pos, token.get(classOf[PartOfSpeechAnnotation]))
      }
      sentences += Sentence(sentence.toList)
  }
    sentences.toList
  }




}

case class WordAnalysis(word: String, lemma: String, pos: POS, stanfordPOS: String)

case class Sentence(words: List[WordAnalysis], raw: String = "")
