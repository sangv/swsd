package com.sanglabs.swsd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import static com.sanglabs.swsd.SentiWordNetService.Sentiment.*;


/**
 * The SentiWordNetService
 *
 * @author Sang Venkatraman
 */
public class SentiWordNetService {


	private String pathToSWN = "/Users/sang/Temp/swsd/data/SentiWordNet_3.0.0_20130122.txt";

	private HashMap<String, Sentiment> _dict = new HashMap<>();

	public enum Sentiment {STRONG_POSITIVE, POSITIVE, WEAK_POSITIVE, NONE, WEAK_NEGATIVE, NEGATIVE, STRONG_NEGATIVE};

	private final static Logger LOGGER = LoggerFactory.getLogger(SentiWordNetService.class);

	public SentiWordNetService(){

		HashMap<String, Vector<Double>> _temp = new HashMap<String, Vector<Double>>();
		try{
			BufferedReader csv =  new BufferedReader(new FileReader(pathToSWN));
			String line = "";
			while((line = csv.readLine()) != null)
			{
				if(!line.trim().startsWith("#")){
					String[] data = line.split("\t");
					Double score = Double.parseDouble(data[2])-Double.parseDouble(data[3]);
					String[] words = data[4].split(" ");
					for(String w:words)
					{
						String[] w_n = w.split("#");
						String pos = data[0];
						String key = w_n[0] + "#" + pos + "#" + w_n[1];
						int index = Integer.parseInt(w_n[1])-1;
						if(_temp.containsKey(key))
						{
							Vector<Double> v = _temp.get(key);
							if(index>v.size())
								for(int i = v.size();i<index; i++)
									v.add(0.0);
							v.add(index, score);
							_temp.put(key, v);
						}
						else
						{
							Vector<Double> v = new Vector<Double>();
							for(int i = 0;i<index; i++)
								v.add(0.0);
							v.add(index, score);
							_temp.put(key, v);
						}
					}
				}
			}
			Set<String> temp = _temp.keySet();
			for (Iterator<String> iterator = temp.iterator(); iterator.hasNext();) {
				String word = (String) iterator.next();
				Vector<Double> v = _temp.get(word);
				double score = 0.0;
				double sum = 0.0;
				for(int i = 0; i < v.size(); i++)
					score += ((double)1/(double)(i+1))*v.get(i);
				for(int i = 1; i<=v.size(); i++)
					sum += (double)1/(double)i;
				score /= sum;

				Sentiment sent = NONE;
				if(score>=0.75)
					sent = STRONG_POSITIVE;
				else
				if(score > 0.25 && score<=0.5)
					sent = POSITIVE;
				else
				if(score > 0 && score>=0.25)
					sent = WEAK_POSITIVE;
				else
				if(score < 0 && score>=-0.25)
					sent = WEAK_NEGATIVE;
				else
				if(score < -0.25 && score>=-0.5)
					sent = NEGATIVE;
				else
				if(score<=-0.75)
					sent = STRONG_NEGATIVE;
				_dict.put(word, sent);
			}
		}
		catch(Exception e){e.printStackTrace();}
	}

	public Sentiment extract(String synset)
	{
		return _dict.get(synset);
	}


}
