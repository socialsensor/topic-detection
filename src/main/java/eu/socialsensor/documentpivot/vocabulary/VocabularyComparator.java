/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.socialsensor.documentpivot.vocabulary;

import eu.socialsensor.documentpivot.dfidft.DFIDFTDirectory;
import java.io.*;
import java.util.*;
import eu.socialsensor.documentpivot.termfeature.TermFeature;
import eu.socialsensor.documentpivot.termlikelihood.TermLikelihood;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import java.util.Map.Entry;
import eu.socialsensor.documentpivot.dyscoutils.DyscoUtils;
import eu.socialsensor.graphbased.clust.Community;
import eu.socialsensor.graphbased.clust.MyLink;
import eu.socialsensor.graphbased.clust.ScanCommunityDetector;
import eu.socialsensor.graphbased.clust.ScanCommunityStructure;
import eu.socialsensor.documentpivot.Utilities;
import eu.socialsensor.documentpivot.Constants;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.dysco.Ngram;
import org.apache.lucene.index.Term;
/**
 *
 * @author gpetkos
 */
public class VocabularyComparator {
    public Vocabulary vocabulary_new_corpus;
    public Vocabulary vocabulary_reference;
    List<TermLikelihood> terms;
    Map<String,TermLikelihood> termsMap;
    
    public VocabularyComparator(Vocabulary vocabulary_new_corpus, Vocabulary vocabulary_reference) {
        this.vocabulary_new_corpus = vocabulary_new_corpus;
        this.vocabulary_reference = vocabulary_reference;
        int n_items=vocabulary_new_corpus.size();
        terms=new ArrayList<TermLikelihood>();
        termsMap=new HashMap<String,TermLikelihood>();
        compare();
    }
       
    public void compare(){
        for(TermFeature termfeature:vocabulary_new_corpus.terms.values()){
            double likelihood_new_corpus=vocabulary_new_corpus.getTermLikelihood(termfeature.name);
            double likelihood_reference=vocabulary_reference.getTermLikelihood(termfeature.name);
            double term_frequency=vocabulary_new_corpus.getTermFrequency(termfeature.name);
            double likelihood_ratio=(likelihood_new_corpus/likelihood_reference)*Math.log(term_frequency)*Math.log(term_frequency);
            TermLikelihood tmp_likelihood=new TermLikelihood(termfeature,likelihood_ratio);
            if(tmp_likelihood.term.name.length()>2){
                terms.add(tmp_likelihood);
                termsMap.put(tmp_likelihood.term.name, tmp_likelihood);
            }
        }
        
        Collections.sort(terms, new Comparator<TermLikelihood>() {

        public int compare(TermLikelihood o1, TermLikelihood o2) {
            if(o1.likelihood_ratio>o2.likelihood_ratio) return -1;
            if(o1.likelihood_ratio<o2.likelihood_ratio) return 1;
            return 0;
        }
        });
    }
   
