package eu.socialsensor.documentpivot.LSH;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.socialsensor.documentpivot.model.Vector;
import eu.socialsensor.documentpivot.model.VectorSpace;


public class HashTable extends HashMap<Signature, List<Object>> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 223843523508594017L;
	private HashFamily hashFamily = null;
 
	public HashTable(int k, int d) {
		
		hashFamily = new HashFamily(k, d);
	}
	
	public void add(Vector u) {
		Signature hash = hashFamily.hr(u.v);
		if(this.containsKey(hash)) {
			 List<Object> bucket = this.get(hash);
			 bucket.add(u);
		}
		else{
			List<Object> bucket = new ArrayList<Object>();
			bucket.add(u);
			this.put(hash, bucket);
		}
	}
	
	public void add(VectorSpace vsm) {
		Signature hash = hashFamily.hr(vsm);
		//System.out.print(hash.hashCode()+"   ");
		//System.out.println(vsm.text());
		if(this.containsKey(hash)) {
			 List<Object> bucket = this.get(hash);
			 bucket.add(vsm);
		}
		else{
			List<Object> bucket = new ArrayList<Object>();
			bucket.add(vsm);
			this.put(hash, bucket);
		}
	}
	
	public List<Object> get(Vector u) {
		Signature hash = hashFamily.hr(u.v);
		List<Object> bucket = this.get(hash);
		if(bucket != null)
			return bucket;
		else
			return new ArrayList<Object>();
	}

	public List<Object> get(VectorSpace vsm) {
		Signature hash = hashFamily.hr(vsm);
		List<Object> bucket = this.get(hash);
		if(bucket != null)
			return bucket;
		else
			return new ArrayList<Object>();
	}

	public void addUniqueVector(VectorSpace vsm) {
		Signature hash = hashFamily.hr(vsm);
		if(this.containsKey(hash)) {
			 List<Object> bucket = this.get(hash);
			 for(Object other : bucket) {
				 double similarity = vsm.cosineSimilarity((VectorSpace) other);
				 if(similarity>0.8) {
					return;
				 }
			 }
			bucket.add(vsm);
		}
		else {
			List<Object> bucket = new ArrayList<Object>();
			bucket.add(vsm);
			this.put(hash, bucket);
		}
		
	}
}
