package com.sanglabs.swsd;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.dictionary.AbstractCachingDictionary;
import net.sf.extjwnl.dictionary.Dictionary;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 *  The Neo4jWordnetDataLoader takes the file based dictionary and puts it into Neo4j format. Note, there is no need to run this file if you
 *  already have the data built in the Neo4j graph format.
 *
 *  @author Sang Venkatraman
 */
public class Neo4jWordnetDataLoader {

	private final static Logger LOGGER = LoggerFactory.getLogger(Neo4jWordnetDataLoader.class);

	private static final String DEST = "data/wordnetgraph.db";

	private static final String FILE_PROPERTIES_LOCATION = "data/file_properties.xml";

	protected final Map<POS,Map<String,String>> pointerTypeSymbolMultiMap = new HashMap<>();

	protected final Map<POS,Map<Long, long[]>> indexWordIdToSynsetOffset;

	/**
	 * Mapping of synset offset id's to database id's. 1:1.
	 */
	protected final Map<POS,Map<Long, Long>> synsetOffsetToSynsetId;

	protected final Dictionary dictionary;

	protected final GraphDatabaseService graphDb;

	public enum RelationshipType {Synset, IndexWord, Synsets, Words};

	Index<Node> indexWordIndex = null;

	Index<Node> synsetIndex = null;

	public static void main(String args[]) {
		try {
			new Neo4jWordnetDataLoader(Dictionary.getInstance(new FileInputStream(FILE_PROPERTIES_LOCATION))).insertData();
		} catch (JWNLException| FileNotFoundException e) {
			LOGGER.error(e.getLocalizedMessage(),e);
		}
	}

	/**
	 * Create a new DictionaryToDatabase with a database connection. JWNL already initialized.
	 *
	 * @param dictionary the dictionary
	 */
	public Neo4jWordnetDataLoader(Dictionary dictionary) {
		this.dictionary = dictionary;

		indexWordIdToSynsetOffset = new HashMap<POS,Map<Long, long[]>>();

		synsetOffsetToSynsetId = new HashMap<POS,Map<Long, Long>>();

		if (dictionary instanceof AbstractCachingDictionary) {
			((AbstractCachingDictionary) dictionary).setCachingEnabled(false);
		}

		deleteFileOrDirectory( new File( DEST ) );
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DEST);
		registerShutdownHook( graphDb );
		Transaction tx = graphDb.beginTx();
		try {
			indexWordIndex = graphDb.index().forNodes("indexword");
			synsetIndex = graphDb.index().forNodes("synset");

			tx.success();
		} catch (Exception e) {
			tx.failure();
			LOGGER.error(e.getLocalizedMessage(),e);
		} finally {
			tx.close();
		}

