package eu.socialsensor.documentpivot;

import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.documentpivot.vocabulary.Vocabulary;
import eu.socialsensor.framework.common.services.GenericDyscoCreator;
import eu.socialsensor.documentpivot.LSH.HashTables;
import eu.socialsensor.documentpivot.dyscoutils.DyscoUtils;
import eu.socialsensor.documentpivot.model.VectorSpace;
import eu.socialsensor.documentpivot.model.RankedObject;
import eu.socialsensor.documentpivot.preprocessing.TweetPreprocessor;
import eu.socialsensor.framework.client.dao.ItemDAO;
import eu.socialsensor.framework.client.dao.impl.ItemDAOImpl;
import java.util.*;
import java.util.Map.Entry;
import org.apache.lucene.index.Term;

/**
 *
 * @author gpetkos
 *
 * This is the central class that implements document-pivot topic detection
 * using LSH (as in "Streaming First Story Detection with application to
 * Twitter", Petrovic, et.al).
 */
public class DyscoCreator {

    private static Vocabulary vocabulary_reference;
    private static int L = 12, k = 16;
    private static eu.socialsensor.documentpivot.model.Vocabulary vocabulary;
    private static double similarity_threshold = 0.1;
    private static double def_similarity_threshold = 0.1;
    private static int minimum_cluster_size;

    public DyscoCreator() {
        eu.socialsensor.documentpivot.Constants.configuration=new eu.socialsensor.documentpivot.Configuration();
        similarity_threshold=Double.parseDouble(Utilities.readProperty(eu.socialsensor.documentpivot.Constants.SIMILARITY_THRESHOLD,eu.socialsensor.documentpivot.Constants.configuration.getConfig()));
        minimum_cluster_size=Integer.parseInt(Utilities.readProperty(eu.socialsensor.documentpivot.Constants.MIN_NO_OF_DOCUMENTS_PER_CLUSTER,eu.socialsensor.documentpivot.Constants.configuration.getConfig()));
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
            vocabulary_reference.load(false);
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

        postIteratorNew = postsList.values().iterator();
        clusters = clusterTweets(postIteratorNew);
        
        
        eu.socialsensor.framework.common.domain.dysco.Dysco tmp_dysco = new Dysco();
        List<Dysco> dyscos = new ArrayList<Dysco>();

        for (Entry<String, List<String>> tmp_entry : clusters.entrySet()) {
            if(tmp_entry.getValue().size()>=minimum_cluster_size){
                Dysco dysco = new Dysco();
                dysco.setId(UUID.randomUUID().toString());
                dysco.setTitle("");
                Map<String,Double> keywords = new HashMap<String,Double>();
                List<Item> assigned_posts = new ArrayList<Item>();
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
                List<String> terms = TweetPreprocessor.Tokenize(tmp_post, true, false, true);
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
    public static Map<String, List<String>> clusterTweets(Iterator<Item> postsIterator) {
        Map<String, List<String>> clusters = new HashMap<String, List<String>>();
        int d = vocabulary.size();
        HashTables hashTables = new HashTables(L, k, d);

        try {
            Item tmp_post = null;
            while (postsIterator.hasNext()) {
                tmp_post = postsIterator.next();
                List<String> tokens = TweetPreprocessor.Tokenize(tmp_post, true, false, true);
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
    
    
}
