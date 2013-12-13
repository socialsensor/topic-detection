/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.socialsensor.documentpivot.vocabulary;

import eu.socialsensor.framework.common.domain.Item;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import eu.socialsensor.documentpivot.termfeature.TermFeature;
import eu.socialsensor.documentpivot.preprocessing.TweetPreprocessor;
 
/**
 *
 * @author gpetkos
 */
public class Vocabulary {
    public Map<String,TermFeature> terms;
    double fTotal;
    double minF;
    public String directory;
    public String filename_start;
    
    public Vocabulary() {
        terms=new HashMap<String,TermFeature>();
        fTotal=0;
        minF=1;
    }

    
    public void load(boolean readTweetIndices){
        readFromResource(readTweetIndices);
    }
    
    
    public void increaseTermFrequency(String tmp_term){
        TermFeature tmp_term_feature;
        tmp_term_feature=terms.get(tmp_term);
        if(tmp_term_feature==null){
            tmp_term_feature=new TermFeature(tmp_term);
            terms.put(tmp_term, tmp_term_feature);
        }
        tmp_term_feature.fTotal++;
        fTotal++;
    }

    public void increaseTermFrequency(String tmp_term,String tweetID){
        TermFeature tmp_term_feature;
        tmp_term_feature=terms.get(tmp_term);
        if(tmp_term_feature==null){
            tmp_term_feature=new TermFeature(tmp_term);
            terms.put(tmp_term, tmp_term_feature);
        }
        tmp_term_feature.fTotal++;
//        tmp_term_feature.docs.add(tweetID);
        tmp_term_feature.docs.put(tweetID,1);
        fTotal++;
    }
    
    
    public int size(){
        return terms.size();
    }
    
    public void writeToFile(String filename,boolean saveTweetIndices){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(filename), "UTF8"));
            for (Entry<String,TermFeature> tmp_entry:terms.entrySet()) {
                writer.append(tmp_entry.getKey()+" "+tmp_entry.getValue().fTotal);
                if(saveTweetIndices){
//                    for(String tmp_ind:tmp_entry.getValue().docs)
                    for(String tmp_ind:tmp_entry.getValue().docs.keySet())
                        writer.append(" "+tmp_ind);
                }
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            if (writer != null) {
                e.printStackTrace();
                try {
                    writer.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    public void writeToFileOrdered(String filename,boolean saveTweetIndices){
        List<TermFeature> termsByFreq = new ArrayList<TermFeature>(terms.values());

        Collections.sort(termsByFreq, new Comparator<TermFeature>() {

        public int compare(TermFeature o1, TermFeature o2) {
            return o1.fTotal - o2.fTotal;
        }
        });

        
        
        
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(filename), "UTF8"));
            for (TermFeature p : termsByFreq) {
                writer.append(p.name + " " + p.fTotal);
                if(saveTweetIndices){
//                    for(String tmp_ind:p.docs)
                    for(String tmp_ind:p.docs.keySet())
                        writer.append(" "+tmp_ind);
                }
                writer.newLine();
            }        
            writer.close();
        } catch (IOException e) {
            if (writer != null) {
                e.printStackTrace();
                try {
                    writer.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void readFromResource(boolean readTweetIndices){
        BufferedReader reader = null;
        ClassLoader loader=ClassLoader.getSystemClassLoader ();
        InputStream IS = null;
        IS = loader.getResourceAsStream ("vocabulary_corpus.txt");
        try {
//                reader = new BufferedReader(
  //                              new InputStreamReader(new FileInputStream(filename),"UTF8"));
                reader = new BufferedReader(
                                new InputStreamReader(IS,"UTF8"));
                String line = null;
                String[] parts;
                while ( (line = reader.readLine()) != null){
                    parts=line.split(" ");
                    TermFeature tf_tmp=new TermFeature(parts[0]);
                    tf_tmp.fTotal=Integer.parseInt(parts[1]);
                    terms.put(parts[0], tf_tmp);
                    fTotal=fTotal+tf_tmp.fTotal;
                    if(readTweetIndices){
                        for(int i=2;i<parts.length;i++)
//                            tf_tmp.docs.add(parts[i]);
                            tf_tmp.docs.put(parts[i],1);
                    }
                         
                        
                    
                }
                reader.close();
        } catch (IOException e){
                e.printStackTrace();
                if (reader != null){
                        try {
                                reader.close();
                        } catch (IOException ex){
                                ex.printStackTrace();
                        }
                }
        }
    }

    public void readFromFile(String filename,boolean readTweetIndices){
        BufferedReader reader = null;
        try {
                reader = new BufferedReader(
                                new InputStreamReader(new FileInputStream(filename),"UTF8"));
                String line = null;
                String[] parts;
                while ( (line = reader.readLine()) != null){
                    parts=line.split(" ");
                    TermFeature tf_tmp=new TermFeature(parts[0]);
                    tf_tmp.fTotal=Integer.parseInt(parts[1]);
                    terms.put(parts[0], tf_tmp);
                    fTotal=fTotal+tf_tmp.fTotal;
                    if(readTweetIndices){
                        for(int i=2;i<parts.length;i++)
//                            tf_tmp.docs.add(parts[i]);
                            tf_tmp.docs.put(parts[i],1);
                    }
                         
                        
                    
                }
                reader.close();
        } catch (IOException e){
                e.printStackTrace();
                if (reader != null){
                        try {
                                reader.close();
                        } catch (IOException ex){
                                ex.printStackTrace();
                        }
                }
        }
    }
    
    public double getTermFrequency(String term){
        TermFeature tmp_term_feature;
        tmp_term_feature=terms.get(term);
        double freq;
        if(tmp_term_feature==null)
            freq=minF;
        else
            freq=tmp_term_feature.fTotal;
        return freq;
    }

    
    public double getTermLikelihood(String term){
        TermFeature tmp_term_feature;
        tmp_term_feature=terms.get(term);
        double freq;
        if(tmp_term_feature==null)
            freq=minF;
        else
            freq=tmp_term_feature.fTotal;
        
//        double likelihood=(freq+Constants.LIKELIHOOD_DELTA)/(fTotal+Constants.LIKELIHOOD_DELTA*terms.size());
        double likelihood=(freq)/(fTotal);
        return likelihood;
    }
    
    public void filterByNoOfOccurrences(int minOccurences){
        Set<String> toRemove=new HashSet<String>();
        for(Entry<String,TermFeature> tmp_entry:terms.entrySet()){
            if(tmp_entry.getValue().fTotal<minOccurences)
                toRemove.add(tmp_entry.getKey());
        }
        for(String tmp_string:toRemove)
            terms.remove(tmp_string);
    }
 
    public static Vocabulary getCorpusVocabulary(Iterator<Item> postIterator,boolean keepTweetIDs){
       Vocabulary vocabulary=new Vocabulary();
       Item tmp_post;
//        while((tmp_tweet=getNextTweetFromFile())!=null){
       Iterator<String> it;
        while(postIterator.hasNext()){
            tmp_post=postIterator.next();
            List<String> tmp_tokens=TweetPreprocessor.Tokenize(tmp_post, true, true,true);
//            List<String> tmp_tokens=tmp_post.getTokens();
            String tmp_term;
            it = tmp_tokens.iterator();
            while (it.hasNext()) {
                tmp_term=(String) it.next();
                if(keepTweetIDs)
                    vocabulary.increaseTermFrequency(tmp_term,tmp_post.getId());
                else
                    vocabulary.increaseTermFrequency(tmp_term);
            }
        }
        return vocabulary;
    }

    
    
}
