/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.socialsensor.documentpivot.termvector;

import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author gpetkos
 */
public class TermVector {
    public Map<String,Double> termsAndTFIDFWeights;

    public TermVector() {
        termsAndTFIDFWeights=new HashMap<String,Double>();
    }

    public double Length(){
        double ll=0;
        for(Double term:termsAndTFIDFWeights.values()){
            ll=ll+term*term;
        }
        return Math.sqrt(ll);
    }
    
    public double CosineSimilarity(TermVector tv){
        double d=0;
        Set<String> intersection=new HashSet(termsAndTFIDFWeights.keySet());
        intersection.retainAll(tv.termsAndTFIDFWeights.keySet());
        Iterator it=intersection.iterator();
        while(it.hasNext()){
            String next_term=(String) it.next();
            d=d+tv.termsAndTFIDFWeights.get(next_term)*termsAndTFIDFWeights.get(next_term);
        }
        d=d/(Length()*tv.Length());
        return d;
    }
    
    public void print(){
        int i;
        for(Entry<String,Double> tmp_entry:termsAndTFIDFWeights.entrySet()){
            System.out.println(tmp_entry.getKey()+" : "+tmp_entry.getValue());
        }
    }
}