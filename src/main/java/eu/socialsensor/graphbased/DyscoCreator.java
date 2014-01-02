/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.socialsensor.graphbased;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import eu.socialsensor.documentpivot.vocabulary.Vocabulary;
import eu.socialsensor.documentpivot.vocabulary.VocabularyComparator;
import eu.socialsensor.entitiesextractor.EntityDetection;
import eu.socialsensor.framework.client.dao.ItemDAO;
import eu.socialsensor.framework.client.dao.impl.ItemDAOImpl;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.dysco.Entity;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author gpetkos
 * 
 * This class implements topic detection via graph clustering on a graph that summarizes cooccurrence relationships.
 * Different types of cooccurrence patterns may be used to construct the graph. For more details please see the comments
 * in the configuration file (graph_parameters.properties), which can be found under resources.
 */
public class DyscoCreator {
    static BufferedReader tweeterReader = null;
    static Vocabulary vocabulary_reference;
    static Vocabulary vocabulary_corpus;
    static VocabularyComparator vocabulary_comparator;
    static String filename_start;
    static String dir;
    static int nTweets;

    
    private static SimpleDateFormat twitterDateFormatter  
            = new SimpleDateFormat ("EEE MMM dd hh:mm:ss zzzzz yyyy",Locale.ENGLISH); 
                                    //"EEE MMM dd HH:mm:ss ZZZZZ yyyy"
                                    //Mon Mar 05 05:38:28 +0000 2012
//    private static String delimiters="[ \t\n\r\f,.:;?!'\"()]";
    private static String delimiters="\\s";
    private static String punctuation="[,.:;?!'\"()“”]";
    
    static final String urlRegex = "\\b(((ht|f)tp(s?)\\:\\/\\/|~\\/|\\/)|www.)" + 
        "(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov" + 
        "|mil|biz|info|mobi|name|aero|jobs|museum" + 
        "|travel|[a-z]{2}))(:[\\d]{1,5})?" + 
        "(((\\/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|\\/)+|\\?|#)?" + 
        "((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
        "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)" + 
        "(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" + 
        "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*" + 
        "(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b";
    static Pattern patternUrl= Pattern.compile(urlRegex);
    
    
    public DyscoCreator() {
        eu.socialsensor.graphbased.Constants.configuration=new eu.socialsensor.graphbased.Configuration();
    }

    
    /**
     * @param args the command line arguments
     */
    public List<Dysco> createDyscos(List<Item> items){
        List<Dysco> dyscos=new ArrayList<Dysco>();
       vocabulary_corpus=Vocabulary.getCorpusVocabulary(items.iterator(),true);
       vocabulary_corpus.writeToFileOrdered(vocabulary_corpus.directory+vocabulary_corpus.filename_start+".vocabulary", true);
       vocabulary_corpus=new Vocabulary();
       vocabulary_corpus.directory=dir;
       vocabulary_corpus.filename_start=filename_start;
       vocabulary_corpus.readFromFile(dir+filename_start+".vocabulary", true);
       vocabulary_corpus.filterByNoOfOccurrences(5);

       vocabulary_reference=new Vocabulary();
       vocabulary_reference.load(true);
       System.out.println("Read reference vocabulary");

       
       vocabulary_comparator=new VocabularyComparator(vocabulary_corpus,vocabulary_reference);
       vocabulary_comparator.outputOrdered(vocabulary_comparator.vocabulary_new_corpus.directory+vocabulary_comparator.vocabulary_new_corpus.filename_start+".vocabulary_ratios");

       System.out.println("Getting topics");
       dyscos=vocabulary_comparator.getDyscosGraphBased(null);

       return dyscos;
    }

    public Set<String> getURLs(String text){
        Set<String> tmp_urls=new HashSet<String>();
        String tmp_text=text;
        if(tmp_text!=null){
            Matcher matcher = patternUrl.matcher(tmp_text);
            while (matcher.find()) {
                tmp_urls.add(matcher.group());
            }
        }
        return tmp_urls;
    }
    
