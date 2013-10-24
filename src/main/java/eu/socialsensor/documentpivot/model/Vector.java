package eu.socialsensor.documentpivot.model;

public class Vector {
	
	public Long id;
	public double[] v;
	
	Vector(Long id, double[] v) {
		this.id = id;
		this.v = v;
	}
	
	public String toString(){
		return this.id.toString();
	}
	
}
