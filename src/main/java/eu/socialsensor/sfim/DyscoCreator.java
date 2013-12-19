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
import eu.socialsensor.documentpivot.Constants;
import java.util.HashMap;
import java.util.Map;

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
        vocabulary_corpus.filterByNoOfOccurrences(5);
        Vocabulary vocabulary_reference=new Vocabulary();
        vocabulary_reference.load(false,true);
        System.out.println("Read reference vocabulary");
        VocabularyComparator vocabulary_comparator=new VocabularyComparator(vocabulary_corpus,vocabulary_reference);
        vocabulary_comparator.outputOrdered(vocabulary_comparator.vocabulary_new_corpus.directory+vocabulary_comparator.vocabulary_new_corpus.filename_start+".vocabulary_ratios");
        System.out.println("Getting topics");
        List<Dysco> dyscos=vocabulary_comparator.getDyscosBySFIM(items_map);
        return dyscos;
    }
    
}
