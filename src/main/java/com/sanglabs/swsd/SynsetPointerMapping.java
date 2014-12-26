package com.sanglabs.swsd;

import java.util.HashMap;
import java.util.Map;

/**
 * The SynsetPointerMapping provides the mapping for all the different wordnet relationships and their acronyms
 *
 * @author Sang Venkatraman
 */

public class SynsetPointerMapping {

	public static Map<String,String> nounPointerTypeMap = new HashMap<String,String>();

	public static Map<String,String> verbPointerTypeMap = new HashMap<String,String>();

	public static Map<String,String> adjPointerTypeMap = new HashMap<String,String>();

	public static Map<String,String> adverbPointerTypeMap = new HashMap<String,String>();

	static{
		nounPointerTypeMap.put("!","Antonym");
		nounPointerTypeMap.put("@","Hypernym");
		nounPointerTypeMap.put("@i","Instance_Hypernym");
		nounPointerTypeMap.put("~","Hyponym");
		nounPointerTypeMap.put("~i","Instance_Hyponym");
		nounPointerTypeMap.put("#m","Member_Holonym");
		nounPointerTypeMap.put("#s","Substance_Holonym");
		nounPointerTypeMap.put("#p","Part_Holonym");
		nounPointerTypeMap.put("%m","Member_Meronym");
		nounPointerTypeMap.put("%s","Substance_Meronym");
		nounPointerTypeMap.put("%p","Part_Meronym");
		nounPointerTypeMap.put("=","Attribute");
		nounPointerTypeMap.put("+","Derivationally_Related_Form");
		nounPointerTypeMap.put(";c","Domain_Of_Synset_TOPIC");
		nounPointerTypeMap.put("-c","Member_Of_This_Domain_TOPIC");
		nounPointerTypeMap.put(";r","Domain_Of_Synset_REGION");
		nounPointerTypeMap.put("-r","Member_Of_This_Domain_REGION");
		nounPointerTypeMap.put(";u","Domain_Of_Synset_USAGE");
		nounPointerTypeMap.put("-u","Member_Of_This_Domain_USAGE");

		verbPointerTypeMap.put("!","Antonym");
		verbPointerTypeMap.put("@","Hypernym");
		verbPointerTypeMap.put("~","Hyponym");
		verbPointerTypeMap.put("*","Entailment");
		verbPointerTypeMap.put(">","Cause");
		verbPointerTypeMap.put("^","Also_See");
		verbPointerTypeMap.put("$","Verb_Group");
		verbPointerTypeMap.put("+","Derivationally_Related_Form");
		verbPointerTypeMap.put(";c","Domain_Of_Synset_TOPIC");
		verbPointerTypeMap.put(";r","Domain_Of_Synset_REGION");
		verbPointerTypeMap.put(";u","Domain_Of_Synset_USAGE");

		adjPointerTypeMap.put("!","Antonym");
		adjPointerTypeMap.put("&","Similar_To");
		adjPointerTypeMap.put("<","Participle_Of_Verb");
		adjPointerTypeMap.put("\\","Pertainym_(pertains_To_Noun)");
		adjPointerTypeMap.put("=","Attribute");
		adjPointerTypeMap.put("^","Also_See");
		adjPointerTypeMap.put(";c","Domain_Of_Synset_TOPIC");
		adjPointerTypeMap.put(";r","Domain_Of_Synset_REGION");
		adjPointerTypeMap.put(";u","Domain_Of_Synset_USAGE");
		adjPointerTypeMap.put("+","Derivationally_Related_Form");//Added manually not in http://wordnet.princeton.edu/man/wninput.5WN.html

		adverbPointerTypeMap.put("!","Antonym");
		adverbPointerTypeMap.put("\\","Derived_From_Adjective");
		adverbPointerTypeMap.put(";c","Domain_Of_Synset_TOPIC");
		adverbPointerTypeMap.put(";r","Domain_Of_Synset_REGION");
		adverbPointerTypeMap.put(";u","Domain_Of_Synset_USAGE");
		adverbPointerTypeMap.put("+","Derivationally_Related_Form");//Added manually not in http://wordnet.princeton.edu/man/wninput.5WN.html


	}
}
