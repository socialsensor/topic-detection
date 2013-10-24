package eu.socialsensor.documentpivot.model;

import java.util.Comparator;

public class RankedObject implements Comparator<RankedObject> {

	private String id;
	private double similarity;
	
	public RankedObject() {
		super();
	}
	
	public RankedObject(String id, double similarity) {
		super();
		this.id = id;
		this.similarity = similarity;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public double getSimilarity() {
		return similarity;
	}
	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}
	
	@Override
	public int compare(RankedObject obj1, RankedObject obj2) {
		if (obj1.getSimilarity() > obj2.getSimilarity())
			return 1;
		else if (obj1.getSimilarity() < obj2.getSimilarity())
			return -1;
		else
			return 0;
	}

}
