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
package eu.socialsensor.topicdetection;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
//import eu.socialsensor.documentpivotentityandhashtagbased.DyscoCreator;
//import eu.socialsensor.documentpivot.DyscoCreator;
//import eu.socialsensor.sfim.DyscoCreator;
//import eu.socialsensor.graphbased.DyscoCreator;
import eu.socialsensor.documentpivot.Utilities;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author gpetkos
 */
public class Tester {
    public static void main(String[] args){
        BasicConfiguration mainConfig=new MainConfiguration();
        mainConfig.getConfig();
        MainConstants.TOPIC_DETECTIONS_METHODS usedTopicDetectionMethod=MainConstants.TOPIC_DETECTIONS_METHODS.valueOf(Utilities.readProperty(MainConstants.TOPIC_DETECTION_METHOD,mainConfig.getConfig()));

        System.out.println("Chosen topic detection methods is : "+usedTopicDetectionMethod);
       
        String directory=Utilities.readProperty(MainConstants.TWEETS_DIRECTORY,mainConfig.getConfig());
        String filename=Utilities.readProperty(MainConstants.TWEETS_FILE,mainConfig.getConfig());

        List<Item> items=loadItemsFromFile(directory+filename);
        List<Dysco> dyscos=new ArrayList<Dysco>();

        switch(usedTopicDetectionMethod){
            case DOC_PIVOT: {
                System.out.println("Getting DySCOs using document pivot");
                eu.socialsensor.documentpivot.DyscoCreator dyscoCreatorDocP=new eu.socialsensor.documentpivot.DyscoCreator();
                dyscos=dyscoCreatorDocP.createDyscos(items);
                break;
            }
            case LDA: {
                System.out.println("Getting DySCOs using LDA");
                eu.socialsensor.lda.DyscoCreator dyscoCreatorLDA=new eu.socialsensor.lda.DyscoCreator();
                dyscos=dyscoCreatorLDA.createDyscos(items);
                break;
            }
            case GRAPH_BASED: {
                System.out.println("Getting DySCOs using graph based");
                eu.socialsensor.graphbased.DyscoCreator dyscoCreatorGraph=new eu.socialsensor.graphbased.DyscoCreator();
                dyscos=dyscoCreatorGraph.createDyscos(items);
                break;
            }
            case SOFT_FIM: {
                System.out.println("Getting DySCOs using SFIM");
                eu.socialsensor.sfim.DyscoCreator dyscoCreatorSFIM=new eu.socialsensor.sfim.DyscoCreator();
                dyscos=dyscoCreatorSFIM.createDyscos(items);
                break;
            }
        }
        
        
        System.out.println("");
        System.out.println("---- Results ----");
        System.out.println("No of Dyscos: "+dyscos.size());
        System.out.println("");
        for(Dysco next_dysco:dyscos){
            List<Item> dysco_items=next_dysco.getItems();
            Map<String,Double> dysco_keywords=next_dysco.getKeywords();
            System.out.println("===========");
            System.out.println("Keywords:");
            for(Entry<String,Double> next_keyword:dysco_keywords.entrySet())
                System.out.print(next_keyword.getKey()+" ("+next_keyword.getValue() +") ");
            System.out.println("");
            System.out.println("Items:");
            for(Item next_item:dysco_items)
                System.out.println("- "+next_item.getTitle());
            
        }
        
    }
    /*

    
    public static void main(String[] args){
        try {
            ItemDAO itemdao=new ItemDAOImpl("social1.atc.gr");
        } catch (Exception ex) {
            Logger.getLogger(Tester.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Getting items from file");
        
        String filenameDocs="C:\\Documents and Settings\\gpetkos\\Desktop\\imc_descriptions_list_reformated.txt";
        List<Item> items=loadItemsFromFilePlain(filenameDocs);
        
//        String filenameResults = "D:\\Topics\\document_pivot_0.0001.txt";
//        String filenameResults = "D:\\Topics\\graph_based.txt";
        String filenameResults = "D:\\Topics\\lda.txt";
        //List<Item> items=itemdao.getLatestItems(1000);
        
        System.out.println("Finding entities");
        EntityDetection ent=new EntityDetection();
        ent.addEntitiesToItems(items);
        System.out.println("Getting dyscos");
        DyscoCreator dc=new DyscoCreator();
        List<Dysco> dyscos=dc.createDyscos(items);
        
        System.out.println("");
        System.out.println("---- Results ----");
        System.out.println("No of Dyscos: "+dyscos.size());
        System.out.println("");
        try{
            BufferedWriter bw=new BufferedWriter(new FileWriter(filenameResults));
            bw.append("No of topics: "+dyscos.size());
            bw.newLine();
            bw.append("============");
            bw.newLine();
            for(Dysco next_dysco:dyscos){
                next_dysco.setTitle(TrendsLabeler.findPopularTitle(next_dysco));
                KeywordExtractor.extractKeywordsCERTH(next_dysco);
                List<Item> dysco_items=next_dysco.getItems();
                Map<String,Double> dysco_keywords=next_dysco.getKeywords();
                System.out.println("===========");
  //              System.out.println("Title:");
    //            System.out.println(next_dysco.getTitle());
                System.out.println("Keywords:");
                for(Entry<String,Double> next_keyword:dysco_keywords.entrySet())
    //                System.out.print(next_keyword.getKey()+" ("+next_keyword.getValue() +") ");
                    System.out.print(next_keyword.getKey()+" ");
                System.out.println("");
                System.out.println("Items:");
                for(Item next_item:dysco_items)
                    System.out.println(next_item.getId()+"- "+next_item.getTitle());

                bw.append("===========");
                bw.newLine();
                bw.append("Title:");
                bw.newLine();
                bw.append(next_dysco.getTitle());
                bw.newLine();
                bw.append("Keywords:");
                bw.newLine();
                for(Entry<String,Double> next_keyword:dysco_keywords.entrySet())
    //                System.out.print(next_keyword.getKey()+" ("+next_keyword.getValue() +") ");
                    bw.append(next_keyword.getKey()+" ");
                bw.newLine();
                bw.append("Items:");
                bw.newLine();
                for(Item next_item:dysco_items){
                    bw.append(next_item.getId()+"- "+next_item.getTitle());
                    bw.newLine();
                }
                
                
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
   */
    
