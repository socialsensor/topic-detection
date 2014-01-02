/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.socialsensor.sfim;

import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.documentpivot.vocabulary.Vocabulary;
import eu.socialsensor.documentpivot.vocabulary.VocabularyComparator;
import eu.socialsensor.documentpivot.Utilities;
import eu.socialsensor.framework.common.services.GenericDyscoCreator;
import java.util.List;
import eu.socialsensor.entitiesextractor.EntityDetection;
import eu.socialsensor.framework.client.dao.ItemDAO;
import eu.socialsensor.framework.client.dao.impl.ItemDAOImpl;
import eu.socialsensor.framework.common.domain.dysco.Entity;
import java.util.*;

/**
 *
 * @author gpetkos
 * 
 * This class implements topic detection via soft frequent itemset mining (Aiello et.al. "Sensing trending topics in Twitter").
 * For more details on the parameters of the implementation please see the comments
 * in the configuration file (sfim_parameters.properties), which can be found under resources.
  */
public class DyscoCreator {

    public DyscoCreator() {
        eu.socialsensor.sfim.Constants.configuration=new eu.socialsensor.sfim.Configuration();
    }
    
    
    public List<Dysco> createDyscos(List<Item> items) {
        Map<String,Item> items_map=new HashMap<String,Item>();
        for(int i=0;i<items.size();i++)
            items_map.put(items.get(i).getId(),items.get(i));
        Vocabulary vocabulary_corpus=Vocabulary.getCorpusVocabulary(items.iterator(),true);
        vocabulary_corpus.filterByNoOfOccurrences(2);
        Vocabulary vocabulary_reference=new Vocabulary();
        vocabulary_reference.load(true);
        System.out.println("Read reference vocabulary");
        VocabularyComparator vocabulary_comparator=new VocabularyComparator(vocabulary_corpus,vocabulary_reference);
        vocabulary_comparator.outputOrdered(vocabulary_comparator.vocabulary_new_corpus.directory+vocabulary_comparator.vocabulary_new_corpus.filename_start+".vocabulary_ratios");
        System.out.println("Getting topics");
        List<Dysco> dyscos=vocabulary_comparator.getDyscosBySFIM(items_map);
        return dyscos;
    }
    
    public static void main(String[] args){
        System.out.println("SFIIIIIIIIIIIIIIM");
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
        eu.socialsensor.sfim.DyscoCreator dc=new eu.socialsensor.sfim.DyscoCreator();
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
