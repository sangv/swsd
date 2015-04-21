package com.sanglabs.swsd.dependency;


/**
 * The ActionsGraphTest
 *
 * @author Sang Venkatraman
 */
public class ActionsGraphProducer {


	public static void generateDependencyGraphs(){

		StanfordNLPDependencyGraphProvider dependencyGraphProvider = new StanfordNLPDependencyGraphProvider();
		dependencyGraphProvider.dependencyGraph("The quick brown fox jumps over the lazy dog.");
		dependencyGraphProvider.dependencyGraph("John, who is CEO of the company, plays golf.");
		dependencyGraphProvider.dependencyGraph("Siri, Where is the nearest Starbucks?");
		dependencyGraphProvider.dependencyGraph("Siri, Where can I withdraw some money?");
		dependencyGraphProvider.dependencyGraph("Siri, Where is the nearest mexican restaurant?");
		dependencyGraphProvider.dependencyGraph("Siri, Which is the nearest mexican food place?");
		dependencyGraphProvider.dependencyGraph("Siri, Are we friends?");
		dependencyGraphProvider.dependencyGraph("Siri, Are you my friend?");
		dependencyGraphProvider.dependencyGraph("I do not play golf.");
	}

	public static void main(String[] args) {
		generateDependencyGraphs();
	}

}
