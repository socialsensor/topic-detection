package eu.socialsensor.lda;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.index.Term;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.dysco.Ngram;

/**
 * Mallet LDA 
 * @author Luca Maria Aiello (Yahoo! Inc.)
 * @version 1.0
 */
public class LDA
{	
	/**
	 * Creates an instance of the LDA topic modeler
	 */
	public LDA()
	{}

	/**
	 * Creates a list of Malelt instances from a list of documents
	 * @param texts a list of documents
	 * @return a list of Mallet instances
	 * @throws IOException
	 */
	private InstanceList createInstanceList(List<String> texts) throws IOException
	{
		ArrayList<Pipe> pipes = new ArrayList<Pipe>();
		pipes.add(new CharSequence2TokenSequence());
		pipes.add(new TokenSequenceLowercase());
		pipes.add(new TokenSequenceRemoveStopwords());
		pipes.add(new TokenSequence2FeatureSequence());
		InstanceList instanceList = new InstanceList(new SerialPipes(pipes));
		instanceList.addThruPipe(new ArrayIterator(texts));
		return instanceList;
	}
	
	/**
	 * Creates the LDA model on the specified document corpus
	 * @param texts a list of documents
	 * @param numTopics the number of desired documents
	 * @param numIterations the number of LDA iterationss
	 * @return An LDA topic model
	 * @throws IOException
	 */
	private ParallelTopicModel createLDAModel(List<String> texts, int numTopics, int numIterations) throws IOException
	{
		InstanceList instanceList = createInstanceList(texts);
		ParallelTopicModel model = new ParallelTopicModel(numTopics);
		model.addInstances(instanceList);
		model.setNumIterations(numIterations);
		model.estimate();
		return model;
	}
	
	/**
	 * Computes LDA on the specified document corpus
	 * @param texts a list of documents
	 * @param numTopics the number of desired documents
	 * @param numIterations the number of LDA iterations
	 * @numKeywords the number of desired keywords per topic
	 * @return a list of Topics
	 * @throws Exception
	 */
	public List<LDATopic> run(List<Item> items, int numTopics, int numIterations, int numKeywords) throws Exception
	{
		//retrieves text of the documents
    	ArrayList<String> texts = new ArrayList<String>();
    	ArrayList<Item> itemsArray = new ArrayList<Item>();
    	for (Item item : items)
    	{
    		String text = item.getTitle();
    		texts.add(text);
    		itemsArray.add(item);
    	}
		
		int numDocuments = texts.size();
		ParallelTopicModel model = createLDAModel(texts,numTopics,numIterations);
		
		LinkedList<LDATopic> topicList = new LinkedList<LDATopic>();
		
		//topicId -> (most representative doc idx, score)
		HashMap<Integer, Pair<Integer,Double>> topicToRepresentativeDoc = new HashMap<Integer, Pair<Integer,Double>>();
		
        for (int docId=0; docId<numDocuments; docId++)
        {
        	double[] probs = model.getTopicProbabilities(docId);
        	int maxIndex = -1;
        	double maxProb = -1;
        	for (int i=0; i<probs.length; i++)
        	{
        		if (probs[i] > maxProb)
        		{
        			maxProb = probs[i];
        			maxIndex = i;
        		}
        	}
        	if (topicToRepresentativeDoc.containsKey(maxIndex))
        	{
        		if (topicToRepresentativeDoc.get(maxIndex).v < maxProb)
        		{
        			topicToRepresentativeDoc.put(maxIndex, new Pair<Integer,Double>(docId, maxProb));
        		}
        	}
        	else
        	{
        		topicToRepresentativeDoc.put(maxIndex, new Pair<Integer,Double>(docId, maxProb));
        	}
        }
        
		Object[][] words = model.getTopWords(numKeywords);
        for(int topicId=0; topicId<words.length; topicId++)
        {
        	LDATopic topic = new LDATopic();
        	LinkedList<Ngram> keywords = new LinkedList<Ngram>();
        	double i = 1.0;
        	for(int wordId=0; wordId<words[topicId].length; wordId++)
        	{
        		String keyword = (String)words[topicId][wordId];
        		double score = i;
        		Ngram n = new Ngram(keyword, new Float(score));
        		keywords.add(n);
        		i = i/2;
        	}
        	topic.setKeywords(keywords);
        	Item reprItem = itemsArray.get(topicToRepresentativeDoc.get(topicId).k);
        	topic.setTitle(reprItem.getText());
        	LinkedList<Item> reprDocs = new LinkedList<Item>();
        	reprDocs.add(reprItem);
        	topic.setRepresentativeDocuments(reprDocs);
        	
        	
        	topicList.add(topic);
        }

		return topicList;
	}
	
	class Pair<T,V>
	{
	    public T k;
	    public V v;
	    Pair(T p1, V p2)
	    {
	        this.k = p1;
	        this.v = p2;
	    }
	}
}