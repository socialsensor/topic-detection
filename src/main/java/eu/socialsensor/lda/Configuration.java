/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.socialsensor.lda;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import eu.socialsensor.topicdetection.BasicConfiguration;

/**
 *
 * @author gpetkos
 * 
 * This class returns the configuration for the LDA DySCO creator.
 * The parameters of the execution are loaded from the resource file "lda_parameters.properties"
 */
public class Configuration extends BasicConfiguration {
    
    public Properties getConfig(){
        if(config==null){
            ClassLoader loader=ClassLoader.getSystemClassLoader ();
            InputStream in = null;
//            try {
                            in = loader.getResourceAsStream ("lda_parameters.properties");
//                            in = new FileInputStream("lda_parameters.properties");
//            } catch (FileNotFoundException ex) {
//                Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
  //          }
            if (in != null)
            {
                config = new Properties ();
                try {
                    config.load (in); // Can throw IOException
                } catch (IOException ex) {
                    Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
                }
            }        
        }        
        return config;
    }
    
    
}
