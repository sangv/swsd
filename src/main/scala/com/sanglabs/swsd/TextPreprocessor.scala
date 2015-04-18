package com.sanglabs.swsd

/**
 *
 * The TextPreprocessor 
 *
 * @author Sang Venkatraman
 *
 */
object TextPreprocessor {

  def preprocess(text: String): String = {
     //preprocessing new lines

      val paragraphs = text.split("\n\n")
      val lines: Array[String] = paragraphs map {_.split("\n")} flatten
      val processedLines = lines.filterNot(l => {(l.startsWith("[") && l.endsWith("]")) || l.length < 1}) map {line => {if(!line.matches(".*\\p{P}$")) line + "." else line}}
      processedLines.mkString(" ")
  }

}
