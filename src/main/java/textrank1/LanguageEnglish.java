package textrank1;

import opennlp.OpenNlpToolkit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tartarus.snowball.ext.englishStemmer;


public class LanguageEnglish
		extends LanguageModel
{
	// logging

	private final static Log LOG =
			LogFactory.getLog(LanguageEnglish.class.getName());

	  OpenNlpToolkit openNlpToolkit = new OpenNlpToolkit();

	public static englishStemmer stemmer_en = new englishStemmer();

	/**
	 * Split getSentences within the paragraph text.
	 */

	public String[]
	splitParagraph (final String text)
	{
		return openNlpToolkit.detectSentencesApplyNewlines(text).toArray(new String[]{});
	}


	/**
	 * Tokenize the sentence text into an array of tokens.
	 */

	public String[]
	tokenizeSentence (final String text)
	{

		return openNlpToolkit.tokenize(text);
	}


	/**
	 * Run a part-of-speech tagger on the sentence token list.
	 */

	public String[]
	tagTokens (final String[] token_list)
	{

		return openNlpToolkit.tagPartOfSpeech(token_list);

	}


	/**
	 * Prepare a stable key for a graph node (stemmed, lemmatized)
	 * from a token.
	 */

	public String
	getNodeKey (final String text, final String pos)
			throws Exception
	{
		return pos.substring(0, 2) + stemToken(scrubToken(text)).toLowerCase();
	}


	/**
	 * Determine whether the given PoS tag is a noun.
	 */

	public boolean
	isNoun (final String pos)
	{
		return pos.startsWith("NN");
	}


	/**
	 * Determine whether the given PoS tag is an adjective.
	 */

	public boolean
	isAdjective (final String pos)
	{
		return pos.startsWith("JJ");
	}


	/**
	 * Perform stemming on the given token.
	 */

	public String
	stemToken (final String token)
	{

		stemmer_en.setCurrent(token);
		stemmer_en.stem();

		return stemmer_en.getCurrent();
	}
}
