package eu.socialsensor.documentpivot.LSH;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aliasi.util.BoundedPriorityQueue;

import eu.socialsensor.documentpivot.model.RankedObject;
import eu.socialsensor.documentpivot.model.Vector;
import eu.socialsensor.documentpivot.model.VectorSpace;

public class HashTables {

	HashTable hashTables[];
	
	public HashTables(int L, int k, int d) {
		hashTables = new HashTable[L];
		for(int i=0;i<L;i++) {
			//System.out.print(".");
			hashTables[i] = new HashTable(k,d);
		}
	}
	
	public void add(Vector u) {
		for(HashTable hashTable : hashTables) {
			hashTable.add(u);
		}
	}
	
	public void add(VectorSpace vsm) {
		for(HashTable hashTable : hashTables) {
			hashTable.add(vsm);
		}
	}
	
	public void addUniqueVector(VectorSpace vsm) {
		for(HashTable hashTable : hashTables) {
			hashTable.addUniqueVector(vsm);
		}
	}
	
	public Vector[] get(Vector u) {
		Set<Object> set = new HashSet<Object>();
		for(HashTable hashTable : hashTables) {
			List<Object> bucket = hashTable.get(u);
			set.addAll(bucket);
		}
		return set.toArray(new Vector[set.size()]);
	}
	
	public VectorSpace[] get(VectorSpace vsm) {
		Set<Object> set = new HashSet<Object>();
		for(HashTable hashTable : hashTables) {
			List<Object> bucket = hashTable.get(vsm);
			if(bucket!=null) {
				set.addAll(bucket);
			}
		}
		return set.toArray(new VectorSpace[set.size()]);
	}
	
	public RankedObject[] getNN(VectorSpace vsm, int N) {
		BoundedPriorityQueue<RankedObject> nn = 
				new BoundedPriorityQueue<RankedObject>(new RankedObject(), N);
		VectorSpace[] condidate_nns = get(vsm);
		for(VectorSpace candidate : condidate_nns){
			double similarity = vsm.cosineSimilarity(candidate);
			nn.offer(new RankedObject(candidate.id(), similarity));
		}
		return nn.toArray(new RankedObject[nn.size()]);
	}
	
	public RankedObject getNearest(VectorSpace vsm) {
		BoundedPriorityQueue<RankedObject> nn = 
				new BoundedPriorityQueue<RankedObject>(new RankedObject(), 1);
		VectorSpace[] condidate_nns = get(vsm);
		for(VectorSpace candidate : condidate_nns){
			double similarity = vsm.cosineSimilarity(candidate);
			nn.offer(new RankedObject(candidate.id(), similarity));
		}
		return nn.peek();
	}
	
	public void clear() {
		for(HashTable hashTable : hashTables) {
			hashTable.clear();
		}
	}
	public static void main(String[] argv) {
		
	}
}