		pointerTypeSymbolMultiMap.put(POS.NOUN, SynsetPointerMapping.nounPointerTypeMap);
		pointerTypeSymbolMultiMap.put(POS.VERB, SynsetPointerMapping.verbPointerTypeMap);
		pointerTypeSymbolMultiMap.put(POS.ADJECTIVE, SynsetPointerMapping.adjPointerTypeMap);
		pointerTypeSymbolMultiMap.put(POS.ADVERB, SynsetPointerMapping.adverbPointerTypeMap);
	}

	/**
	 * Inserts the data into the database. Iterates through the various POS,
	 * then stores all the index words, synsets, exceptions of that POS.
	 *
	 * @throws net.sf.extjwnl.JWNLException JWNLException
	 * @throws java.sql.SQLException  SQLException
	 */
	public void insertData() throws JWNLException {

		try {
			for (POS pos : POS.getAllPOS()) {
				LOGGER.info("inserting data for pos " + pos);

				indexWordIdToSynsetOffset.put(pos,new HashMap<Long,long[]>());
				synsetOffsetToSynsetId.put(pos,new HashMap<Long,Long>());

				storeIndexWords(dictionary.getIndexWordIterator(pos),pos);
				storeSynsets(dictionary.getSynsetIterator(pos),pos);
				buildRelationshipBetweenIndexWordAndSynsets(pos);
				storeExceptions(dictionary.getExceptionIterator(pos));

				LOGGER.info("done inserting data for pos " + pos);
			}

			for(POS pos: POS.getAllPOS()){
				buildRelationshipBetweenSynsets(dictionary.getSynsetIterator(pos), pointerTypeSymbolMultiMap.get(pos),pos);
			}

			indexWordIdToSynsetOffset.clear();
			synsetOffsetToSynsetId.clear();
		} catch (JWNLException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} finally {

		}
		try {
			Thread.currentThread().sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	/**
	 * Store all the index words.
	 *
	 * @param itr - the index word iterator
	 * @throws java.sql.SQLException SQLException
	 */
	protected void storeIndexWords(Iterator<IndexWord> itr,POS pos) {
		LOGGER.info("Storing Index words");

		Transaction tx = graphDb.beginTx();
		int count = 0;
		try {
			while (itr.hasNext()) {
				if (count % 10000 == 0) {
					LOGGER.info("indexword count: " + count);
				}
				count++;
				IndexWord indexWord = itr.next();
				Node node = graphDb.createNode();
				node.setProperty("lemma", indexWord.getLemma());
				node.setProperty("pos", indexWord.getPOS().getKey());
				indexWordIndex.add( node, "lemma", indexWord.getLemma() );
				indexWordIndex.add(node, "pos", indexWord.getPOS());
				indexWordIdToSynsetOffset.get(pos).put(node.getId(), indexWord.getSynsetOffsets());
			}
			tx.success();
		} catch (Exception e) {
			tx.failure();
			LOGGER.error(e.getLocalizedMessage(),e);
		} finally {
			tx.close();
		}

		LOGGER.info("Stored Index words: " + count);
	}

	protected void storeSynsets(Iterator<Synset> itr, POS pos) {

		Transaction tx = graphDb.beginTx();
		LOGGER.info("Storing Synsets");
		int count = 0;
		try {
			while (itr.hasNext()) {
				if (count % 10000 == 0) {
					LOGGER.info("synset count: " + count);
				}
				count++;

				Synset synset = itr.next();
				Node synsetNode = graphDb.createNode();
				synsetOffsetToSynsetId.get(pos).put(synset.getOffset(), synsetNode.getId());

				synsetNode.setProperty("offset", synset.getOffset());
				synsetNode.setProperty("lex_file_num", synset.getLexFileNum());
				synsetNode.setProperty("pos", synset.getPOS().getKey());
				synsetNode.setProperty("is_adj_cluster", POS.ADJECTIVE == synset.getPOS() && synset.isAdjectiveCluster());
				synsetNode.setProperty("gloss", synset.getGloss());

				synsetIndex.add(synsetNode,"offset",synset.getOffset());
				synsetIndex.add(synsetNode,"pos",synset.getPOS());

				List<Word> words = synset.getWords();


				BitSet allWordFrames = null;
				if (synset instanceof VerbSynset) {
					Node synsetVerbFrameNode = graphDb.createNode();
					synsetVerbFrameNode.setProperty("synset_id", synsetNode.getId());//TODO convert this into a relationship
					allWordFrames = synset.getVerbFrameFlags();
					synsetVerbFrameNode.setProperty("word_index", 0);//applicable to all words
					for (int i = allWordFrames.nextSetBit(0); i >= 0; i = allWordFrames.nextSetBit(i + 1)) {
						synsetVerbFrameNode.setProperty("frame_number", i);
					}
				}

				Map<Word,Node> wordMap = new HashMap<>();
				for (Word word : words) {
					Node synsetWordNode = graphDb.createNode();
					//synsetWordNode.setProperty("synset_id", synsetNode.getId());//TODO convert this into a relationship

					synsetWordNode.setProperty("synsetOffset",synset.getOffset());
					synsetWordNode.setProperty("word", word.getLemma());
					synsetWordNode.setProperty("word_index", word.getIndex());
					synsetWordNode.setProperty("usage_cnt", word.getUseCount());
					synsetWordNode.setProperty("lex_id", word.getLexId());

					if (word instanceof Verb) {   //FIXME connect this back
						Node synsetVerbFrameNode = graphDb.createNode();
						synsetVerbFrameNode.setProperty("word_index", word.getIndex());
						BitSet bits = ((Verb) word).getVerbFrameFlags();
						for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
							if (null != allWordFrames && !allWordFrames.get(i)) {
								synsetVerbFrameNode.setProperty("frame_number", i);
							}
						}
					}

					synsetNode.createRelationshipTo(synsetWordNode, new org.neo4j.graphdb.RelationshipType() {
						@Override
						public String name() {
							return RelationshipType.Words.toString();
						}
					});

					wordMap.put(word,synsetWordNode);
				}
				tx.success();
			}
		} catch (Exception e){
			tx.failure();
			LOGGER.error(e.getLocalizedMessage(),e);
		} finally {
			tx.close();
		}
		LOGGER.info("Stored Synsets: " + count);
	}

	protected void buildRelationshipBetweenSynsets(Iterator<Synset> iter, Map<String,String> pointerTypeSymbolMap, POS pos){

		Transaction tx = graphDb.beginTx();
		LOGGER.info("Storing relationships between Synsets for pos {}", pos);

		try {
			while (iter.hasNext()) {
				Synset synset = iter.next();
				List<Pointer> pointers = synset.getPointers();

				Node synsetNode = graphDb.getNodeById(synsetOffsetToSynsetId.get(pos).get(synset.getOffset()));
				for (Pointer pointer : pointers) {
					String pointerTypeKey = pointer.getType().getKey().trim();


					Node targetSynsetNode = graphDb.getNodeById(synsetOffsetToSynsetId.get(pointer.getTargetPOS()).get(pointer.getTargetOffset()));
					Relationship relationship = synsetNode.createRelationshipTo(targetSynsetNode, new org.neo4j.graphdb.RelationshipType() {
						@Override
						public String name() {
							return RelationshipType.Synset.toString();
						}
					});

					relationship.setProperty("pointer_type",pointerTypeSymbolMap.get(pointerTypeKey));
					relationship.setProperty("target_pos",pointer.getTargetPOS().getKey());
					relationship.setProperty("source_index",pointer.getSourceIndex());
					relationship.setProperty("target_index",pointer.getTargetIndex());

				}

				//add readable synsetNames to the synset node to make querying easier
				String[] synsetNames = null;
				if(!CollectionUtils.isEmpty(synset.getWords())) {
					synsetNames = new String[synset.getWords().size()];
					int arrayIndex = 0;
					for (Word word:synset.getWords()){
						String lemma = word.getLemma();
						if(StringUtils.isNotBlank(lemma)) {
							List<Synset> dictionarySynsets = dictionary.getIndexWord(synset.getPOS(), lemma).getSenses();
							int indexOfSynset = 0;
							for(Synset dictionarySynset: dictionarySynsets){
								++indexOfSynset;
								if(dictionarySynset.getOffset() == synset.getOffset()){
									String synsetName = new StringBuilder(lemma).append("#").append(synset.getPOS().getKey()).append("#").append(indexOfSynset).toString();
									synsetNames[arrayIndex++] = synsetName;
									break;
								}

							}
						}
					}
				}

				if (synsetNames != null){
					synsetIndex.add(synsetNode,"synsetNames",synsetNames);
					synsetNode.setProperty("synsetNames", synsetNames);
				}
				//Done with adding readable synsetNames
			}
			tx.success();
		} catch (Exception e) {
			tx.failure();
			LOGGER.error(e.getLocalizedMessage(),e);
		} finally {
			tx.close();
		}

		LOGGER.info("Stored relationships between Synsets");

	}

	protected void buildRelationshipBetweenIndexWordAndSynsets(POS pos) {

		LOGGER.info("Storing relationships between Index words and Synsets");
		Transaction tx = graphDb.beginTx();

		try {
			for (Map.Entry<Long, long[]> entry : indexWordIdToSynsetOffset.get(pos).entrySet()) {

				Node indexWordNode = graphDb.getNodeById(entry.getKey());

				long offsets[] = entry.getValue();
				int index = 0;
				for (long offset : offsets) {

					int nextIndex = ++index;
					Node synsetNode = graphDb.getNodeById(synsetOffsetToSynsetId.get(pos).get(offset));
					synsetNode.createRelationshipTo(indexWordNode, new org.neo4j.graphdb.RelationshipType() {
						@Override
						public String name() {
							return RelationshipType.IndexWord.toString();
						}
					});

					//The following properties are only set for querying and readability purposes
					//synsetNode.setProperty("lemma",indexWordNode.getProperty("lemma"));//Can do this elsewhere also

					Relationship relationship = indexWordNode.createRelationshipTo(synsetNode, new org.neo4j.graphdb.RelationshipType() {
						@Override
						public String name() {
							return RelationshipType.Synsets.toString();
						}
					});

					//setting these so that we can get bank#n#1 getting bank from index word and others from relationship
					relationship.setProperty("synset_pos",synsetNode.getProperty("pos"));
					relationship.setProperty("synset_index",nextIndex);
				}
			}
			tx.success();
		} catch (Exception e) {
			tx.failure();
			LOGGER.error(e.getLocalizedMessage(),e);
		} finally {
			tx.close();
		}


		LOGGER.info("Stored relationships between Index words and Synsets");
	}

	protected void storeExceptions(Iterator<Exc> itr) {
		LOGGER.info("Storing Exceptions");
		Transaction tx = graphDb.beginTx();
		try {
			while (itr.hasNext()) {
				Exc exc = itr.next();

				for (Object o : exc.getExceptions()) {
					Node exceptionNode = graphDb.createNode();
					exceptionNode.setProperty("pos", exc.getPOS().getKey());
					exceptionNode.setProperty("base", (String) o);
					exceptionNode.setProperty("derivation", exc.getLemma());
				}
			}
			tx.success();
		} catch (Exception e) {
			tx.failure();
			LOGGER.error(e.getLocalizedMessage(),e);
		} finally {
			tx.close();
		}

		LOGGER.info("Stored Exceptions");
	}

	public void deleteFileOrDirectory( File file )
	{
		if ( !file.exists() )
		{
			return;
		}

		if ( file.isDirectory() )
		{
			for ( File child : file.listFiles() )
			{
				deleteFileOrDirectory( child );
			}
		}
		else
		{
			file.delete();
		}
	}

	private static void registerShutdownHook( final GraphDatabaseService graphDb )
	{
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook( new Thread()
		{
			@Override
			public void run()
			{
				graphDb.shutdown();
			}
		} );
	}
}