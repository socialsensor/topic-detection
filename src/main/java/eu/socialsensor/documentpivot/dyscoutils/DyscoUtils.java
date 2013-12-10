/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.socialsensor.documentpivot.dyscoutils;

import eu.socialsensor.framework.common.domain.dysco.Dysco;
import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import eu.socialsensor.documentpivot.termlikelihood.TermLikelihood;
import eu.socialsensor.documentpivot.termvector.TermVector;
import java.util.Map.Entry;

/**
 *
 * @author gpetkos
 */
public class DyscoUtils {

    public static TermVector getTermVectorTFIDF(Dysco dysco,Map<String,TermLikelihood> allTerms,long N){
        TermVector tmp_vec=new TermVector();
        int i;
        Map<String,Double> keywords=dysco.getKeywords();
        int n_terms=keywords.size();
        for(Entry<String,Double> tmp_entry:keywords.entrySet()){
//        for(i=0;i<n_terms;i++){
            String next_term=tmp_entry.getKey();
            TermLikelihood feat=allTerms.get(next_term);
            double weight=Math.log(N/feat.term.fTotal);
            tmp_vec.termsAndTFIDFWeights.put(next_term, weight);
        }
        return tmp_vec;
    }
    
    
    
    public static double dyscoSimilarity(Dysco dysco1,Dysco dysco2){
        int i;
        if((dysco1==null)||(dysco2==null)) return 0;
        Map<String,Double> keywords1=dysco1.getKeywords();
        if(keywords1==null) return 0;
        Set<String> keywordsSet1=new HashSet<String>();
        for(Entry<String,Double> tmp_entry:keywords1.entrySet())
            keywordsSet1.add(tmp_entry.getKey());
        Map<String,Double> keywords2=dysco2.getKeywords();
        if(keywords2==null) return 0;
        Set<String> keywordsSet2=new HashSet<String>();
        for(Entry<String,Double> tmp_entry:keywords2.entrySet())
            keywordsSet2.add(tmp_entry.getKey());
        keywordsSet1.retainAll(keywordsSet2);
        double n_common=(double) keywordsSet1.size();

        int size1=keywords1.size();
        int size2=keywords2.size();
        int min_size=size1;
        if(size2<size1) min_size=size2;
        
        
        return n_common/((double) min_size);
    }
    
    public static void writeDyscoCollectionToStdOut(List<Dysco> dyscos,int n_dyscos){
        int i=0;
        for(Dysco tmp_dysco:dyscos){
            i++;
            if(i>n_dyscos) return;
            Map<String,Double> keywords=tmp_dysco.getKeywords();
            System.out.print(i+": ");
            for(Entry<String,Double> tmp_entry:keywords.entrySet()){
                System.out.print(tmp_entry.getKey()+" ");
            }
            System.out.println();
        }
    }

    public static void writeDyscoCollectionToFile(List<Dysco> dyscos,String filename,int n_dyscos){
        BufferedWriter out = null;
        try {
            int i=0;
            out = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(filename), "UTF8"));
            for(Dysco tmp_dysco:dyscos){
                i++;
                if(i>n_dyscos) return;
                Map<String,Double> keywords=tmp_dysco.getKeywords();
                for(Entry<String,Double> tmp_entry:keywords.entrySet()){
                        out.append(tmp_entry.getKey()+" ");
                }
                out.newLine();
            }
            out.close();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DyscoUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DyscoUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
