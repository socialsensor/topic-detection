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
import eu.socialsensor.framework.common.domain.dysco.Ngram;

/**
 *
 * @author gpetkos
 */
public class DyscoUtils {

    public static TermVector getTermVectorTFIDF(Dysco dysco,Map<String,TermLikelihood> allTerms,long N){
        TermVector tmp_vec=new TermVector();
        int i;
        List<Ngram> keywords=dysco.getNgrams();
        int n_terms=keywords.size();
        for(i=0;i<n_terms;i++){
            String next_term=(String) keywords.get(i).getTerm();
            TermLikelihood feat=allTerms.get(next_term);
            double weight=Math.log(N/feat.term.fTotal);
            tmp_vec.termsAndTFIDFWeights.put(next_term, weight);
        }
        return tmp_vec;
    }
    
    
    
    public static double dyscoSimilarity(Dysco dysco1,Dysco dysco2){
        int i;
        if((dysco1==null)||(dysco2==null)) return 0;
        List<Ngram> keywords1=dysco1.getNgrams();
        if(keywords1==null) return 0;
        Set<String> keywordsSet1=new HashSet<String>();
        for(Ngram ngram:keywords1)
            keywordsSet1.add(ngram.getTerm());
        List<Ngram> keywords2=dysco2.getNgrams();
        if(keywords2==null) return 0;
        Set<String> keywordsSet2=new HashSet<String>();
        for(Ngram ngram:keywords2)
            keywordsSet1.add(ngram.getTerm());
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
            List<Ngram> keywords=tmp_dysco.getNgrams();
            System.out.print(i+": ");
            for(Ngram tmp_ngram:keywords){
                System.out.print(tmp_ngram.getTerm()+" ");
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
                List<Ngram> keywords=tmp_dysco.getNgrams();
                for(Ngram tmp_ngram:keywords){
                        out.append(tmp_ngram.getTerm()+" ");
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
