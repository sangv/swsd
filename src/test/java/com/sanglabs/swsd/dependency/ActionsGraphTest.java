package com.sanglabs.swsd.dependency;


import opennlp.OpenNlpToolkit;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * The ActionsGraphTest
 *
 * @author Sang Venkatraman
 */
public class ActionsGraphTest {

	ActionsGraph actionsGraph = new ActionsGraph();

	OpenNlpToolkit openNLPSentenceDetector = new OpenNlpToolkit();

	public void testDependencyAnalyze(String sentence,int numberOfActions){

		Set<Action> actions = actionsGraph.getActions(sentence,true);

		for(Action action: actions){
			System.out.println("Action: " + action.getSubject() +  " => " + action.getPredicate() + " => " + action.getObject());
		}

		try {
			Thread.currentThread().sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

	}

	@Test
	public void testLyrics(){

		String lyrics = LyricsPointOfViewTestDataProvider.boyNamedSueJohnyCash;
		List<String> sentences = Arrays.asList(openNLPSentenceDetector.detectSentencesApplyNewlines(lyrics));

		for(String sentence: sentences) {

			Set<Action> actions = actionsGraph.getActions(sentence,false);

			for(Action action: actions){
				System.out.println("Action: " + action.getSubject() +  " => " + action.getPredicate() + " => " + action.getObject());
			}

		}
	}

	public Object[][] sentencesToBeSimplified(){
		return new Object[][] {
				{ "John, who was CEO of the company, played golf.", 2 },
				{ "John, who is friends with Marcus, said he would play tennis with me sometime", 2 },
				{ "He didn't leave much to ma and me", 1},
				//{"I was born to be your Dead Sea",2},
				//{"Domestic life, it never suited you like a suitcase",3}
				};
	}
}
