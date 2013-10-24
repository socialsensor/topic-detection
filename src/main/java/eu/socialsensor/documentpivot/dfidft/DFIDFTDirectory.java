/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.socialsensor.documentpivot.dfidft;

import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.dysco.Ngram;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author iti
 */
public class DFIDFTDirectory {
    Map<String,Double> dfidftScores;
    static String separator=";;;";
    static String singleSeparator=";";

    public DFIDFTDirectory() {
        dfidftScores=new HashMap<String,Double>();        
    }
    
    public void load(String filename){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filename), "UTF8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                line=line.trim();
                String[] parts=line.split(separator);
                parts[1]=parts[1].replaceAll(singleSeparator, "");
                double dfidft_value=Double.parseDouble(parts[1]);
                dfidftScores.put(parts[0].trim(), dfidft_value);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }        
        
    }
    
    public void computeDyscoScore(Dysco topic){
        double score=0;
        List<Ngram> keywords=topic.getNgrams();
        for(int i=0;i<keywords.size();i++){
            Ngram tmp_string=keywords.get(i);
            Double tmp_score=dfidftScores.get(tmp_string);
            if(tmp_score!=null){
                score=score+tmp_score;
            }
        }        
        topic.setScore((float) score/(keywords.size()));
    }
    
}
