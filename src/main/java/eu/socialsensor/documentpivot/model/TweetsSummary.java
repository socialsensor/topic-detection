package eu.socialsensor.documentpivot.model;


import twitter4j.Status;

public class TweetsSummary {

	public String text;
	public long id;

	public TweetsSummary(Status status){
		this.id = status.getId();
		this.text = status.getText();
	}
}
