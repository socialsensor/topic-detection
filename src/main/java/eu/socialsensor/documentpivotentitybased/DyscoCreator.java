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
package eu.socialsensor.documentpivotentitybased;

import eu.socialsensor.framework.client.dao.DyscoDAO;
import eu.socialsensor.framework.client.dao.impl.DyscoDAOImpl;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.dysco.Entity;
import eu.socialsensor.keywordextractor.KeywordExtractor;
import java.util.*;
import java.util.Map.Entry;

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
        Map<Set<String>,List<Item>> entitiesSets=new HashMap<Set<String>,List<Item>>();
        for(Item item:items){
            List<Entity> item_entities=item.getEntities();
            Set<String> entities_set=new HashSet<String>();
            for(Entity entity:item_entities){
                String[] ent_parts=entity.getName().trim().split("\\s+");
                for(int i=0;i<ent_parts.length;i++)
                    entities_set.add(ent_parts[i]);
            }
            List<Item> items_list=entitiesSets.get(entities_set);
            if(items_list==null){
                items_list=new ArrayList<Item>();
                items_list.add(item);
                entitiesSets.put(entities_set, items_list);
            }
            else
                items_list.add(item);
            
        }
        List<Dysco> dyscos=new ArrayList<Dysco>();
        for(Entry<Set<String>,List<Item>> next_entry:entitiesSets.entrySet()){
            Dysco newDysco=new Dysco();
            newDysco.setItems(next_entry.getValue());
            dyscos.add(newDysco);
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
        }
        System.out.println("No of dyscos: "+dyscos.size());
        return dyscos;
    }
    
    
}
