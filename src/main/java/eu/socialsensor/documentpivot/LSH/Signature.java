package eu.socialsensor.documentpivot.LSH;

import java.util.BitSet;

public class Signature extends BitSet implements Comparable<Signature> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6099984869699506404L;
	private int nBits;
	
	public Signature(int nBits) {
		super(nBits);
		this.nBits = nBits;
	}
	
	public int size() {
		return nBits;
	}
	
	@Override
	public int hashCode() {
		int hash=0;
		for(int i= this.nextSetBit(0); i>=0; i=this.nextSetBit(i+1)){
			hash += Math.pow(2, i);
		}
		return hash;
	}
	
	public double similarity(Signature other) {
		Signature temp = (Signature) this.clone();
		temp.xor(other);
		double hamming_similarity = ((double)temp.cardinality()) / ((double)nBits);
		return Math.cos(hamming_similarity*Math.PI);
	}
	
	public double distance(Signature other) {
		return 1.0 - similarity(other);
	}
	
	public Signature permute(Integer[] indeces) {
		Signature permutation = new Signature(nBits);
		int i = -1;
		while((i = this.nextSetBit(i))>=0) {
			permutation.set(indeces[i]);
		}	
		return permutation;
	}
	
	@Override
	public int compareTo(Signature other) {
		int start = Math.max(this.length(), other.length());
		for(; start>=0; start--) {
			if(this.get(start)==other.get(start)) {
				continue;
			}
			else {
				if(this.get(start) && !other.get(start))
					return 1;
				else
					return -1; 
			}
		}
		return 0;
	}

	public static void main(String[] args) {
		Signature s = new Signature(10);
		System.out.println(s.hashCode());
		s.set(0);
		System.out.println(s.hashCode());
	}
}
