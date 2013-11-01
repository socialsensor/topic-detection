/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.socialsensor.topicdetection;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gpetkos
 */
public class MainConfiguration extends BasicConfiguration{
    
    public Properties getConfig(){
        if(config==null){
            ClassLoader loader=ClassLoader.getSystemClassLoader ();
            InputStream in = null;
            in = loader.getResourceAsStream ("main_parameters.properties");
            if (in != null)
            {
                config = new Properties ();
                try {
                    config.load (in); // Can throw IOException
                } catch (IOException ex) {
                    Logger.getLogger(MainConfiguration.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }        
        return config;
    }
    
    
}
