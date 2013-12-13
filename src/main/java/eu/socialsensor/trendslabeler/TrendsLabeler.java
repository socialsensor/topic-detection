/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.socialsensor.trendslabeler;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer.PTBTokenizerFactory;
import edu.stanford.nlp.util.CoreMap;
import eu.socialsensor.documentpivot.preprocessing.StopWords;
import eu.socialsensor.framework.client.dao.ItemDAO;
import eu.socialsensor.framework.client.dao.DyscoDAO;
import eu.socialsensor.framework.client.dao.impl.ItemDAOImpl;
import eu.socialsensor.framework.client.dao.impl.DyscoDAOImpl;

import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.WebPage;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.dysco.Entity;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.String;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public class TrendsLabeler {

    public static final String[] BREAKING_ARRAY = new String[] { "6017542", 
        "5402612","428333","23484039","15108702","18767649","18424289","87416722","384438102",
"87416722","7587032","361501426","612473","14138785","15012486","11014272","14569869","354267800",
"48833593","807095","7587032", "113050195","15110357","7905122",
"16672510","788524","10977192","14138785","138387125","19656220","19536881"};

    static{
        System.setProperty ("sun.net.client.defaultReadTimeout", "7000");
        System.setProperty ("sun.net.client.defaultConnectTimeout", "7000");
    }
    
    /*
    public static final String[] BREAKING_ARRAY = new String[] {"BreakingNews", 
    "BBCBreaking","cnnbrk","WSJbreakingnews","ReutersLive","CBSTopNews","AJELive",
"SkyNewsBreak","ABCNewsLive","SkyNewsBreak","SkyNews","BreakingNewsUK","BBCNews","TelegraphNews",
"CBSNews","ftfinancenews","Channel4News","5_News","24HOfNews","nytimes",
"SkyNews","skynewsbiz","ReutersBiz","guardiantech","mediaguardian",
"guardiannews","fttechnews","telegraphnews","telegraphsport","telegraphbooks","telefinance"  };
     */

    public static final String titleSeparators="[|-]";
    public static final double thresholdMediaName=0.3;
    
    
    public static final Set<String> BREAKING_ACCOUNTS = new HashSet<String>(Arrays.asList(BREAKING_ARRAY));    
    public static double url_threshold_similarity=0.2;
    
    public static void main(String[] args) {
        
        DyscoDAO dyscoDAO = new DyscoDAOImpl(); 
        try{
//        DyscoDAO dyscoDAO = new DyscoDAOImpl("social1.atc.gr","dyscos","items","MediaItems");        try{
            BufferedWriter bw=new BufferedWriter(new FileWriter("D:\\topicTitlesChanges.txt"));
            int n_to_process=1;
//            int n_to_process=dyscoIds.length;
            String title=null;
            for(int i=0;i<n_to_process;i++){

                Dysco dysco=dyscoDAO.findDysco(dyscoIds[i]);
                List<Entity> ents=dysco.getEntities();
                bw.append("");
                bw.append("-------------------------------\n");
                bw.append("Dysco ID :\n\t"+dyscoIds[i]);
                bw.newLine();
                bw.append("Old title is: \n\t"+dysco.getTitle());
                bw.newLine();
                
                System.out.println("----------------");
                System.out.println("Old title is: \n"+dysco.getTitle());

                String new_title_no_urls=findPopularTitle(dysco);
                title=new_title_no_urls;
                String new_title_urls=findPopularTitleCERTHIncludeURLs(dysco);
                dysco.setTitle(new_title_no_urls);
                dyscoDAO.updateDysco(dysco);
                System.out.println("New title (no URLS):\n"+new_title_no_urls);
                System.out.println("New title (with URLS):\n"+new_title_urls);
                System.out.println("");
                bw.append("New title (no URLS):\n\t"+new_title_no_urls);
                bw.newLine();
                bw.append("New title (with URLS):\n\t"+new_title_urls);
                bw.newLine();
                bw.newLine();
                List<Item> items=dysco.getItems();

                System.out.println("----------");
                for(Item tmp_item:items){
                    System.out.println(tmp_item.getTitle());
                }

                bw.append("List of tweets: \n");
                for(Item tmp_item:items){
                    bw.append("\t"+tmp_item.getTitle()+"\n");
                }
                bw.newLine();
                
                
                System.out.println("Entities : ");
                if(ents!=null){
                    for(Entity ent:ents)
                        System.out.println(ent.getName()+" "+ent.getType());
                }
                else{
                    System.out.println("IS NULL");
                }

                bw.append("Entities: \n");
                if(ents!=null){
                    for(Entity ent:ents)
                        bw.append("\t"+ent.getName()+" - "+ent.getType()+"\n");
                }
                else{
                    bw.append("\tNULL");
                }
            }        
            bw.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    
    static Extractor extr = new Extractor();

    private final static Integer MAX_TOKEN_LENGTH = 20;

    public static double bestJaccard(ArrayList<Set<String>> sentences,Set<String> newSentence){
        double best_sim=Double.MIN_VALUE;
        for(Set<String> next_sentence:sentences){
            double sim=Jaccard(next_sentence,newSentence);
            if(sim>best_sim)
                best_sim=sim;
        }
        return best_sim;
    }
    
    public static double Jaccard(Set<String> terms1,Set<String> terms2){
        Set<String> intersection = new HashSet<String>(terms1);
        intersection.retainAll(terms2);
        Set<String> union = new HashSet<String>(terms1);
        union.addAll(terms2);
        if(union.size()>0)
            return ((double) intersection.size())/((double) union.size());
        else
            return 0.0;
    }
    
    
    public static String findPopularTitleCERTHIncludeURLs(Dysco dysco){
        List<Item> items=dysco.getItems();
        Set<String> entities=new HashSet<String>();
        List<Entity> ents=dysco.getEntities();
        for(Entity ent:ents)
            entities.add(ent.getName());
        List<String> textItems=new ArrayList<String>();
        Map<String,List<String>> perURLitems=new HashMap<String,List<String>>();
        ArrayList<Set<String>> cleanedTokens=new ArrayList<Set<String>>();
        for(Item item_tmp:items){
            List<String> sentences=getSentences1(item_tmp.getTitle(),entities);
            textItems.addAll(sentences);
            for(String sentence:sentences){
                cleanedTokens.add(tokenizeClean(sentence));
            }
            URL[] tmp_urls=item_tmp.getLinks();
            if(tmp_urls!=null){
                for(int i=0;i<tmp_urls.length;i++){
                    String tmp_url_str=tmp_urls[i].toString();
                    if(!perURLitems.containsKey(tmp_url_str)){
                        List<String> key_elements=grabKeyElementsFromURL(tmp_url_str);
                        perURLitems.put(tmp_url_str, key_elements);
                    }
                }
            }
            List<WebPage> webpages=item_tmp.getWebPages();
            if(webpages!=null){
                for(WebPage tmp_webpage:webpages){
                    String tmp_url_str=tmp_webpage.getUrl();
                    if(!perURLitems.containsKey(tmp_url_str)){
                        List<String> key_elements=grabKeyElementsFromURL(tmp_url_str);
                        List<String> new_cands=new ArrayList<String>();
                        for(String tmp_cand:key_elements){
                            List<String> tmp_sentences=getSentences1(tmp_cand,entities);
                            new_cands.addAll(tmp_sentences);
                        }
                        perURLitems.put(tmp_url_str, new_cands);
                    }
                }
            }

        }
        
        for(List<String> tmp_key_elements:perURLitems.values()){
            for(String nextSentence:tmp_key_elements){
                Set<String> next_cand=tokenizeClean(nextSentence);
                double best_similarity=bestJaccard(cleanedTokens,next_cand);
                if(best_similarity>url_threshold_similarity){
                    String cleanedSentence=extractor.cleanText(nextSentence);
                    textItems.add(cleanedSentence);
                }
            }
        }
        
        String title=findPopularTitleNew(textItems);
        if(((title==null)||(title.trim().length()==0))&&(textItems.size()>0)){
            System.out.println("NULL CASE 1 : "+title+"----");
            title=extractor.cleanText(textItems.get(0));       
        }
        if(((title==null)||(title.trim().length()==0))&&(textItems.size()>0)){
            System.out.println("NULL CASE 2");
            title=textItems.get(0);
        }
        if(((title==null)||(title.trim().length()==0))&&(items.size()>0)){
            System.out.println("NULL CASE 3");
            title=items.get(0).getTitle();
        }

        
        return title;
    }
    
    public static Set<String> tokenizeClean(String sentence){
        HashSet<String> tokens=new HashSet<String>();
        String cleanedSentence=extractor.cleanText(sentence);       
        String[] parts=cleanedSentence.split("\\s");
        for(int i=0;i<parts.length;i++)
            if(!StopWords.isStopWord(parts[i]))
                tokens.add(parts[i].toLowerCase());
        return tokens;
    }
    
    
    //THIS IS THE NEWEST VERSION 
    public static String findPopularTitle(Dysco dysco){
        List<Item> items=dysco.getItems();
        Logger.getRootLogger().info("Title extractor : Examining case 1 (getting title from most popular url)");
        //Case 1, there are urls that point to a webpage that has a title
        //        pick the title of the most popular page.
        Map<String,Integer> url_counts=new HashMap<String,Integer>();
        Logger.getRootLogger().info("Title extractor  (case 1) : finding most popular URL");
        for(Item item_tmp:items){
            URL[] tmp_urls=item_tmp.getLinks();
            Logger.getRootLogger().info("Title extractor  (case 1) : got list of urls will now expand");
            if(tmp_urls!=null){
                for(int i=0;i<tmp_urls.length;i++){
                    String resolved=null;
                    Logger.getRootLogger().info("Title extractor  (case 1) : will now expand" + tmp_urls[i].toString());
                    resolved = URLDeshortener.expandFast(tmp_urls[i].toString());
                    Logger.getRootLogger().info("Title extractor  (case 1) : expanded"+tmp_urls[i].toString());
                    if(resolved!=null){
                        Integer count=url_counts.get(resolved);
                        if(count == null)
                            url_counts.put(resolved,1);
                        else
                            url_counts.put(resolved,count+1);
                    }
                }
            }
            Logger.getRootLogger().info("Title extractor  (case 1) : expanded, will now count");
        }
        Logger.getRootLogger().info("Title extractor  (case 1) : found most popular URL");
        if (url_counts.size()>0){
            int maximum=Integer.MIN_VALUE;
            String most_popular_url=null;
            for(Entry<String,Integer> tmp_entry:url_counts.entrySet()){
                if(tmp_entry.getValue()>maximum){
                    maximum=tmp_entry.getValue();
                    most_popular_url=tmp_entry.getKey();
                }
            }
            if(most_popular_url!=null){
                Logger.getRootLogger().info("Title extractor  (case 1) : fetching title from most popular url");
                String candidate_title=grabTitleFromURL(most_popular_url);
                candidate_title=StringEscapeUtils.unescapeHtml(candidate_title);
                if ((candidate_title!=null)&&(!candidate_title.equals(""))){
                        String[] parts1=candidate_title.split("\\|");
                        int max_length=parts1[0].length();
                        candidate_title=parts1[0];
                        for(int p=1;p<parts1.length;p++)
                            if(parts1[p].length()>max_length){
                                max_length=parts1[p].length();
                                candidate_title=parts1[p];
                            }
                                
                        Logger.getRootLogger().info("Title extractor  (case 1) : cleaning candidate title");
                        candidate_title=cleanTitleFromCommonMediaNames(candidate_title);
                        Logger.getRootLogger().info("Title extractor  (case 1) : getting site name");
                        String mediaName=getSiteNameFromURL(most_popular_url).toLowerCase();
                        String[] titleParts=candidate_title.split(titleSeparators);
                        candidate_title="";
                        AbstractStringMetric metric = new Levenshtein();
                        for(int i=0;i<titleParts.length;i++){
                            String next_part=titleParts[i].trim();
                            float mediaNameSimilarity=metric.getSimilarity(mediaName, next_part.replace(" ", "").toLowerCase());
                            if(mediaNameSimilarity<thresholdMediaName){
                                candidate_title=candidate_title+next_part+" ";
                            }
                            
                        }
                        candidate_title=candidate_title.trim();
                        if ((candidate_title!=null)&&(!candidate_title.equals(""))&&(!candidate_title.toLowerCase().equals("home")))
                            return candidate_title;
                }
            }
        }
        
        
        
        Logger.getRootLogger().info("Title extractor : Examining case 2 (message posted by listed user)");
        Set<String> entities=new HashSet<String>();
        List<Entity> ents=dysco.getEntities();
        for(Entity ent:ents)
            entities.add(ent.getName());
        for(Item item_tmp:items){
            if(BREAKING_ACCOUNTS.contains(item_tmp.getAuthorScreenName())){
                String candidate_title=item_tmp.getTitle();
                List<String> parts=getSentences1(candidate_title,entities);
                candidate_title="";
                for(String part:parts) candidate_title=candidate_title+" "+part;
                candidate_title=candidate_title.trim();
                candidate_title=StringEscapeUtils.unescapeHtml(candidate_title);
                if(candidate_title.endsWith(":"))
                    candidate_title=candidate_title.substring(0,candidate_title.length()-2)+".";

                
                if ((candidate_title!=null)&&(!candidate_title.equals("")))
                    return candidate_title;
            }
        }
        
        //Case 3, default certh procedure: finding most popular sentence in all tweets
        Logger.getRootLogger().info("Title extractor : Examining case 3 (most popular sentence)");
        String candidate_title=findPopularTitleCERTH(dysco);
        candidate_title=StringEscapeUtils.unescapeHtml(candidate_title);
        if(candidate_title.endsWith(":"))
            candidate_title=candidate_title.substring(0,candidate_title.length()-1)+".";
        return candidate_title;
    
    }

    
    public static String findPopularTitleCERTH(Dysco dysco){
        List<Item> items=dysco.getItems();
        List<String> textItems=new ArrayList<String>();
        Set<String> entities=new HashSet<String>();
        List<Entity> ents=dysco.getEntities();
        for(Entity ent:ents)
            entities.add(ent.getName());
        Logger.getRootLogger().info("Title extractor (case 3): Getting candidate sentences");
        for(Item item_tmp:items){
            List<String> sentences=getSentences1(item_tmp.getTitle(),entities);
            textItems.addAll(sentences);
        }
            
        Logger.getRootLogger().info("Title extractor (case 3): Finding most popular sentence");
        String title=findPopularTitleNew(textItems);
        if(((title==null)||(title.trim().length()==0))&&(textItems.size()>0))
            title=extractor.cleanText(textItems.get(0));       
        if(((title==null)||(title.trim().length()==0))&&(textItems.size()>0))
            title=textItems.get(0);
        if(((title==null)||(title.trim().length()==0))&&(items.size()>0))
            title=items.get(0).getTitle();
        return title;
        
    }


    private static String findPopularTitleNew(List<String> textItems) {
        List<String> titles = textItems;
        
        List<String> filteredTitles = getFilteredTitles(titles);
        if(filteredTitles.size() == 0) {
            return null;
        }

//        List<String> combinations = new ArrayList<String>();
        HashMap<String,Integer> combinationsCounts = new HashMap<String,Integer>();
        List<String> filteredTitlesFinal = new ArrayList<String>();
        
        for(String title : filteredTitles) {
            String[] titleTokens = title.trim().split("[\\s]+");

            List<String> tokens = Arrays.asList(titleTokens);
            String str_concat="";
            for(String tmp_str:tokens)
                str_concat=str_concat+tmp_str+" ";
            str_concat=str_concat.trim();
            filteredTitlesFinal.add(str_concat);
            addCombinationsCounts(str_concat,combinationsCounts);
        }

        if(combinationsCounts.size() == 0){
            return null;
        }

        Map<String, RankedTitle> titlesFrequencies = new HashMap<String, RankedTitle>();
        for(Entry<String,Integer> combination : combinationsCounts.entrySet()){
//        for(String combination : combinations){
            RankedTitle rankTitle = titlesFrequencies.get(combination.getKey());
            if (rankTitle == null){
                titlesFrequencies.put(combination.getKey(), new RankedTitle(combination.getKey(), combination.getValue()));
            }else{
                rankTitle.setFrequency(rankTitle.getFrequency()+combination.getValue());
            }
        }

        List<RankedTitle> listOfRankedTitles = new ArrayList<RankedTitle>();
        for (Entry<String, RankedTitle> entry2 : titlesFrequencies.entrySet()){
            listOfRankedTitles.add(entry2.getValue());
            
        }

        //candidates and final selection
        List<String> finalSelectedTitles = getFinalTitles(listOfRankedTitles, filteredTitles.size());
        if(finalSelectedTitles.size()>0){
            String best_phrase=finalSelectedTitles.get(0);
//            System.out.println("Best phrase : "+best_phrase);
            Map<String,Integer> counts=new HashMap<String,Integer>();
            for(String tmp_str:textItems){
                if((tmp_str.contains(best_phrase.trim()))){
                    Integer cc=counts.get(tmp_str);
                    if(cc==null)
                        counts.put(tmp_str,1);
                    else
                        counts.put(tmp_str,cc+1);
                }
            }
            String best_sentence="";
            int best_count=-1;
            for(Entry<String,Integer> tmp_entry:counts.entrySet())
                if(tmp_entry.getValue()>best_count){
                    best_count=tmp_entry.getValue();
                    best_sentence=tmp_entry.getKey();
                }
            best_sentence=extractor.cleanText(best_sentence);
            return best_sentence;
        }
        else {
            return null;
        }

    }
    
    
    public static void addCountsToCounts(HashMap<String,Integer> addIt,HashMap<String,Integer> addTarget){
        for(Entry<String,Integer> entry_tmp:addIt.entrySet()){
            Integer count_tmp=addTarget.get(entry_tmp.getKey());
            if(count_tmp==null)
                addTarget.put(entry_tmp.getKey(), entry_tmp.getValue());
            else
                addTarget.put(entry_tmp.getKey(), count_tmp+entry_tmp.getValue());
        }
    }
    
    private static Extractor extractor = new Extractor();

    public static List<String> getClusterTitles(List<String> textItems) {
            List<String> titles = new ArrayList<String>();
            for(String tmp_text : textItems) {
                String title = extractor.cleanText(tmp_text);
                titles.add(title);
            }
            return titles;
    }

    public static List<String> getFilteredTitles(List<String> titles) {
        List<String> filteredTitles = new ArrayList<String>();

        for(String title : titles){
            int numberOfLetters = 0;
            if(title == null){
                continue;
            }
            for(int i = 0; i < title.length(); i++){
                if(Character.isLetter(title.charAt(i))){
                    numberOfLetters++;
                }
            }
            //more than 4 letters
            if(numberOfLetters <= 4){
                continue;
            }
            if(hasHighDigitToletterRatio(title, 0.5)){
                continue;
            }
            filteredTitles.add(title);
        }

        return filteredTitles;
    }

    public static List<String> getFinalTitles(List<RankedTitle> listOfRankedTitles, int numberOfPhotos) {

        for(RankedTitle title : listOfRankedTitles){
            int titleLength = title.getTitle().split(" ").length;
            title.setFrequency(title.getFrequency() * titleLength);
        }

        rerankTitlesByNumberOfPhotos(listOfRankedTitles, numberOfPhotos);

        rerankRedundantSingleTokenTitles(listOfRankedTitles);
        rerankMaximumTokenLengthWithinATitle(listOfRankedTitles);
        Collections.sort(listOfRankedTitles, Collections.reverseOrder());

        List<RankedTitle> highRankedTokens = filterLowRankTokens(listOfRankedTitles);
        if(highRankedTokens.size() > 0){
            return filterByLevensteinSimilarity(highRankedTokens);
        }else{
            return new ArrayList<String>();
        }

    }

    public static void rerankMaximumTokenLengthWithinATitle(List<RankedTitle> titles) {

        for(RankedTitle title : titles){
            int maxLength = 0;
            String[] parts = title.getTitle().split(" ");
            for(String token : parts){
                if(token.length() > maxLength){
                    maxLength = token.length();
                }
            }
            if(maxLength < 4){
                title.setFrequency(-2);
            }
        }

    }

    public static List<String> filterByLevensteinSimilarity(List<RankedTitle> tokenTitles) {

        if(tokenTitles.size() == 0){
            throw new IllegalArgumentException("Cannot process an empty list");
        }


        List<String> finalTitles = new ArrayList<String>();
        finalTitles.add(tokenTitles.get(0).getTitle());
        if(tokenTitles.size() < 2){
            return finalTitles;
        }

        AbstractStringMetric metric = new Levenshtein();
        for(int i = 1; i < tokenTitles.size(); i++){
            boolean reject = false;
            for(String title : finalTitles){
                float result = metric.getSimilarity(title, tokenTitles.get(i).getTitle());
                if (result >= 0.7){
                    reject = true;
                    break;
                }
            }
            if(!reject){
                finalTitles.add(tokenTitles.get(i).getTitle());
            }
        }

        return finalTitles;
    }

    public static List<RankedTitle> filterLowRankTokens(List<RankedTitle> tokenTitles){
        List<RankedTitle> filteredTitles = new ArrayList<RankedTitle>();
        for(RankedTitle title : tokenTitles){
            if(title.getFrequency() < 0){
                continue;
            }
            filteredTitles.add(title);
        }
        return filteredTitles;
    }

    public static void rerankRedundantSingleTokenTitles(List<RankedTitle> tokens) {

        for(int i = 0; i < tokens.size(); i++){
            for(int j = 0; j < tokens.size(); j++){
                if(i == j){
                    continue;
                }
 if(tokens.get(i).getTitle().contains(tokens.get(j).getTitle())){
                    int freq1 = tokens.get(i).getFrequency();
                    int freq2 = tokens.get(j).getFrequency();

                    if(freq2 < 3 * freq1){
                        tokens.get(j).setFrequency(-1);
                    }

                }
            }
        }
    }

    public static void rerankTitlesByNumberOfPhotos(List<RankedTitle> listOfRankedTitles, int numberOfPhotos) {

        int threshold = Math.max( (int) Math.floor((double)numberOfPhotos / (double)8) , 3);
        for(RankedTitle title : listOfRankedTitles){
            if(title.getFrequency() < threshold){
                title.setFrequency(0);
            }
        }
    }

    public static String upperCaseWords(String line)
    {
//        line = line.trim().toLowerCase();
        String data[] = line.split("\\s");
        line = "";
        for(int i =0;i< data.length;i++)
        {
            if(data[i].length()>1)
                line = line + data[i].substring(0,1).toUpperCase()+data[i].substring(1)+" ";
            else
                line = line + data[i].toUpperCase();
        }
        return line.trim();
    }

    public static void addCombinationsCounts(String sentence,HashMap<String,Integer> combinationsCounts) {
            String[] parts = sentence.split("\\s");
            String nextSentence="";
            int length = 2;
            while( length <= MAX_TOKEN_LENGTH && length <= parts.length ){
                for(int i = 0; i < (parts.length - length) + 1 ; i++){
                    nextSentence=getOneCombination(parts, i, length);
                    Integer count=combinationsCounts.get(nextSentence);
                    if(count==null)
                        combinationsCounts.put(nextSentence, 1);
                    else
                        combinationsCounts.put(nextSentence, count+1);
                }
                length++;
//            }
        }
            
    }

    public static String getOneCombination(String[] parts, int start, int length) {
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < length; i++){
            buf.append(parts[start + i] + " ");
        }
        return buf.substring(0, buf.length()-1).toString();
    }
 

    public static List<String> extractTokens(String[] titleTokens, List<Integer> uselessWordsIndexes) {
        List<String> tokens = new ArrayList<String>();
        if(titleTokens.length == 0){
            return tokens;
        }

        if(uselessWordsIndexes.size() == 0){
            tokens.add(extractTitleFromTokens(titleTokens, 0, titleTokens.length-1));
            return tokens;
        }

        for (int i = 1; i < uselessWordsIndexes.size(); i++){
            int index1 = uselessWordsIndexes.get(i-1)+1;
            int index2 =  uselessWordsIndexes.get(i)-1;
            if (index1 > index2){
                continue;
            }
            tokens.add(extractTitleFromTokens(titleTokens, index1, index2));
        }

        return tokens;
    }

    protected static String extractTitleFromTokens(String[] tokens, int index0, int index1) {
        if (index0 < 0 || index1 > tokens.length || index1 < index0){
            throw new IllegalArgumentException("Inappropriate input arguments: " + index0 + ", " + index1);
        }
        StringBuffer buf = new StringBuffer();
        for (int i = index0; i < index1; i++){
            buf.append(tokens[i] + " ");
        }
        buf.append(tokens[index1]);

        return buf.toString();
    }


    public static List<Integer> findUselessWordsInTitle(String[] titleTokens) {
        List<Integer> indexes = new ArrayList<Integer>();
        indexes.add(-1);
        for(int i = 0; i < titleTokens.length; i++){
            if(!acceptTag(titleTokens[i])){
                indexes.add(i);
            }
        }
        indexes.add(titleTokens.length);
        return indexes;
    }


    public static boolean acceptTag(String tag){
      
        if (isNumeric(tag)){
            return false;
        }
        if(isOneLetterLength(tag)){
            return false;
        }
        
        if(containsStrangeCharacterSequences(tag)){
            return false;
        }
        if(hasMoreNumbersThanLetters(tag)){
            return false;
        }
        if(isScreenname(tag)) {
            return false;
        }
        return true;
    }

        public static boolean isOneLetterLength(String tag) {
        if(tag.length() > 1){
            return false;
        }
        return true;
    }

        public static boolean isScreenname(String tag) {
            if(tag.startsWith("@")){
                return true;
            }
            return false;
        }

    public static boolean isNumeric(String tag){
        for (int x = 0; x < tag.length(); x++){
            if (!Character.isDigit(tag.charAt(x))){
                return false;
            }
        }
        return true;
    }

    public static boolean containsStrangeCharacterSequences(String tag){
        for(int i = 0; i < strangeSequencesIntoTags.length; i++){
            if(tag.startsWith(strangeSequencesIntoTags[i])){
                return true;
            }
        }
        return false;
    }

    private static final String[] strangeSequencesIntoTags = {
        "img_",
        "img-",
        "dmc-",
        "dmc",
        "img",
        "finepix",
        "dsc",
        "jpg",
        "jpeg"
    };
 
    public static boolean hasMoreNumbersThanLetters(String tag){
        int numberOfDigits = 0;
        int numberOfLetters = 0;
        for(int i = 0; i < tag.length(); i++){
            if(Character.isDigit(tag.charAt(i))){
                numberOfDigits++;
            }else{
                numberOfLetters++;
            }
        }
        if(numberOfDigits > numberOfLetters){
            return true;
        }
        return false;
    }

    public static boolean hasHighDigitToletterRatio(String tag, double threshold){
        int numberOfDigits = 0;
        int numberOfLetters = 0;
        for(int i = 0; i < tag.length(); i++){
            if(Character.isDigit(tag.charAt(i))){
                numberOfDigits++;
            }else if (Character.isLetter(tag.charAt(i))){
                numberOfLetters++;
            }
        }
        if(numberOfDigits > threshold * numberOfLetters){
            return true;
        }
        return false;
    }

    public static String grabTitleFromURL(String url){
        Parser parser;
        String result=null;
        try {
            parser = new Parser(url);
//            TagNameFilter[] tagNamesFilter = { new TagNameFilter("title"), new TagNameFilter("h1"), new TagNameFilter("h2"), new TagNameFilter("h3")  };
            TagNameFilter[] tagNamesFilter = { new TagNameFilter("title") };
            OrFilter orTagNameFilter = new OrFilter(tagNamesFilter);
            NodeList list = parser.parse(orTagNameFilter);
            SimpleNodeIterator nodeElements = list.elements();
            Node node;
            if(nodeElements.hasMoreNodes()){
                node = nodeElements.nextNode();
                result=node.toPlainTextString().trim();
            }
        } catch (ParserException ex) {
        }
        return result;
    }

    public static List<String> grabKeyElementsFromURL(String url){
        Parser parser;
        List<String> result=new ArrayList<String>();
        try {
            parser = new Parser(url);
//            TagNameFilter[] tagNamesFilter = { new TagNameFilter("title"), new TagNameFilter("h1"), new TagNameFilter("h2"), new TagNameFilter("h3")  };
            TagNameFilter[] tagNamesFilter = { new TagNameFilter("title"), new TagNameFilter("h1"), new TagNameFilter("h2") };
            OrFilter orTagNameFilter = new OrFilter(tagNamesFilter);
            NodeList list = parser.parse(orTagNameFilter);
            SimpleNodeIterator nodeElements = list.elements();
            Node node;
            while (nodeElements.hasMoreNodes())
            {
                    node = nodeElements.nextNode();
                    result.add(node.toPlainTextString().trim());
            }
        } catch (ParserException ex) {
        }
        return result;
    }
    
    
    public static List<String> getSentences1(String text, Set<String> entities){
        text=text.trim();
        text=StringEscapeUtils.escapeHtml(text);
        text=text.replaceAll("http:.*&hellip;\\z","");
        String[] toMatch={"\\ART\\s+@\\S+","\\AMT\\s+@\\S+"};
        for(String t:toMatch){
                Pattern pattern = Pattern.compile(t, Pattern.CASE_INSENSITIVE);
                String newTweet = text.trim();
                text="";
                while(!newTweet.equals(text)){         //each loop will cut off one "RT @XXX" or "#XXX"; may need a few calls to cut all hashtags etc.
                        text=newTweet;
                        Matcher matcher = pattern.matcher(text);
                        newTweet = matcher.replaceAll("");
                        newTweet =newTweet.trim();
                }
        }
        text=text.replaceAll("-\\s*\\z","");
        text=text.replaceAll("&hellip;\\z","");
        text=StringEscapeUtils.unescapeHtml(text);
        text=text.trim();
        String[] parts=text.split(Extractor.urlRegExp);
        List<String> sentences=new ArrayList<String>();
        
        for(int i=0;i<parts.length;i++){
//            parts[i]=text.replace("http://*&hellip;","");
            String text_cleaned=extractor.cleanText(parts[i]);
//            List<String> sentences_tmp=new ArrayList<String>();
            Reader reader = new StringReader(text_cleaned);
            DocumentPreprocessor dp = new DocumentPreprocessor(reader);
            dp.setTokenizerFactory(PTBTokenizerFactory.newWordTokenizerFactory("ptb3Escaping=false"));
            
            Iterator<List<HasWord>> it = dp.iterator();
            while (it.hasNext()) {
                StringBuilder sentenceSb = new StringBuilder();
                List<HasWord> sentence = it.next();
                boolean last_keep=false;
                for (HasWord token : sentence) {
                    if((!token.word().matches("[,:!.;?)]"))&&(!token.word().contains("'"))&&!last_keep){
                        sentenceSb.append(" ");
                    }
                    last_keep=false;
                    if(token.word().matches("[(\\[]"))
                            last_keep=true;
                    String next_word=token.toString();
                      
                    if((next_word.toUpperCase().equals(next_word))&&(!next_word.equals("I"))&&(!entities.contains(next_word)))
                        next_word=next_word.toLowerCase();
                    if(next_word.equals("i")) next_word="I";
                    sentenceSb.append(next_word);
                }
                String new_sentence=sentenceSb.toString().trim();
                Character fc=new_sentence.charAt(0);
                new_sentence=fc.toString().toUpperCase()+new_sentence.substring(1);
                if(new_sentence.endsWith(":"))
                    text=text.substring(0,text.length()-3)+".";

                sentences.add(new_sentence);
            }
  //          sentences.addAll(sentences_tmp);
        }
        return sentences;
    }
    
    public static List<String> getSentences2(String text){
        String[] parts=text.split(Extractor.urlRegExp);
        List<String> sentences=new ArrayList<String>();
        for(int i=0;i<parts.length;i++){
            String text_cleaned=extractor.cleanText(parts[i]);
            Properties props = new Properties();
            props.put("annotators", "tokenize, ssplit");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
            // create an empty Annotation just with the given text
            Annotation document = new Annotation(text_cleaned);
            // run all Annotators on this text
            pipeline.annotate(document);
            // these are all the sentences in this document
            // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
            List<CoreMap> sentences_c = document.get(SentencesAnnotation.class);

            for(CoreMap sentence: sentences_c) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
                String new_sentence="";
                for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                    if((!token.word().matches("[\\p{Punct}]"))&&(!token.word().contains("'"))){
                        new_sentence=new_sentence+" ";
                    }
                    String next_word=token.word();
                    if (next_word.toUpperCase().equals(next_word))
                        next_word=next_word.toLowerCase();
                    new_sentence=new_sentence+token;

                }
                new_sentence=new_sentence.trim();
                Character fc=new_sentence.charAt(0);
                new_sentence=fc.toString().toUpperCase()+new_sentence.substring(1);
                sentences.add(new_sentence);
            }
//            sentences.addAll(sentences_tmp);
        }        
        return sentences;
    }
    
    public static String getSiteNameFromURL(String url_str){
        String media=null;
        try {
            URL url=new URL(url_str);
            String host=url.getHost();
            String[] parts=host.split("\\.");
            for(int i=0;i<parts.length;i++)
                if((!parts[i].equals("www"))&&(!parts[i].equals("en"))) return parts[i];
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        return media;
        
    }
    
    private static String cleanTitleFromCommonMediaNames(String title){
        title=title.trim();
        return title.replace("- ITV News","").replace("- Bloomberg","").replace("- CNN.com","").replace("| News | Pitchfork","").replace("- The Next Web","").replace("- CBS News Video","").replace("- NYTimes.com","").replace(" - YouTube","").replace("- WSJ.com","").replace(": Pressparty","").replace(" :: Beatport Play","").replace(" [VIDEO]","").replace(": The New Yorker","").replace("(Video)","").replace("Business News & Financial News - The Wall Street Journal - Wsj.com","").replace("| TIME.com","").replace("NME Magazine","");
    }
    
    static String[] dyscoIds=new String[]{
"c28c26dd-f373-4975-8fcb-d7600c6a1979",
"dd6ba020-408f-415b-b803-879971aa90ef",
"1747f8e6-dc8d-45b9-8d29-05eac42864fa",
"0cc3a5f1-4963-4e21-a47c-bad176be0f16"};    
}

