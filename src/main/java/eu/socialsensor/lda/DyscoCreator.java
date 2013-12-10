package eu.socialsensor.lda;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.services.GenericDyscoCreator;
import java.util.Map;

/**
 * Integration of Mallet LDA with Dyscos 
 * @author Luca Maria Aiello (Yahoo! Inc.)
 * @author gpetkos 
 * @version 1.0
 * 
 * This class implements topic detection via LDA.
 * It is a wrapper of the mallet implementation for the purposes of socialsensor, i.e. it generates DySCOs.
 * For more details on the parameters of the implementation please see the comments
 * in the configuration file (lda_parameters.properties), which can be found under resources.

 */
public class DyscoCreator implements GenericDyscoCreator
{
	private int numIterations = 300;
	private int numTopics = 10;
	private int numKeywords = 10;
	
	/**
	 * Creates a DyscoCreator with default parameters
	 */
	public DyscoCreator() {}
	
	/**
	 * Creates a DyscoCreator
	 * @param numTopics
	 * @param numIterations
	 * @param numKeywords
	 */
	public DyscoCreator(int numTopics, int numIterations, int numKeywords)
	{
		this.numTopics = numTopics;
		this.numIterations = numIterations;
		this.numKeywords = numKeywords;
                eu.socialsensor.lda.Constants.configuration=new eu.socialsensor.lda.Configuration();
	}
	
	/**
	 * Creates a list of Dyscos from the provided corpus of items
	 * @param items a list of social media items
	 */
    public List<Dysco> createDyscos(List<Item> items)
    {
    	//runs LDA
    	LinkedList<Dysco> dyscoList = new LinkedList<Dysco>();
    	try
    	{
    		LDA lda = new LDA();
    		List<LDATopic> topics = lda.run(items,this.numTopics,this.numIterations,this.numKeywords);
    		
    		for (LDATopic t : topics)
    		{
	    		String title = t.getTitle();
	    		Map<String,Double> keywords = t.getKeywords();
	    		List<Item> representativeDocuments = t.getRepresentativeDocuments();
	    		
	    		Dysco dysco = new Dysco();
	    		dysco.setId(UUID.randomUUID().toString());
	    		dysco.setTitle(title);
	    		dysco.setKeywords(keywords);
	    		dysco.setItems(representativeDocuments);
	    		
	    		dyscoList.add(dysco);
    		}
    	}
    	catch(Exception e)
    	{
    		return null;
    	}
    	
    	return dyscoList;
    }
}