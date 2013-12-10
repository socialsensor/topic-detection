package eu.socialsensor.lda;

import eu.socialsensor.framework.common.domain.Item;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Wrapper for major elements of a topic 
 * @author Luca Maria Aiello (Yahoo! Inc.)
 * @version 1.0
 */
public class LDATopic
{
	private String title;
	private Map<String,Double> keywords;
	private List<Item> representativeDocuments;
	
	/**
	 * Creates an empty LDATopic instance
	 */
	public LDATopic()
	{
		this.title = "";
		this.keywords = new HashMap<String,Double>();
		this.representativeDocuments = new LinkedList<Item>();
	}
	
	/**
	 * Creates a new LDATopic instance
	 * @param title the title of the topic
	 * @param keywords the weighted list of keywords that define the topic
	 * @param representativeDocuments a list of documents that most represent the topic
	 */
	public LDATopic(String title, Map<String,Double> keywords, List<Item> representativeDocuments)
	{
		this.title = title;
		this.keywords = keywords;
		this.representativeDocuments = representativeDocuments;
	}
	
	/**
	 * Returns the title of the topic
	 * @return the topic title
	 */
	public String getTitle()
	{
		return title;
	}
	
	/**
	 * Sets the title of the topic
	 * @param title the topic title
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	/**
	 * Gets the weighted list of keywords that define the topic
	 * @return the list of keywords
	 */
	public Map<String,Double> getKeywords()
	{
		return keywords;
	}
	
	/**
	 * Sets the weighted list of keywords that define the topic
	 * @param keywords the list of keywords
	 */
	public void setKeywords(Map<String,Double> keywords)
	{
		this.keywords = keywords;
	}
	
	/**
	 * Gets a list of documents that most represent the topic
	 * @return the list of topics
	 */
	public List<Item> getRepresentativeDocuments()
	{
		return representativeDocuments;
	}
	
	/**
	 * Sets the list of documents that most represent the topic
	 * @param representativeDocuments the list of topics
	 */
	public void setRepresentativeDocuments(List<Item> representativeDocuments)
	{
		this.representativeDocuments = representativeDocuments;
	}
	
	public String toString()
	{
		String repr = "Title: "+this.title+"\n";
		repr = repr + "Keywords:";
		for (Entry<String,Double> tmp_entry : this.keywords.entrySet())
		{
			repr = repr + " " + tmp_entry.getKey();
		}
		repr = repr + "\n";
		repr = repr + "Representative Docs:";
		for (Item i : this.representativeDocuments)
		{
			String txt = i.getTitle();
			if (txt.length() > 140)
				txt = txt.substring(0, 140);
			repr = repr + i.getTitle() + " -- ";
		}
		return repr;
	}
}