    public static boolean isUserMention(String token){
        if(token.startsWith("@")) 
            return true;
        else
            return false;
    }
    
    public Set<String> getTerms(Item item){
        String tmp_item_text=item.getTitle();
        Set<String> tmp_urls=getURLs(tmp_item_text);
        Iterator it = tmp_urls.iterator();
        while (it.hasNext()) {
            String next_url=(String) it.next();
            tmp_item_text=tmp_item_text.replaceAll(next_url, "");
        }

        Set<String> tmp_set=new HashSet<String>();
        Scanner tokenize;
        String tmp_word;
        if(tmp_item_text!=null){
            tokenize = new Scanner(tmp_item_text);
            //Punctuation and white space is used for 
            //splitting the text in tokens.
            tokenize.useDelimiter(delimiters);
            while (tokenize.hasNext()) {
                tmp_word=tokenize.next().toLowerCase();
                tmp_word.replaceAll(delimiters, "");
                //The following lines try to remove punctuation.
                //Maybe it should be skipped or revised
                String[] tmp_str=tmp_word.split(punctuation);
                while((tmp_word.length()>0)&&((tmp_word.charAt(0)+"").matches(punctuation)))
                    tmp_word=tmp_word.substring(1);
                while((tmp_word.length()>0)&&((tmp_word.charAt(tmp_word.length()-1)+"").matches(punctuation)))
                    tmp_word=tmp_word.substring(0,tmp_word.length()-1);
                if(!isUserMention(tmp_word)){
    //                if((!StopWords.IsStopWord(tmp_word))&&(tmp_word.length()>=Constants.MIN_WORD_LENGTH))
                    if((tmp_word.length()>=3))
                        tmp_set.add(tmp_word);
                }
//                else if(Constants.TWEET_REMOVE_USER_MENTIONS==false)
//                    tmp_set.add(tmp_word);
            }
        }
        return tmp_set;
    }

    
    
    public static Vocabulary getCorpusVocabulary(boolean keepTweetIDs,List<Item> items){
       Vocabulary vocabulary=new Vocabulary();
       vocabulary.directory=dir;
       vocabulary.filename_start=filename_start;
       Iterator it;
       for(Item tmp_item:items){
           
            List<String> tmp_set=tmp_item.getKeywords();
            String tmp_term;
            it = tmp_set.iterator();
            while (it.hasNext()) {
                tmp_term=(String) it.next();
                if(keepTweetIDs)
                    vocabulary.increaseTermFrequency(tmp_term,tmp_item.getId());
                else
                    vocabulary.increaseTermFrequency(tmp_term);
            }
        }
        System.out.println("No of tweets retrieved: "+nTweets);
        return vocabulary;
    }

    public static void main(String[] args){
        ItemDAO itemdao=new ItemDAOImpl("social1.atc.gr");
        System.out.println("Getting items");
        List<Item> items=itemdao.getLatestItems(1000);
        
        System.out.println("Filtering items");
        List<Item> filtered=new ArrayList<Item>();
        for(Item item:items){
            if((item==null)||(item.getTitle()==null))
                continue;
            filtered.add(item);
        }
        items=filtered;
        System.out.println("No of items post filtering: "+items.size());
        System.out.println("Extracting entities");
        EntityDetection ent=new EntityDetection();
        ent.addEntitiesToItems(items);
        System.out.println("Getting dyscos");
        eu.socialsensor.graphbased.DyscoCreator dc=new eu.socialsensor.graphbased.DyscoCreator();
        List<Dysco> dyscos=dc.createDyscos(items);
        int count=0;
        System.out.println("Printing dyscos:");
        for(Dysco dysco:dyscos){
            System.out.println("--------------------");
            List<Item> tmp_items=dysco.getItems();
            count=count+tmp_items.size();
            for(Item item:tmp_items)
                System.out.println(item.getTitle());
        }
        System.out.println("No of retrieved items: "+items.size());
        System.out.println("No of grouped items : "+count);
        System.out.println("No of dyscos : "+dyscos.size());
    }
    
    
    
}
