/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.socialsensor.documentpivot.termlikelihood;

import org.apache.commons.lang.StringEscapeUtils;
import eu.socialsensor.documentpivot.termfeature.TermFeature;

/**
 *
 * @author gpetkos
 */
public class TermLikelihood {
    public TermFeature term;
    public double likelihood_ratio;

    public TermLikelihood(TermLikelihood tl){
        term=new TermFeature(tl.term);
        likelihood_ratio=tl.likelihood_ratio;
    }
    
    public TermLikelihood(TermFeature term, double likelihood_ratio) {
        this.term = term;
        this.likelihood_ratio = likelihood_ratio;
    }

    public TermLikelihood(String name) {
        this.term=new TermFeature(name);
        this.likelihood_ratio = 0;
    }
    
    
    public String toString() { 
        return StringEscapeUtils.escapeXml(term.name);       
//        return StringEscapeUtils.escapeHtml(term.name);       
    }        

    public double length(){
        return term.length();
    }
    
}
