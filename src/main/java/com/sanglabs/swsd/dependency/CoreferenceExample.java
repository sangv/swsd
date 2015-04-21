// Copyright (c) 2012 Kenzie Lane Mosaic, LLC. All rights reserved.
package com.sanglabs.swsd.dependency;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The CoreferenceExample
 *
 * @author Sang Venkatraman
 */
public class CoreferenceExample {

	public static void main(String[] args) {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		props.put("dcoref.score", true);
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		//Annotation document = new Annotation("The atom is a basic unit of matter, it   consists of a dense central nucleus surrounded by a cloud of negatively charged electrons.");
		Annotation document = new Annotation("John plays golf. He is the CEO of the company.");
		pipeline.annotate(document);

		Map<Integer, CorefChain> coref = document.get(CorefCoreAnnotations.CorefChainAnnotation.class);

		for(Map.Entry<Integer, CorefChain> entry : coref.entrySet()) {

			CorefChain c = entry.getValue();

			//this is because it prints out a lot of self references which aren't that useful
			if(c.getMentionsInTextualOrder().size() <= 1)
				continue;

			CorefChain.CorefMention cm = c.getRepresentativeMention();
			String clust = "";
			List<CoreLabel> tks = document.get(CoreAnnotations.SentencesAnnotation.class).get(cm.sentNum-1).get(CoreAnnotations.TokensAnnotation.class);
			for(int i = cm.startIndex-1; i < cm.endIndex-1; i++)
				clust += tks.get(i).get(CoreAnnotations.TextAnnotation.class) + " ";
			clust = clust.trim();
			System.out.println("representative mention: \"" + clust + "\" is mentioned by:");

			for(CorefChain.CorefMention m : c.getMentionsInTextualOrder()){
				String clust2 = "";
				tks = document.get(CoreAnnotations.SentencesAnnotation.class).get(m.sentNum-1).get(CoreAnnotations.TokensAnnotation.class);
				for(int i = m.startIndex-1; i < m.endIndex-1; i++)
					clust2 += tks.get(i).get(CoreAnnotations.TextAnnotation.class) + " ";
				clust2 = clust2.trim();
				//don't need the self mention
				if(clust.equals(clust2))
					continue;

				System.out.println("\t" + clust2);
			}
		}
	}
}
