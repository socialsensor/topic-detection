package eu.socialsensor.lda;

import java.util.LinkedList;
import java.util.List;

import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.dysco.Ngram;

/**
 * Wrapper for major elements of a topic 
 * @author Luca Maria Aiello (Yahoo! Inc.)
 * @version 1.0
 */
public class LDATopic
{
	private String title;
	private List<String> keywords;
	private List<Item> representativeDocuments;
	
	/**
	 * Creates an empty LDATopic instance
	 */
	public LDATopic()
	{
		this.title = "";
		this.keywords = new LinkedList<String>();
		this.representativeDocuments = new LinkedList<Item>();
	}
	
	/**
	 * Creates a new LDATopic instance
	 * @param title the title of the topic
	 * @param keywords the weighted list of keywords that define the topic
	 * @param representativeDocuments a list of documents that most represent the topic
	 */
	public LDATopic(String title, List<String> keywords, List<Item> representativeDocuments)
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
	public List<String> getKeywords()
	{
		return keywords;
	}
	
	/**
	 * Sets the weighted list of keywords that define the topic
	 * @param keywords the list of keywords
	 */
	public void setKeywords(List<String> keywords)
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
		for (String n : this.keywords)
		{
			repr = repr + " " + n;
		}
		repr = repr + "\n";
		repr = repr + "Representative Docs:";
		for (Item i : this.representativeDocuments)
		{
			String txt = i.getText();
			if (txt.length() > 140)
				txt = txt.substring(0, 140);
			repr = repr + i.getText() + " -- ";
		}
		return repr;
	}
}
