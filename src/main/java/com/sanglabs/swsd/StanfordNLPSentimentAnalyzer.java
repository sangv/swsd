package com.sanglabs.swsd;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;



import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;


import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The StanfordNLPSentimentAnalyzer
 *
 * @author Sang Venkatraman
 */
public class StanfordNLPSentimentAnalyzer {

	String sentimentModel;

	String parserModel;

	StanfordCoreNLP stanfordCoreNLPPipeline;

	private final static Logger LOGGER = LoggerFactory.getLogger(StanfordNLPSentimentAnalyzer.class);

	public enum Sentiment {VERY_NEGATIVE, NEGATIVE, NEUTRAL, POSITIVE, VERY_POSITIVE, UNKNOWN};

	public enum AggregateFunction {MEAN,MEDIAN,LOWEST_SENTIMENT_SCORE,FIRST_SENTENCE_SENTIMENT,MOST_COMMON_SENTIMENT}

	public Map<Sentiment,Integer> sentimentScoreMap = new LinkedHashMap<>();//ordered

	public StanfordNLPSentimentAnalyzer() {

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
		if (sentimentModel != null) {
			props.setProperty("sentiment.model", sentimentModel);
		}
		if (parserModel != null) {
			props.setProperty("parse.model", parserModel);
		}
		stanfordCoreNLPPipeline = new StanfordCoreNLP(props);

		sentimentScoreMap.put(Sentiment.VERY_NEGATIVE,0);
		sentimentScoreMap.put(Sentiment.NEGATIVE,1);
		sentimentScoreMap.put(Sentiment.NEUTRAL,2);
		sentimentScoreMap.put(Sentiment.POSITIVE,3);
		sentimentScoreMap.put(Sentiment.VERY_POSITIVE,4);

		sentimentScoreMap.put(Sentiment.UNKNOWN,0);//FIXME will work for mean but fail for others


	}

	public Sentiment getSentiment(String text){


		Sentiment sentimentString = Sentiment.UNKNOWN;

		Annotation annotation = new Annotation(text);
		stanfordCoreNLPPipeline.annotate(annotation);

		for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
			Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
			int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
			LOGGER.debug(sentence.toString());
			sentimentString = getSentiment(sentiment);
			LOGGER.debug("Predicted sentiment: " + sentimentString);
		}

		return sentimentString;
	}

	public static Sentiment getSentiment(int sentiment) {
		switch(sentiment) {
			case 0:
				return Sentiment.VERY_NEGATIVE;
			case 1:
				return Sentiment.NEGATIVE;
			case 2:
				return Sentiment.NEUTRAL;
			case 3:
				return Sentiment.POSITIVE;
			case 4:
				return Sentiment.VERY_POSITIVE;
			default:
				return Sentiment.UNKNOWN;
		}
	}

	public Sentiment aggregateSentiments(List<Sentiment> sentiments, AggregateFunction aggregateFunction) {
		List<Integer> sentimentValues = new ArrayList<>();
		Sentiment overallSentiment = null;
		Integer sum = 0;
		for(Sentiment sentiment: sentiments){
			if(sentimentScoreMap.containsKey(sentiment)){
				sum = sum + sentimentScoreMap.get(sentiment);
				sentimentValues.add(sentimentScoreMap.get(sentiment));
			}
		}

		if(aggregateFunction == null || aggregateFunction.equals(AggregateFunction.MEAN)){
			overallSentiment = getSentiment(sum/sentimentValues.size());
		} else if(aggregateFunction.equals(AggregateFunction.MEDIAN)) {
			Collections.sort(sentimentValues);
			overallSentiment = getSentiment(sentimentValues.get(sentimentValues.size()/2));
		} else if(aggregateFunction.equals(AggregateFunction.LOWEST_SENTIMENT_SCORE)){
			Collections.sort(sentimentValues);
			overallSentiment = getSentiment(sentimentValues.get(0));
		} else if(aggregateFunction.equals(AggregateFunction.FIRST_SENTENCE_SENTIMENT)){
			overallSentiment = sentiments.get(0);
		} else if(aggregateFunction.equals(AggregateFunction.MOST_COMMON_SENTIMENT)){
			Multiset<Sentiment> sentimentMultiset = HashMultiset.create(sentiments);

			for(Sentiment sentiment:sentimentScoreMap.keySet()){
				if(sentimentMultiset.count(sentiment) > sentimentMultiset.count(overallSentiment)){
					overallSentiment = sentiment;//FIXME handle equals
				}
			}
		}


		LOGGER.info("Got overall sentiment {} from sentence sentiments {} using aggregate function {}",new Object[]{sentiments,overallSentiment,aggregateFunction});
		return overallSentiment;
	}
}