    public static List<Item> loadItemsFromFile(String filename){
        List<Item> items=new ArrayList<Item>();
        try{
            BufferedReader br=new BufferedReader(new FileReader(filename));
            String line=null;
            while((line=br.readLine())!=null){
                if(line.trim()!=""){
                    Item new_item=new Item();
                    DBObject dbObject = (DBObject) JSON.parse(line);
                    String id=(String) dbObject.get("id_str");
                    new_item.setId(id);
                    String text=(String) dbObject.get("text");
                    new_item.setTitle(text);
                    DBObject tmp_obj=(DBObject) dbObject.get("user");
                    String uploader=(String) tmp_obj.get("screen_name");
                    new_item.setAuthorScreenName(uploader);
                    items.add(new_item);
                }
            }
            br.close();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return items;
    }

    public static List<Item> loadItemsFromFilePlain(String filename){
        List<Item> items=new ArrayList<Item>();
        try{
            BufferedReader br=new BufferedReader(new FileReader(filename));
            String line=null;
            int i=0;
            while((line=br.readLine())!=null){
                i=i+1;
                if(line.trim()!=""){
                    Item new_item=new Item();
                    String[] parts=line.split("%%%");
                    new_item.setId(parts[0]);
                    new_item.setTitle(parts[1].replaceAll("\"",""));
                    items.add(new_item);
                }
            }
            br.close();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return items;
    }
    
    
    public static Long parseTwitterDate(String dateStr) {
        // parse Twitter date
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                        "EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
        dateFormat.setLenient(false);
        Long created = null;
        try {
                created = dateFormat.parse(dateStr).getTime();
                return created;
        } catch (Exception e) {
                return null;
        }
    }
    
    
}
