package com.sanglabs.swsd.dependency;

import com.chaoticity.dependensee.Main;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ObjectArrays;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.*;
import opennlp.OpenNlpToolkit;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.*;

import static com.sanglabs.swsd.dependency.StanfordNLPDependencyGraphProvider.PointOfView.*;


public class StanfordNLPDependencyGraphProvider {

	TokenizerFactory tokenizerFactory;

	LexicalizedParser lexicalizedParser;

	GrammaticalStructureFactory grammaticalStructureFactory;

	OpenNlpToolkit openNLPSentenceDetector = new OpenNlpToolkit();

	private final static Logger LOGGER = LoggerFactory.getLogger(StanfordNLPDependencyGraphProvider.class);

	private String[] firstPersonPronouns = {"I","Me","Mine"};

	private String[] secondPersonPronouns = {"You","Your"};

	private static List<String> subjectTags = Arrays.asList(new String[]{"nsubj"});

	private static List<String> objectTags =  Arrays.asList(new String[]{"dobj","advmod"});

	public enum PointOfView  {THIRD_PERSON, FIRST_PERSON, SECOND_PERSON, DIRECT_ADDRESS};

	public StanfordNLPDependencyGraphProvider() {
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		grammaticalStructureFactory = tlp.grammaticalStructureFactory();
		lexicalizedParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		lexicalizedParser.setOptionFlags(new String[]{"-maxLength", "1500", "-retainTmpSubcategories"});
		tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
	}

	public List<Tree> getNounPhrases(String lineOfText)
	{
		List wordList = tokenizerFactory.getTokenizer(new StringReader(lineOfText)).tokenize();
		Tree tree = lexicalizedParser.apply(wordList);

		List<Tree> phraseList=new ArrayList<Tree>();
		for (Tree subtree: tree)
		{

			if(subtree.label().value().equals("NP"))
			{

				phraseList.add(subtree);
				System.out.println(subtree);

			}
		}

		return phraseList;

	}

	public PointOfView getPointOfView(Set<Action> actions){
		Set<String> subjects = new LinkedHashSet<String>();

		for(Action action:actions){
			subjects.add(action.getSubject());
		}

		boolean firstPerson = isPointOfView(subjects,firstPersonPronouns);
		boolean secondPerson = isPointOfView(subjects,secondPersonPronouns);

		if(firstPerson && !secondPerson){
			return FIRST_PERSON;
		} else if (secondPerson && !firstPerson){
			return SECOND_PERSON;
		} else if (firstPerson && secondPerson){
			return DIRECT_ADDRESS;
		}

		return THIRD_PERSON;
	}

	public Set<Action> filterByPointOfView(Set<Action> actions, PointOfView pointOfView) {

		switch (pointOfView){
			case FIRST_PERSON: return filter(actions,firstPersonPronouns);
			case SECOND_PERSON:return filter(actions, secondPersonPronouns);
			case DIRECT_ADDRESS:return filter(actions, ObjectArrays.concat(firstPersonPronouns, secondPersonPronouns, String.class));
			default: return actions;
		}
	}

	protected Set<Action> filter(Set<Action> actions, String[] pronouns){
		Set<Action> filteredActions = new LinkedHashSet<Action>();
		for(Action action: actions){
			if(Arrays.asList(pronouns).contains(action.getSubject())) {
				filteredActions.add(action);
			}
		}
		return filteredActions;
	}

	public Set<Action> parse(String text) {

		Set<Action> actions = new LinkedHashSet<Action>();

		List<String> lines = openNLPSentenceDetector.detectSentencesApplyNewlines(text);

		for(String line: lines){

			List wordList = tokenizerFactory.getTokenizer(new StringReader(line)).tokenize();
			Tree tree = lexicalizedParser.apply(wordList);

			GrammaticalStructure gs = grammaticalStructureFactory.newGrammaticalStructure(tree);
			Collection<TypedDependency> tdl = gs.typedDependenciesCCprocessed(true);




			//do a sentence split here somewhere
			Multimap<String,String> subjectActions =  HashMultimap.create();

			Multimap<String,String> objectActions =  HashMultimap.create();
			for(TypedDependency dependency: tdl){

				GrammaticalRelation relation = dependency.reln();
				if (subjectTags.contains(relation.getShortName())){
					subjectActions.put(dependency.dep().nodeString(), dependency.gov().nodeString());
				}
				else if (objectTags.contains(relation.getShortName())){
					objectActions.put(dependency.gov().nodeString(), dependency.dep().nodeString());
				}

			}

			Map<String,Collection<String>> objectActionMap = objectActions.asMap();
			for(Map.Entry<String,Collection<String>> actionEntry: subjectActions.asMap().entrySet()){
				Collection<String> predicates = actionEntry.getValue();
				for(String predicate: predicates){
					if(objectActionMap.containsKey(predicate)){
						Collection<String> objects = objectActionMap.get(predicate);
						for(String object:objects){
							Action action  = new Action(WordUtils.capitalize(actionEntry.getKey()),predicate,object);
							actions.add(action);
						}
					}
				}
			}

		}

		return actions;

	}

	public GrammaticalStructure dependencyGraph(String text) {

		List<String> lines = Arrays.asList(openNLPSentenceDetector.detectSentences(text));

		GrammaticalStructure grammaticalStructure = null;

		int lineNumber = 0;
		for(String line: lines){

			List wordList = tokenizerFactory.getTokenizer(new StringReader(line)).tokenize();
			Tree tree = lexicalizedParser.apply(wordList);
			grammaticalStructure = grammaticalStructureFactory.newGrammaticalStructure(tree);
			Collection<TypedDependency> tdl = grammaticalStructure.typedDependenciesCCprocessed(true);

			try {
				++lineNumber;
				Main.writeImage(tree, tdl, "sue" + lineNumber + ".png", 3);
			} catch (Exception e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			//do a sentence split here somewhere
			Multimap <String,String> subjectActions =  HashMultimap.create();

			Multimap<String,String> objectActions =  HashMultimap.create();
			for(TypedDependency dependency: tdl){

				GrammaticalRelation relation = dependency.reln();
				if (relation.getShortName().endsWith("subj")){
					subjectActions.put(dependency.dep().nodeString(), dependency.gov().nodeString());
				}
				else { //if(relation.getShortName().equals("dobj")){
					objectActions.put(dependency.gov().nodeString(), dependency.dep().nodeString());
				}

			}



		}

		return grammaticalStructure;

	}

	public boolean isPointOfView(Set<String> subjects, String[] pronouns){
		for(String pronoun: pronouns){
			if(subjects.contains(pronoun)){
				return true;
			}
		}
		return false;
	}

}