    public void outputOrdered(String filename){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(filename), "UTF8"));
            for (TermLikelihood p : terms) {
                writer.append(p.term.name + "\t" + p.likelihood_ratio);
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
    
    public void filterByRatio(double minRatio){
        ArrayList<TermLikelihood> new_terms=new ArrayList<TermLikelihood>();
        HashMap<String,TermLikelihood> new_termsMap=new HashMap<String,TermLikelihood>();
        for(TermLikelihood tmp_tl:terms){
            if(tmp_tl.likelihood_ratio>minRatio){
                new_terms.add(tmp_tl);
                new_termsMap.put(tmp_tl.term.name, tmp_tl);
            }
        }
        terms=new_terms;
        termsMap=new_termsMap;
    }


        public List<Dysco> getDyscosBySFIM(Map<String,Item> items_map){
            int i,j;
            List<Dysco> dyscos=new ArrayList<Dysco>();
            //First filter the nodes
            double term_threshold=0;
            
            String term_selection_type_string=Utilities.readProperty(eu.socialsensor.sfim.Constants.TERM_SELECTION_METHOD,eu.socialsensor.sfim.Constants.configuration.getConfig());
            Constants.TERM_SELECTION_TYPES term_selection_type=Constants.TERM_SELECTION_TYPES.valueOf(term_selection_type_string);
            if(term_selection_type==Constants.TERM_SELECTION_TYPES.RATIO_THRESHOLD)
                term_threshold=Double.parseDouble(Utilities.readProperty(eu.socialsensor.sfim.Constants.TERM_SELECTION_RATIO_THRESHOLD,eu.socialsensor.sfim.Constants.configuration.getConfig()));
            if(term_selection_type==Constants.TERM_SELECTION_TYPES.TOP_N){
//                term_threshold=terms.get(terms.size()-Constants.TERM_SELECTION_TOP_N+1).likelihood_ratio;
                int top_n=Integer.parseInt(Utilities.readProperty(eu.socialsensor.sfim.Constants.TERM_SELECTION_TOP_N,eu.socialsensor.sfim.Constants.configuration.getConfig()));
                term_threshold=terms.get(top_n+1).likelihood_ratio;
            }
            if(term_selection_type==Constants.TERM_SELECTION_TYPES.TOP_PERCENTAGE){
                double top_percentage=Double.parseDouble(Utilities.readProperty(eu.socialsensor.sfim.Constants.TERM_SELECTION_TOP_PERCENTAGE,eu.socialsensor.sfim.Constants.configuration.getConfig()));
                term_threshold=terms.get((int) Math.round(top_percentage*terms.size())+1).likelihood_ratio;
            }

            int min_topic_size=Integer.parseInt(Utilities.readProperty(eu.socialsensor.sfim.Constants.MIN_TOPIC_SIZE,eu.socialsensor.sfim.Constants.configuration.getConfig()));
                     
            
            System.out.println("Threshold : "+term_threshold);
            filterByRatio(term_threshold);
            int n_terms=terms.size();
            Set<TermLikelihood> tmp_topic_terms;
            TermLikelihood working_term;
            double working_term_length;
            double[] tmp_similarities=new double[n_terms];
            double[] tmp_probabilities=new double[n_terms];
            Random rnd=new Random();
            System.out.println("n_terms : "+n_terms);
            for(i=0;i<n_terms;i++){
                tmp_topic_terms=new HashSet<TermLikelihood>();
                tmp_topic_terms.add(terms.get(i));
                working_term=new TermLikelihood(terms.get(i));
                working_term_length=working_term.length();
                
                boolean finished=false;
                while(!finished){
                    for(j=0;j<n_terms;j++){
                        TermLikelihood tmp_term=terms.get(j);
                        if(!tmp_topic_terms.contains(tmp_term)){
                                Set<String> intersection = new HashSet<String>(working_term.term.docs.keySet());
                                intersection.retainAll(tmp_term.term.docs.keySet());
                                double no_of_cooccurrences=(double) intersection.size();
                                Map<String,Integer> smallest_set;
                                Map<String,Integer> largest_set;
                                if(working_term.term.docs.size()<tmp_term.term.docs.size()){
                                    smallest_set=working_term.term.docs;
                                    largest_set=tmp_term.term.docs;
                                }
                                else{
                                    largest_set=working_term.term.docs;
                                    smallest_set=tmp_term.term.docs;
                                }
                                double numerator=0;
                                for(Entry<String,Integer> tmp_entry:smallest_set.entrySet()){
                                    Integer second_freq=largest_set.get(tmp_entry.getKey());
                                    if(second_freq!=null)
                                        numerator=numerator+second_freq*tmp_entry.getValue();
                                }
                                
                                double denumerator=working_term_length*Math.sqrt((double) tmp_term.term.docs.size());
                                double factor2=((double) no_of_cooccurrences)/((double) largest_set.size());
                                double factor3=((double) no_of_cooccurrences)/((double) smallest_set.size());
                                if(no_of_cooccurrences!=0)
                                    tmp_similarities[j]=numerator/denumerator;
                                else
                                    tmp_similarities[j]=0;
                        }
                         else
                            tmp_similarities[j]=0;
                    }
                    finished=true;
                    double sum=0;
                    double expansion_threshold=1-1/(1+Math.exp(((double)tmp_topic_terms.size()-5)/2));
                    for(j=0;j<n_terms;j++){
                        sum=sum+tmp_similarities[j];
                        if(tmp_similarities[j]>expansion_threshold)
                            finished=false;
                    }
                    if(!finished){
                        int pos=0;
                        for(j=0;j<n_terms;j++)
                            tmp_probabilities[j]=tmp_similarities[j]/sum;

                        double max_prob=tmp_probabilities[0];
                        for(j=1;j<n_terms;j++)
                            if(tmp_probabilities[j]>max_prob){
                                max_prob=tmp_probabilities[j];
                                pos=j;
                            }
                        TermLikelihood entry_term=terms.get(pos);
                        tmp_topic_terms.add(entry_term);
                        
                        for(Entry<String,Integer> tmp_entry:entry_term.term.docs.entrySet()){
                            Integer prev_freq=working_term.term.docs.get(tmp_entry.getKey());
                            if(prev_freq==null)
                                working_term.term.docs.put(tmp_entry.getKey(),1);
                            else
                                working_term.term.docs.put(tmp_entry.getKey(), prev_freq+1);
                        }
                        int threshold=(int) Math.ceil(((double)tmp_topic_terms.size())/2);
                        Set<String> toRemove=new HashSet<String>();
                        for(Entry<String,Integer> tmp_entry:working_term.term.docs.entrySet())
                            if(tmp_entry.getValue()<threshold)
                                toRemove.add(tmp_entry.getKey());
                        for(String tmp_string:toRemove)
                           working_term.term.docs.remove(tmp_string);
                    }

                }
                
                if(tmp_topic_terms.size()>=min_topic_size){
                    Dysco tmp_dysco=new Dysco();
                    List<Ngram> keywords=new ArrayList<Ngram>();
                    List<Item> rel_items=new ArrayList<Item>();
                    for(TermLikelihood  tmp_tl:tmp_topic_terms){
                        String tmp_str=tmp_tl.term.name;
                        Ngram tmp_ngram=new Ngram(tmp_str,0.0f);
                        keywords.add(tmp_ngram);
                    }
                    for(String tmp_id:working_term.term.docs.keySet()){
                        Item next_rel_item=items_map.get(tmp_id);
                        if(next_rel_item!=null)
                            rel_items.add(next_rel_item);
                    }
                    tmp_dysco.setScore((float) working_term.term.length());
                    tmp_dysco.setNgrams(keywords);
                    tmp_dysco.setId(UUID.randomUUID().toString());
                    tmp_dysco.setItems(rel_items);
                    dyscos.add(tmp_dysco);
                }
            }
            
        Collections.sort(dyscos, new Comparator<Dysco>() {
        @Override public int compare(Dysco p1, Dysco p2) {
            return (int) (p2.getScore() - p1.getScore());
        }});

        List<Dysco> topicsToRemove=new ArrayList<Dysco>();
        for(int kk=0;kk<dyscos.size();kk++){
            Dysco dysco1=dyscos.get(kk);
            for(int ll=kk+1;ll<dyscos.size();ll++){
                Dysco dysco2=dyscos.get(ll);
                double jac_sim=DyscoUtils.dyscoSimilarity(dysco1, dysco2);
                if(jac_sim>0.75){
                    topicsToRemove.add(dysco2);
                    ll=dyscos.size();
                }
            }
        }
        
        dyscos.removeAll(topicsToRemove);


        boolean perform_reranking=true;
            List<Dysco> new_list=new ArrayList<Dysco>(dyscos);
           return new_list;
    }

    public Graph<TermLikelihood,MyLink> generateGraph(){
            eu.socialsensor.topicdetection.MainConstants.TERM_SIMILARITY_METHODS USED_TERM_SIMILARITY_METHOD;
            String term_similarity_method_string=Utilities.readProperty(eu.socialsensor.graphbased.Constants.TERM_SIMILARITY_METHOD,eu.socialsensor.graphbased.Constants.configuration.getConfig());
            USED_TERM_SIMILARITY_METHOD=eu.socialsensor.topicdetection.MainConstants.TERM_SIMILARITY_METHODS.valueOf(term_similarity_method_string);

            eu.socialsensor.graphbased.Constants.CORRELATION_SELECTION_TYPES USED_CORRELATION_SELECTION_TYPE;
            String correlation_type_string=Utilities.readProperty(eu.socialsensor.graphbased.Constants.CORRELATION_SELECTION_TYPE,eu.socialsensor.graphbased.Constants.configuration.getConfig());
            USED_CORRELATION_SELECTION_TYPE=eu.socialsensor.graphbased.Constants.CORRELATION_SELECTION_TYPES.valueOf(correlation_type_string);

            Graph<TermLikelihood,MyLink> termGraph;
            termGraph=new UndirectedSparseGraph<TermLikelihood,MyLink>();
            int i,j;
            
            //First filter the nodes
            double term_threshold=0;
            eu.socialsensor.graphbased.Constants.TERM_SELECTION_TYPES USED_TERM_SELECTION_TYPE=eu.socialsensor.graphbased.Constants.TERM_SELECTION_TYPES.valueOf(Utilities.readProperty(eu.socialsensor.graphbased.Constants.TERM_SELECTION_METHOD,eu.socialsensor.graphbased.Constants.configuration.getConfig()));
                   
            if(USED_TERM_SELECTION_TYPE==eu.socialsensor.graphbased.Constants.TERM_SELECTION_TYPES.RATIO_THRESHOLD)
                term_threshold=Double.parseDouble(Utilities.readProperty(eu.socialsensor.graphbased.Constants.TERM_SELECTION_RATIO_THRESHOLD,eu.socialsensor.graphbased.Constants.configuration.getConfig()));
            if(USED_TERM_SELECTION_TYPE==eu.socialsensor.graphbased.Constants.TERM_SELECTION_TYPES.TOP_N)
//                term_threshold=terms.get(terms.size()-Constants.TERM_SELECTION_TOP_N+1).likelihood_ratio;
                term_threshold=terms.get(Integer.parseInt(Utilities.readProperty(eu.socialsensor.graphbased.Constants.TERM_SELECTION_TOP_N,eu.socialsensor.graphbased.Constants.configuration.getConfig()))+1).likelihood_ratio;
            if(USED_TERM_SELECTION_TYPE==eu.socialsensor.graphbased.Constants.TERM_SELECTION_TYPES.TOP_PERCENTAGE)
//                term_threshold=terms.get((int) Math.round(Constants.TERM_SELECTION_TOP_PERCENTAGE*terms.size())+1).likelihood_ratio;
                term_threshold=terms.get((int) Math.round(Double.parseDouble(Utilities.readProperty(eu.socialsensor.graphbased.Constants.TERM_SELECTION_TOP_PERCENTAGE,eu.socialsensor.graphbased.Constants.configuration.getConfig()))*terms.size())).likelihood_ratio;
            
            
            filterByRatio(term_threshold);
            int n_terms=terms.size();
            for(i=0;i<n_terms;i++)
                termGraph.addVertex(terms.get(i));
            
            //Then link the edges
            System.out.println("Starting to compute correlations");
            float[][] graphArray=new float[n_terms][n_terms];
            TermLikelihood tl1;
            for(i=0;i<n_terms;i++){
                tl1=terms.get(i);
                for(j=i+1;j<n_terms;j++){
                    float tmp_res=(float) tl1.term.similarity(terms.get(j).term,USED_TERM_SIMILARITY_METHOD);
                    graphArray[i][j]=tmp_res;
                    graphArray[j][i]=tmp_res;
                }
            }
            
            
            for(i=0;i<n_terms;i++)
                graphArray[i][i]=0;
            System.out.println("Finished with correlations");
            
            if(USED_CORRELATION_SELECTION_TYPE==eu.socialsensor.graphbased.Constants.CORRELATION_SELECTION_TYPES.GLOBAL_AVERAGE_DEGREE){
                System.out.println("method : global average");
                ArrayList<Float> allSimilarities=new ArrayList<Float>();
                for(i=0;i<n_terms;i++){
                    for(j=i+1;j<n_terms;j++){
                        allSimilarities.add(new Float(graphArray[i][j]));
                    }
                }
                Collections.sort(allSimilarities);
                int n_full=allSimilarities.size();
                float edge_threshold=allSimilarities.get(n_full-Integer.parseInt(Utilities.readProperty(eu.socialsensor.graphbased.Constants.CORRELATION_SELECTION_AVERAGE_DEGREE,eu.socialsensor.sfim.Constants.configuration.getConfig()))*n_terms/2);
                
                if(edge_threshold==0) edge_threshold=0.0000001f;
                for(i=0;i<n_terms;i++)
                    for(j=i+1;j<n_terms;j++)
                        if(graphArray[i][j]>=edge_threshold)
                            termGraph.addEdge(new MyLink(graphArray[i][j]), terms.get(i), terms.get(j));
            }

            
            
            if(USED_CORRELATION_SELECTION_TYPE==eu.socialsensor.graphbased.Constants.CORRELATION_SELECTION_TYPES.FULL){
                for(i=0;i<n_terms;i++)
                    for(j=i+1;j<n_terms;j++)
                        if(graphArray[i][j]>0)
                            termGraph.addEdge(new MyLink(graphArray[i][j]), terms.get(i), terms.get(j));
            }

            if(USED_CORRELATION_SELECTION_TYPE==eu.socialsensor.graphbased.Constants.CORRELATION_SELECTION_TYPES.THRESHOLD){
                double CORRELATION_THRESHOLD=Double.parseDouble(Utilities.readProperty(eu.socialsensor.graphbased.Constants.CORRELATION_THRESHOLD,eu.socialsensor.graphbased.Constants.configuration.getConfig()));
                for(i=0;i<n_terms;i++)
                    for(j=i+1;j<n_terms;j++)
                        if(graphArray[i][j]>CORRELATION_THRESHOLD)
                            termGraph.addEdge(new MyLink(graphArray[i][j]), terms.get(i), terms.get(j));
            }
            
            
            
            if(USED_CORRELATION_SELECTION_TYPE==eu.socialsensor.graphbased.Constants.CORRELATION_SELECTION_TYPES.GLOBAL_N){
                ArrayList<Float> allSimilarities=new ArrayList<Float>();
                for(i=0;i<n_terms;i++){
                    for(j=i+1;j<n_terms;j++){
                        allSimilarities.add(new Float(graphArray[i][j]));
                    }
                }
                Collections.sort(allSimilarities);
                int n_full=allSimilarities.size();
                int CORRELATION_SELECTION_GLOBAL_N=Integer.parseInt(Utilities.readProperty(eu.socialsensor.graphbased.Constants.CORRELATION_SELECTION_GLOBAL_N,eu.socialsensor.graphbased.Constants.configuration.getConfig()));
                float edge_threshold=allSimilarities.get(n_full-CORRELATION_SELECTION_GLOBAL_N);
                if(edge_threshold==0) edge_threshold=0.0000001f;
                for(i=0;i<n_terms;i++)
                    for(j=i+1;j<n_terms;j++)
                        if(graphArray[i][j]>=edge_threshold)
                            termGraph.addEdge(new MyLink(graphArray[i][j]), terms.get(i), terms.get(j));
            }

            if(USED_CORRELATION_SELECTION_TYPE==eu.socialsensor.graphbased.Constants.CORRELATION_SELECTION_TYPES.GLOBAL_PERCENTAGE){
                ArrayList<Float> allSimilarities=new ArrayList<Float>();
                for(i=0;i<n_terms;i++){
                    for(j=i+1;j<n_terms;j++){
                        allSimilarities.add(new Float(graphArray[i][j]));
                    }
                }
                Collections.sort(allSimilarities);
                int n_full=allSimilarities.size();
                double CORRELATION_SELECTION_GLOBAL_RATIO=Double.parseDouble(Utilities.readProperty(eu.socialsensor.graphbased.Constants.CORRELATION_SELECTION_GLOBAL_RATIO,eu.socialsensor.graphbased.Constants.configuration.getConfig()));
                float edge_threshold=allSimilarities.get(n_full-(int)(CORRELATION_SELECTION_GLOBAL_RATIO*n_full));
                for(i=0;i<n_terms;i++)
                    for(j=i+1;j<n_terms;j++)
                        if(graphArray[i][j]>edge_threshold)
                            termGraph.addEdge(new MyLink(graphArray[i][j]), terms.get(i), terms.get(j));
            }
            
            if(USED_CORRELATION_SELECTION_TYPE==eu.socialsensor.graphbased.Constants.CORRELATION_SELECTION_TYPES.LOCAL_N){
                for(i=0;i<n_terms;i++){
                    ArrayList<Float> allSimilarities=new ArrayList<Float>();
                    for(j=0;j<n_terms;j++)
                        allSimilarities.add(new Float(graphArray[i][j]));
                    Collections.sort(allSimilarities);
                    int CORRELATION_SELECTION_LOCAL_N=Integer.parseInt(Utilities.readProperty(eu.socialsensor.graphbased.Constants.CORRELATION_SELECTION_LOCAL_N,eu.socialsensor.graphbased.Constants.configuration.getConfig()));
                    float edge_threshold=allSimilarities.get(n_terms-CORRELATION_SELECTION_LOCAL_N);
                     
                    for(j=0;j<n_terms;j++)
                        if(graphArray[i][j]>edge_threshold)
                            termGraph.addEdge(new MyLink(graphArray[i][j]), terms.get(i), terms.get(j));
                }
            }
            
            if(USED_CORRELATION_SELECTION_TYPE==eu.socialsensor.graphbased.Constants.CORRELATION_SELECTION_TYPES.LOCAL_PERCENTAGE){
                for(i=0;i<n_terms;i++){
                    ArrayList<Float> allSimilarities=new ArrayList<Float>();
                    for(j=0;j<n_terms;j++)
                        allSimilarities.add(new Float(graphArray[i][j]));
                    Collections.sort(allSimilarities);
                    double CORRELATION_SELECTION_LOCAL_RATIO=Double.parseDouble(Utilities.readProperty(eu.socialsensor.graphbased.Constants.CORRELATION_SELECTION_LOCAL_RATIO,eu.socialsensor.graphbased.Constants.configuration.getConfig()));
                    float edge_threshold=allSimilarities.get(n_terms-(int)(CORRELATION_SELECTION_LOCAL_RATIO*n_terms));
                    for(j=i+1;j<n_terms;j++)
                        if(graphArray[i][j]>edge_threshold)
                            termGraph.addEdge(new MyLink(graphArray[i][j]), terms.get(i), terms.get(j));
                }
            }
            
            System.out.println("vertex count: "+termGraph.getVertexCount());
            return termGraph;
    }
        
        
      public List<Dysco> getDyscosGraphBased(Map<String,Item> items_map){
        Graph<TermLikelihood,MyLink> termGraph;
        termGraph=generateGraph();
        
        eu.socialsensor.topicdetection.MainConstants.TERM_SIMILARITY_METHODS USED_TERM_SIMILARITY_METHOD;
        String term_similarity_method_string=Utilities.readProperty(eu.socialsensor.graphbased.Constants.TERM_SIMILARITY_METHOD,eu.socialsensor.graphbased.Constants.configuration.getConfig());
        USED_TERM_SIMILARITY_METHOD=eu.socialsensor.topicdetection.MainConstants.TERM_SIMILARITY_METHODS.valueOf(term_similarity_method_string);

        
        double scan_epsilon=0.7;
        int scan_mu=3;
        
        scan_epsilon=Double.parseDouble(Utilities.readProperty(eu.socialsensor.graphbased.Constants.SCAN_EPSILON,eu.socialsensor.sfim.Constants.configuration.getConfig()));
        scan_mu=Integer.parseInt(Utilities.readProperty(eu.socialsensor.graphbased.Constants.SCAN_MU,eu.socialsensor.sfim.Constants.configuration.getConfig()));
        
        
        
        
        ScanCommunityDetector<TermLikelihood, MyLink> detector =
            new ScanCommunityDetector<TermLikelihood, MyLink>(scan_epsilon,scan_mu);

        ScanCommunityStructure<TermLikelihood, MyLink> structure =detector.getCommunityStructure(termGraph);
        int n_terms=terms.size();
        Integer[] labels=new Integer[n_terms];
        int n_communities=structure.getNumberOfCommunities();
        
        int i,j,k;
        List<Dysco> dyscos=new ArrayList<Dysco>();
        for(i=0;i<n_communities;i++){
              Community<TermLikelihood,MyLink> tmp_comm=structure.getCommunity(i);
              if(tmp_comm!=null){
              int n_members=tmp_comm.getNumberOfMembers();
              Dysco tmp_dysco=new Dysco();
              dyscos.add(tmp_dysco);
              List<Ngram> keywords=tmp_dysco.getNgrams();
              for(j=0;j<n_members;j++){
                    TermLikelihood tmp_word_feat=tmp_comm.getMembers().get(j);
                    int pos=0;
                    while((terms.get(pos)!=tmp_word_feat)&&(pos<n_terms)) pos++;
                   keywords.add(new Ngram(tmp_word_feat.term.name,null));
              }
              }
        }
        
        int n_hubs=structure.getHubs().size();

        double hub_linking_threshold=0.1;
        hub_linking_threshold=Double.parseDouble(Utilities.readProperty(eu.socialsensor.graphbased.Constants.HUB_LINKING_THRESHOLD,eu.socialsensor.sfim.Constants.configuration.getConfig()));
        
        for(i=0;i<n_hubs;i++){
            TermLikelihood tmp_hub=structure.getHubs().get(i);
                    int n_comms=structure.getHubAdjacentCommunities(structure.getHubs().get(i));
                    Collection<TermLikelihood> hub_neighbours= termGraph.getNeighbors(tmp_hub);
                    
                    int n_neighbours=hub_neighbours.size();
                    int n_filled=0;
                    for(TermLikelihood tmp_neighbour:hub_neighbours){
                        double similarity=tmp_neighbour.term.similarity(tmp_hub.term,USED_TERM_SIMILARITY_METHOD);
                        if(similarity>hub_linking_threshold)
                            for(k=0;k<dyscos.size();k++){
                                Dysco tmp_dysco=dyscos.get(k);
                                if((tmp_dysco.getKeywords().contains(tmp_neighbour))&&(!tmp_dysco.getKeywords().contains(tmp_hub))){
                                    int pos=0;
                                    while((terms.get(pos)!=tmp_hub)&&(pos<n_terms)) pos++;
                                    tmp_dysco.getKeywords().add(tmp_hub.term.name);
                                }
                            }
                    }
                }

           return dyscos;
                
    }
        
        
}
