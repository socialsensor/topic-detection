/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.socialsensor.documentpivot;

//import eu.socialsensor.topicdetection.Globals;
import java.util.Properties;

/**
 *
 * @author gpetkos
 */
public class Utilities {
   public static String readProperty(String propertyName,Properties props){
        String property=props.getProperty(propertyName);
        if(property==null)
            throw new IllegalStateException("Property "+propertyName+" has not been defined");
        return property;
   }

   /*
   public static String readProperty(String propertyName){
        String property=Globals.getConfig().getProperty(propertyName);
        if(property==null)
            throw new IllegalStateException("Property "+propertyName+" has not been defined");
        return property.trim();
   }
   
   */
}
