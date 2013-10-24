/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.socialsensor.documentpivot.termfeature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import eu.socialsensor.documentpivot.Constants;
import eu.socialsensor.documentpivot.Utilities;

/**
 *
 * @author gpetkos
 */
public class TermFeature {
    
    public String name;
    public int fTotal;
    public Map<String,Integer> docs;

    public TermFeature(TermFeature tf){
        name=tf.name;
        fTotal=tf.fTotal;
        docs=new HashMap<String,Integer>(tf.docs);
    }
    
    public TermFeature(String name) {
        this.name = name;
        docs=new HashMap<String,Integer>();
    }

    public double similarityCosine(TermFeature tf){
        Set<String> intersection = new HashSet<String>(docs.keySet());
        intersection.retainAll(tf.docs.keySet());
        double no_of_cooccurrences=(double) intersection.size();
        double len_prod=Math.sqrt((double)docs.size()*tf.docs.size());
        return ((double) intersection.size())/len_prod;
    }
    
    public double similarityJaccard(TermFeature tf){
        Set<String> intersection = new HashSet<String>(docs.keySet());
        intersection.retainAll(tf.docs.keySet());
        Set<String> union = new HashSet<String>(docs.keySet());
        union.addAll(tf.docs.keySet());
        return ((double) intersection.size())/((double) union.size());
    }

    public double similarityNoOfCooccurrences(TermFeature tf){
        Set<String> intersection = new HashSet<String>(docs.keySet());
        intersection.retainAll(tf.docs.keySet());
        return (double) intersection.size();
    }

    public double similarityRegularizedMax(TermFeature tf){
        Set<String> intersection = new HashSet<String>(docs.keySet());
        intersection.retainAll(tf.docs.keySet());
        double no_of_cooccurrences=(double) intersection.size();
        int max_size=docs.size();
        if(tf.docs.size()>max_size) max_size=tf.docs.size();
        return no_of_cooccurrences/max_size;
    }
    
    public double similarityRegularizedMin(TermFeature tf){
        Set<String> intersection = new HashSet<String>(docs.keySet());
        intersection.retainAll(tf.docs.keySet());
        double no_of_cooccurrences=(double) intersection.size();
        int min_size=docs.size();
        if(tf.docs.size()<min_size) min_size=tf.docs.size();
        return no_of_cooccurrences/min_size;
    }

    public double similarityRegularizedTimes(TermFeature tf){
        Set<String> intersection = new HashSet<String>(docs.keySet());
        intersection.retainAll(tf.docs.keySet());
        double no_of_cooccurrences=(double) intersection.size();
        int size1=docs.size();
        int size2=tf.docs.size();
        return (no_of_cooccurrences/size1)*(no_of_cooccurrences/size2);
    }

    
    public double similarityRegularizedSum(TermFeature tf){
        Set<String> intersection = new HashSet<String>(docs.keySet());
        intersection.retainAll(tf.docs.keySet());
        double no_of_cooccurrences=(double) intersection.size();
        int size1=docs.size();
        int size2=tf.docs.size();
        return (no_of_cooccurrences/size1)+(no_of_cooccurrences/size2);
    }

    public double similarity(TermFeature tf,eu.socialsensor.topicdetection.MainConstants.TERM_SIMILARITY_METHODS USED_TERM_SIMILARITY_METHOD){
        if(USED_TERM_SIMILARITY_METHOD==eu.socialsensor.topicdetection.MainConstants.TERM_SIMILARITY_METHODS.COSINE)
            return similarityCosine(tf);
        if(USED_TERM_SIMILARITY_METHOD==eu.socialsensor.topicdetection.MainConstants.TERM_SIMILARITY_METHODS.JACCARD)
            return similarityJaccard(tf);
        if(USED_TERM_SIMILARITY_METHOD==eu.socialsensor.topicdetection.MainConstants.TERM_SIMILARITY_METHODS.NO_OF_COOCCURRENCES)
            return similarityNoOfCooccurrences(tf);
        if(USED_TERM_SIMILARITY_METHOD==eu.socialsensor.topicdetection.MainConstants.TERM_SIMILARITY_METHODS.NO_OF_COOCCURRENCES_REGULARIZED_MAX)
            return similarityRegularizedMax(tf);
        if(USED_TERM_SIMILARITY_METHOD==eu.socialsensor.topicdetection.MainConstants.TERM_SIMILARITY_METHODS.NO_OF_COOCCURRENCES_REGULARIZED_MIN)
            return similarityRegularizedMin(tf);
        if(USED_TERM_SIMILARITY_METHOD==eu.socialsensor.topicdetection.MainConstants.TERM_SIMILARITY_METHODS.NO_OF_COOCCURRENCES_REGULARIZED_TIMES)
            return similarityRegularizedTimes(tf);
        if(USED_TERM_SIMILARITY_METHOD==eu.socialsensor.topicdetection.MainConstants.TERM_SIMILARITY_METHODS.NO_OF_COOCCURRENCES_REGULARIZED_SUM)
            return similarityRegularizedSum(tf);
        return similarityNoOfCooccurrences(tf);
    }
    
    

    public double length(){
        int i;
        long sum=0;
        for(Integer tmp_int:docs.values())
            sum=sum+tmp_int*tmp_int;
        return Math.sqrt((double) sum);
    }
    
}
