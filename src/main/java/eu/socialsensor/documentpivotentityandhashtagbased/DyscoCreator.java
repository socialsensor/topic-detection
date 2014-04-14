/*
 * Copyright 2013 gpetkos.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.socialsensor.documentpivotentityandhashtagbased;

import eu.socialsensor.entitiesextractor.EntityDetection;
import eu.socialsensor.framework.client.dao.ItemDAO;
import eu.socialsensor.framework.client.dao.impl.ItemDAOImpl;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.dysco.Entity;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gpetkos
 */
public class DyscoCreator {

    public DyscoCreator() {
        
    }
    
    
    
    /*
     * Takes as input a list of items and a similarity threshold and returns a
     * list of DySCOs
     */
    public List<Dysco> createDyscos(List<Item> items) {
        System.out.println("Entities and hashtags");
        Map<Set<String>,List<Item>> entitiesSets=new HashMap<Set<String>,List<Item>>();
        for(Item item:items){
            Set<String> entities_tags_set=new HashSet<String>();
            List<Entity> item_entities=item.getEntities();
            for(Entity entity:item_entities){
                String[] ent_parts=entity.getName().trim().split("\\s+");
                for(int i=0;i<ent_parts.length;i++)
                    entities_tags_set.add(ent_parts[i]);
            }

            String[] item_tags=item.getTags();
            if(item_tags!=null)
                for(int i=0;i<item_tags.length;i++){
                    entities_tags_set.add(item_tags[i]);
                }
            
            List<Item> items_list=entitiesSets.get(entities_tags_set);
            if(items_list==null){
                items_list=new ArrayList<Item>();
                items_list.add(item);
                entitiesSets.put(entities_tags_set, items_list);
            }
            else
                items_list.add(item);
            
        }
        List<Dysco> dyscos=new ArrayList<Dysco>();
        List<Item> emptySet=null;
        for(Entry<Set<String>,List<Item>> next_entry:entitiesSets.entrySet()){
            if(next_entry.getKey().size()==0){
                emptySet=next_entry.getValue();
                continue;
            }
            Dysco newDysco=new Dysco();
            newDysco.setItems(next_entry.getValue());
            Map<String,Double> keywords=new HashMap<String,Double>();
            for(String keyword:next_entry.getKey())
                keywords.put(keyword, 1.0);
            newDysco.setKeywords(keywords);
            dyscos.add(newDysco);
            /*
            System.out.println("++++++++++++++++++++++++++++++++++++++++");
            System.out.println("--------------Entities------------------");
            Set<String> ents=next_entry.getKey();
            for(String ent:ents)
                System.out.println(ent);
            System.out.println("--------------Keywords------------------");
            KeywordExtractor.extractKeywordsCERTH(newDysco);
            Map<String,Double> keywords=newDysco.getKeywords();
            for(Entry<String,Double> tmp_keyw:keywords.entrySet())
                System.out.println(tmp_keyw.getKey()+" ("+tmp_keyw.getValue()+") ");
            System.out.println("--------------Items------------------");
            List<Item> its=next_entry.getValue();
            for(Item it:its)
                System.out.println(it.getTitle());
            */
        }
        eu.socialsensor.documentpivot.DyscoCreator dpdc=new eu.socialsensor.documentpivot.DyscoCreator();
        List<Dysco> dyscosFromEmptySet=dpdc.createDyscos(emptySet);
        dyscos.addAll(dyscosFromEmptySet);
//        System.out.println("No of dyscos: "+dyscos.size());
        return dyscos;
    }
    
    public static void main(String[] args){
        ItemDAO itemdao=null;
        try {
            itemdao = new ItemDAOImpl("social1.atc.gr");
        } catch (Exception ex) {
            Logger.getLogger(DyscoCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            System.out.println("------ Items ------");
            List<Item> tmp_items=dysco.getItems();
            count=count+tmp_items.size();
            for(Item item:tmp_items)
                System.out.println(item.getTitle());
            System.out.println("------ Keywords ------");
            Map<String,Double> keywords=dysco.getKeywords();
            for(String keyword:keywords.keySet())
                System.out.println(keyword);
        }
        System.out.println("No of retrieved items: "+items.size());
        System.out.println("No of grouped items : "+count);
        System.out.println("No of dyscos : "+dyscos.size());
    }

    
    
}
