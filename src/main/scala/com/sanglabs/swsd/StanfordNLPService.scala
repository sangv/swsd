package com.sanglabs.swsd

import java.util.Properties

import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.tagger.maxent.MaxentTagger
import edu.stanford.nlp.util.CoreMap
import grizzled.slf4j.Logger
import net.sf.extjwnl.data.POS

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.xml.XML

/**
 *
 * The StanfordNLPService provides access to certain StanfordNLP functionality like lemmatization, part of speech tagging etc.
 *
 * @author Sang Venkatraman
 *
 */
object StanfordNLPService {


  val props: Properties = new Properties()
  props.put("annotators", "tokenize, ssplit, pos")
  //lemma, ner, parse, dcoref


  val stanfordCoreNLPPipeline: StanfordCoreNLP = new StanfordCoreNLP(props)

  lazy val maxentPOSTagger = new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger")

  //TODO change to relative path
  val nerClassifier = CRFClassifier.getClassifierNoExceptions("/Users/sang/Temp/swsd/data/stanfordnlp/ner/english.all.3class.caseless.distsim.crf.ser.gz")

  val logger = Logger[this.type]

  def getSentences(text: String): List[String] = {

    val document: Annotation = new Annotation(text)

    // run all Annotators on this text
    stanfordCoreNLPPipeline.annotate(document)

    //document.get(classOf[SentencesAnnotation]).asScala map (p => {p.get(classOf[TokensAnnotation]).asScala.flatten.mkString(" ")})

    document.get(classOf[SentencesAnnotation]).asScala map (_.toString) toList
  }


  def analyze(documentText: String): List[Sentence] = {

    def convertPOS(pos: String): POS = pos match {
      case (pos) if pos.startsWith("NN") => POS.NOUN
      case (pos) if pos.startsWith("VB") => POS.VERB
      case (pos) if pos.startsWith("JJ") => POS.ADJECTIVE
      case (pos) if pos.startsWith("RB") => POS.ADVERB
      case _ => null
    }

    var sentences = ListBuffer[Sentence]()

    // create an empty Annotation just with the given text
    val document: Annotation = new Annotation(TextPreprocessor.preprocess(documentText))

    // run all Annotators on this text
    stanfordCoreNLPPipeline.annotate(document)

    val stanfordSentences: mutable.Buffer[CoreMap] = document.get(classOf[SentencesAnnotation]).asScala
    for (stanfordSentence: CoreMap <- stanfordSentences) {

      val sentence = mutable.ListBuffer[WordAnalysis]()

      for (token: CoreLabel <- stanfordSentence.get(classOf[TokensAnnotation]).asScala) {

        val pos = convertPOS(token.get(classOf[PartOfSpeechAnnotation]))
        val surfaceForm = token.get(classOf[OriginalTextAnnotation])
        sentence += WordAnalysis(surfaceForm, WordNetDictionaryService.getBaseForm(pos, surfaceForm), pos, token.get(classOf[PartOfSpeechAnnotation]))
      }
      sentences += Sentence(sentence.toList)
    }
    sentences.toList
  }

  def nerSpots(text: String): Map[String, String] = {
    //import scala.collection.JavaConversions._
    //val out: util.List[util.List[CoreLabel]] = nerClassifier.classify(text)
    //out.flatten filterNot {word => word.get(classOf[AnswerAnnotation]) == "O"} map {(word:CoreLabel) => (word.ner() -> word.get(classOf[AnswerAnnotation]))} toMap
    val nerString = nerClassifier.classifyWithInlineXML(text)

    val xmlString = XML.loadString(s"<text>${nerString}</text>")
    var results = Map[String, String]()
    List("ORGANIZATION", "LOCATION", "PERSON") foreach {
      xmlString \ _ foreach (n => {
        results += (n.text -> n.label)
      })
    }
    results
  }

}

case class WordAnalysis(word: String, lemma: String, pos: POS, stanfordPOS: String)

case class Sentence(words: List[WordAnalysis], raw: String = "")
