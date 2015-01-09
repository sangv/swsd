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
  props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref")


  val stanfordCoreNLPPipeline: StanfordCoreNLP = new StanfordCoreNLP(props)

  val logger = Logger[this.type]


  def analyze(documentText: String): List[Sentence] =
  {

    def convertPOS(pos: String): POS = pos match {
      case(pos) if pos.startsWith("NN") => POS.NOUN
      case(pos) if pos.startsWith("VB") => POS.VERB
      case(pos) if pos.startsWith("JJ") => POS.ADJECTIVE
      case(pos) if pos.startsWith("RB") => POS.ADVERB
      case _ => null
    }

    var rawSentences = ListBuffer[Sentence]()

    // create an empty Annotation just with the given text
    val document: Annotation = new Annotation(documentText)

    //val ignoreWords = mutable.MutableList[String]()

    // run all Annotators on this text
    stanfordCoreNLPPipeline.annotate(document)

    // Iterate over all of the sentences found
    val sentences: mutable.Buffer[CoreMap] = document.get(classOf[SentencesAnnotation]).asScala
    for(sentence: CoreMap <- sentences) {
      // Iterate over all tokens in a sentence
      val rawSentence = mutable.ListBuffer[WordAnalysis]()
      for (token: CoreLabel <- sentence.get(classOf[TokensAnnotation]).asScala) {

        // Retrieve and add the lemma for each word into the list of lemmas
        //if(!(stopWords.contains(token.get(classOf[OriginalTextAnnotation]).toLowerCase) || stopWords.contains(token.get(classOf[LemmaAnnotation]).toLowerCase))) {
        val pos = convertPOS(token.get(classOf[PartOfSpeechAnnotation]))
        val surfaceForm = token.get(classOf[OriginalTextAnnotation])
        rawSentence += WordAnalysis(surfaceForm, WordnetDictionaryService.getBaseForm(pos,surfaceForm), pos)
        /*} else {
          ignoreWords += token.get(classOf[OriginalTextAnnotation])
        }*/
      }
      rawSentences += Sentence(rawSentence.toList)
  }
    //logger.info(s"Ignored words => ${ignoreWords.mkString(", ")}")
    rawSentences.toList
  }




}

case class WordAnalysis(word: String, lemma: String, pos: POS)

case class Sentence(words: List[WordAnalysis])
