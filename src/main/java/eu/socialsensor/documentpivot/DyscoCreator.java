package eu.socialsensor.documentpivot;

import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.documentpivot.vocabulary.Vocabulary;
import eu.socialsensor.documentpivot.LSH.HashTables;
import eu.socialsensor.documentpivot.dyscoutils.DyscoUtils;
import eu.socialsensor.documentpivot.model.VectorSpace;
import eu.socialsensor.documentpivot.model.RankedObject;
import eu.socialsensor.documentpivot.preprocessing.TweetPreprocessor;
import eu.socialsensor.entitiesextractor.EntityDetection;
import eu.socialsensor.framework.client.dao.ItemDAO;
import eu.socialsensor.framework.client.dao.impl.ItemDAOImpl;
import eu.socialsensor.framework.common.domain.dysco.Entity;
import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author gpetkos
 *
 * This is the central class that implements document-pivot topic detection
 * using LSH (as in "Streaming First Story Detection with application to
 * Twitter", Petrovic, et.al).
 */
public class DyscoCreator {

    private Vocabulary vocabulary_reference;
    private int L = 12, k = 16;
    private static eu.socialsensor.documentpivot.model.Vocabulary vocabulary;
    private double similarity_threshold = 0.1;
    private static final double def_similarity_threshold = 0.1;
    private int minimum_cluster_size;
    private boolean filter_hashtags;
    private boolean filter_urls;
    private boolean filter_user_mentions;
    private int boost_hashtags_factor;
    private int boost_entities_factor;
    private boolean perform_full_indexing;
    
    
    public DyscoCreator() {
        eu.socialsensor.documentpivot.Constants.configuration=new eu.socialsensor.documentpivot.Configuration();
        similarity_threshold=Double.parseDouble(Utilities.readProperty(eu.socialsensor.documentpivot.Constants.SIMILARITY_THRESHOLD,eu.socialsensor.documentpivot.Constants.configuration.getConfig()));
        minimum_cluster_size=Integer.parseInt(Utilities.readProperty(eu.socialsensor.documentpivot.Constants.MIN_NO_OF_DOCUMENTS_PER_CLUSTER,eu.socialsensor.documentpivot.Constants.configuration.getConfig()));
        String str_filter_hashtags=Utilities.readProperty(eu.socialsensor.documentpivot.Constants.FILTER_HASHTAGS,eu.socialsensor.documentpivot.Constants.configuration.getConfig()).toLowerCase().trim();
        if(str_filter_hashtags.equals("true"))
            filter_hashtags=true;
        else
            filter_hashtags=false;
        String str_filter_urls=Utilities.readProperty(eu.socialsensor.documentpivot.Constants.FILTER_URLS,eu.socialsensor.documentpivot.Constants.configuration.getConfig()).toLowerCase().trim();
        if(str_filter_urls.equals("true"))
            filter_urls=true;
        else
            filter_urls=false;
        String str_filter_user_mentions=Utilities.readProperty(eu.socialsensor.documentpivot.Constants.FILTER_USER_MENTIONS,eu.socialsensor.documentpivot.Constants.configuration.getConfig()).toLowerCase().trim();
        if(str_filter_user_mentions.equals("true"))
            filter_user_mentions=true;
        else
            filter_user_mentions=false;

        boost_hashtags_factor=Integer.parseInt(Utilities.readProperty(eu.socialsensor.documentpivot.Constants.BOOST_HASHTAGS_FACTOR,eu.socialsensor.documentpivot.Constants.configuration.getConfig()));
        boost_entities_factor=Integer.parseInt(Utilities.readProperty(eu.socialsensor.documentpivot.Constants.BOOST_ENTITIES_FACTOR,eu.socialsensor.documentpivot.Constants.configuration.getConfig()));
        VectorSpace.boost_entities_factor=boost_entities_factor;
        VectorSpace.boost_hashtags_factor=boost_hashtags_factor;
        TweetPreprocessor.filterHashtags=filter_hashtags;
        TweetPreprocessor.filterURLs=filter_urls;
        TweetPreprocessor.filterUserMentions=filter_user_mentions;
        
        String str_perform_full_indexing=Utilities.readProperty(eu.socialsensor.documentpivot.Constants.PERFORM_FULL_INDEXING,eu.socialsensor.documentpivot.Constants.configuration.getConfig()).toLowerCase().trim();
        if(str_perform_full_indexing.equals("true"))
            perform_full_indexing=true;
        else
            perform_full_indexing=false;
        
        
    }
    
    
    
    /*
     * Takes as input a list of items and a similarity threshold and returns a
     * list of DySCOs
     */
    public List<Dysco> createDyscos(List<Item> items, Double similarity_threshold_run) {
        if (similarity_threshold_run != null) {
            similarity_threshold = similarity_threshold_run;
        } else {
            similarity_threshold = def_similarity_threshold;
        }
        return createDyscos(items);
    }

    /*
     * Takes as input a list of items and returns a list of DySCOs
     */
    public List<Dysco> createDyscos(List<Item> items) {
        if (vocabulary_reference == null) {
            vocabulary_reference = new Vocabulary();
            vocabulary_reference.load(true);
        }
        Map<String, Item> postsList = new HashMap<String, Item>();
        Item tmp_post;
        Iterator<Item> postIterator = items.iterator();
        while (postIterator.hasNext()) {
            tmp_post = postIterator.next();
            postsList.put(tmp_post.getId(), tmp_post);
        }

        Iterator<Item> postIteratorNew = postsList.values().iterator();
        vocabulary = eu.socialsensor.documentpivot.model.Vocabulary.createVocabulary(postIteratorNew);
        int d = vocabulary.size();
        HashTables hashTables = new HashTables(L, k, d);

        Map<String, List<String>> clusters;
        if(perform_full_indexing)
            clusters = clusterTweetsFull(items);
        else
            clusters = clusterTweets(items);
        
        eu.socialsensor.framework.common.domain.dysco.Dysco tmp_dysco = new Dysco();
        List<Dysco> dyscos = new ArrayList<Dysco>();

        for (Entry<String, List<String>> tmp_entry : clusters.entrySet()) {
            if((tmp_entry.getValue().size()+1)>=minimum_cluster_size){
                Dysco dysco = new Dysco();
                dysco.setId(UUID.randomUUID().toString());
                dysco.setTitle("");
                Map<String,Double> keywords = new HashMap<String,Double>();
                List<Item> assigned_posts = new ArrayList<Item>();
                assigned_posts.add(postsList.get(tmp_entry.getKey()));
                for (String tmp_str : tmp_entry.getValue()) {
                    tmp_post = postsList.get(tmp_str);
                    assigned_posts.add(tmp_post);
                    //keywords.add(tmp_str);
                }
                dysco.setKeywords(keywords);
                dysco.setItems(assigned_posts);
                dysco.setScore((double) assigned_posts.size());
                if (assigned_posts.size() > 0) {
                    dyscos.add(dysco);
                }
            }
        }

        Collections.sort(dyscos, new Comparator<Dysco>() {

            @Override
            public int compare(Dysco p1, Dysco p2) {
                return (new Double(p2.getScore() - p1.getScore())).intValue();
            }
        });

        int i;
        for (i = 0; i < dyscos.size(); i++) {
            tmp_dysco = dyscos.get(i);
            Map<String, Double> freqs = new HashMap<String, Double>();
            List<Item> assigned_posts = tmp_dysco.getItems();
            for (int j = 0; j < assigned_posts.size(); j++) {
                tmp_post = assigned_posts.get(j);
                
                List<String> terms = TweetPreprocessor.Tokenize(tmp_post.getTitle());
                for (String tmp_term : terms) {
                    Double tmp_freq = freqs.get(tmp_term);
                    if (tmp_freq == null) {
                        freqs.put(tmp_term, 1.0);
                    } else {
                        freqs.put(tmp_term, tmp_freq + 1.0);
                    }
                }
            }

            for (Entry<String, Double> tmp_entry : freqs.entrySet()) {
                double likelihood = vocabulary_reference.getTermFrequency(tmp_entry.getKey());
                double old_val = tmp_entry.getValue();
                tmp_entry.setValue(old_val * (1 / (1 + likelihood)));
            }

            List<Double> values = new ArrayList<Double>();
            for (Entry<String, Double> tmp_entry : freqs.entrySet()) {
                values.add(tmp_entry.getValue());
            }
            Comparator mycomparator = Collections.reverseOrder();
            Collections.sort(values, mycomparator);
            
            //In order to extract keywords, the keyword extractor, outside the dysco creator, is used
            /*
            Set<String> keywords = new HashSet<String>();
            for (int p = 0; (p < 10) && (p < values.size()); p++) {
                Double nextVal = values.get(p);
                for (Entry<String, Double> tmp_entry : freqs.entrySet()) {
                    if ((tmp_entry.getValue() == nextVal) && (!keywords.contains(tmp_entry.getKey()))) {
                        keywords.add(tmp_entry.getKey());
                    }
                }
            }
            tmp_dysco.setKeywords(new ArrayList<String>(keywords));
            */

        }

        //The following code removes near duplicate topics
        List<Dysco> dyscosToRemove = new ArrayList<Dysco>();
        for (int kk = 0; kk < dyscos.size(); kk++) {
            Dysco dysco1 = dyscos.get(kk);
            for (int ll = kk + 1; ll < dyscos.size(); ll++) {
                Dysco dysco2 = dyscos.get(ll);
                double jac_sim = DyscoUtils.dyscoSimilarity(dysco1, dysco2);
                if (jac_sim > 0.5) {
                    dyscosToRemove.add(dysco2);
                    ll = dyscos.size();
                }
            }
        }

        dyscos.removeAll(dyscosToRemove);

        hashTables.clear();
        List<Dysco> new_list = new ArrayList<Dysco>(dyscos);
        return new_list;
    }

    /*
     * This is the function that performs the clustering of items / tweets.
     * It is called by createDyscos, which further processes the clusters produced
     * by clusterTweets to generate DySCOs.
     */
    public Map<String, List<String>> clusterTweets(List<Item> items) {
        Map<String, List<String>> clusters = new HashMap<String, List<String>>();
        int d = vocabulary.size();
        HashTables hashTables = new HashTables(L, k, d);

        //Get Entities Strings
        Set<String> entitiesSet=new HashSet<String>();
        for(Item tmp_post:items){
            List<Entity> entities=tmp_post.getEntities();
            if(entities!=null)
                for(Entity entity:entities){
                    String[] parts=entity.getName().split("\\s+");
                    for(int i=0;i<parts.length;i++)
                        entitiesSet.add(parts[i]);
                }
        }
        VectorSpace.entities=entitiesSet;
        
        try {
            for(Item tmp_post:items){
                List<String> tokens = TweetPreprocessor.Tokenize(tmp_post.getTitle());
                VectorSpace vsm = new VectorSpace(tmp_post.getId(), tokens);
                RankedObject nearest = hashTables.getNearest(vsm);

                if (nearest == null || nearest.getSimilarity() < similarity_threshold) {
                    clusters.put(vsm.id(), new ArrayList<String>());
                    hashTables.add(vsm);
                } else {
                    List<String> cluster = clusters.get(nearest.getId());
                    if (cluster == null) {
                        cluster = new ArrayList<String>();
                        clusters.put(nearest.getId(), cluster);
                    }
                    cluster.add(tmp_post.getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clusters;
    }

    
    /*
     * This is the function that performs the clustering of items / tweets.
     * It is called by createDyscos, which further processes the clusters produced
     * by clusterTweets to generate DySCOs.
     */
    public Map<String, List<String>> clusterTweetsFull(List<Item> items) {
        Map<String, List<String>> clusters = new HashMap<String, List<String>>();
        int d = vocabulary.size();
        HashTables hashTables = new HashTables(L, k, d);

        //Get Entities Strings
        Set<String> entitiesSet=new HashSet<String>();
        for(Item tmp_post:items){
            List<Entity> entities=tmp_post.getEntities();
            if(entities!=null)
                for(Entity entity:entities){
                    String[] parts=entity.getName().split("\\s+");
                    for(int i=0;i<parts.length;i++)
                        entitiesSet.add(parts[i]);
                }
        }
        VectorSpace.entities=entitiesSet;

        
        Map<String,String> itemToClusterMap=new HashMap<String,String>();
        
        try {
            for(Item tmp_post:items){
                List<String> tokens = TweetPreprocessor.Tokenize(tmp_post.getTitle());
                VectorSpace vsm = new VectorSpace(tmp_post.getId(), tokens);
                RankedObject nearest = hashTables.getNearest(vsm);
                hashTables.add(vsm);

                if (nearest == null || nearest.getSimilarity() < similarity_threshold) {
                    clusters.put(vsm.id(), new ArrayList<String>());
                    itemToClusterMap.put(vsm.id(), vsm.id());
//                    hashTables.add(vsm);
                } else {
                    String matchingCluster=itemToClusterMap.get(nearest.getId());
                    List<String> cluster = clusters.get(matchingCluster);
                    if (cluster == null) {
                        cluster = new ArrayList<String>();
                        clusters.put(nearest.getId(), cluster);
                    }
                    itemToClusterMap.put(vsm.id(),matchingCluster);
                    cluster.add(tmp_post.getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clusters;
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
        DyscoCreator dc=new DyscoCreator();
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
