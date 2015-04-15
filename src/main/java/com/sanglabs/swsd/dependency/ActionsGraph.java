package com.sanglabs.swsd.dependency;

import com.chaoticity.dependensee.Main;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import org.jgrapht.ListenableGraph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.Multigraph;

import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public final class ActionsGraph
{


	private StanfordNLPDependencyGraphProvider stanfordNLPDependencyGraphProvider = new StanfordNLPDependencyGraphProvider();

	private static List<String> subjectTags = Arrays.asList(new String[]{"nsubj", "nsubjpass", "xsubj", "rcmod"});//"rcmod"

	private static List<String> objectTags =  Arrays.asList(new String[]{"dobj"});//,"prep","advmod","cop"

	private LexicalizedParser lexicalizedParser;

	private TokenizerFactory tokenizerFactory;

	public ActionsGraph() {
		lexicalizedParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
	}

	public Set<Action> getActions(String string, boolean... drawImage){

		//List<String> strings = openNLPSentenceDetector.getSentences(lyrics);//get strings from entire lyrics -- for later

		Set<Action> actions = new LinkedHashSet<>();
		GrammaticalStructure grammaticalStructure = stanfordNLPDependencyGraphProvider.dependencyGraph(string);

		List<TypedDependency> typedDependencies = grammaticalStructure.typedDependenciesCCprocessed(true);
		System.out.println(typedDependencies);

		ListenableGraph<String, RelationshipEdge> graph = buildGraphAndPopulateEdges(typedDependencies);

		if(drawImage != null && drawImage.length > 0 && drawImage[0]){
			try {

				System.out.println(graph);

				List wordList = tokenizerFactory.getTokenizer(new StringReader(string)).tokenize();
				Tree tree = lexicalizedParser.apply(wordList);
				Main.writeImage(tree, typedDependencies, string.substring(0,20) + ".png", 2);
			} catch (Exception e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}

		Set<String> subjects = new LinkedHashSet<>();
		Set<String> objects = new LinkedHashSet<>();

		for(TypedDependency typedDependency: typedDependencies){
			//System.out.println(typedDependency.reln().getShortName());
			if(subjectTags.contains(typedDependency.reln().getShortName())){ //fixme
				subjects.add(typedDependency.dep().nodeString());
			} else if (objectTags.contains(typedDependency.reln().getShortName())){//fixme
				objects.add(typedDependency.dep().nodeString());
			}
		}


		return actions;

	}

	public ListenableGraph<String, RelationshipEdge> buildGraphAndPopulateEdges(List<TypedDependency> typedDependencies){
		ListenableGraph<String, RelationshipEdge> graph =
				new DefaultListenableGraph<String, RelationshipEdge>(new Multigraph<String, RelationshipEdge>(
						new ClassBasedEdgeFactory<String, RelationshipEdge>(RelationshipEdge.class)));

		for(TypedDependency typedDependency: typedDependencies){
			String gov = typedDependency.gov().nodeString();
			String dep = typedDependency.dep().nodeString();
			String rel = typedDependency.reln().getShortName();

			graph.addVertex(gov);
			graph.addVertex(dep);
			try {
				graph.addEdge(gov,dep,new RelationshipEdge(gov,dep,rel));//TODO makes sense?
			} catch (Exception e) {
				//e.printStackTrace();  //FIXME
			}

		}
		//graph.
		return graph;
	}

	public static class RelationshipEdge<V> extends DefaultEdge {
		private V v1;
		private V v2;
		private String label;

		public RelationshipEdge(V v1, V v2, String label) {
			this.v1 = v1;
			this.v2 = v2;
			this.label = label;
		}

		public V getV1() {
			return v1;
		}

		public V getV2() {
			return v2;
		}

		public String getLabel() {
			return label;
		}

		@Override
		public String toString() {
			return label;
		}




	}
}
