/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.socialsensor.graphbased.clust;
 
/**
 *
 * @author gpetkos
 */
public class MyLink {
    public float weight;
    public String id;
    public static int counter;
    
    public MyLink() {
    }

    public MyLink(float weight) {
        this.weight = weight;
        counter=counter+1;
        id=counter+"";
    }
    
    
}
