/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.socialsensor.documentpivot;

import eu.socialsensor.topicdetection.BasicConfiguration;

/**
 *
 * @author gpetkos
 */
public class Constants {
    public static String SIMILARITY_THRESHOLD="SIMILARITY_THRESHOLD";
    public static enum TERM_SIMILARITY_METHODS {NO_OF_COOCCURRENCES,NO_OF_COOCCURRENCES_REGULARIZED_MIN,NO_OF_COOCCURRENCES_REGULARIZED_MAX,NO_OF_COOCCURRENCES_REGULARIZED_SUM,NO_OF_COOCCURRENCES_REGULARIZED_TIMES,JACCARD,COSINE};
    public static enum TERM_SELECTION_TYPES {RATIO_THRESHOLD,TOP_N,TOP_PERCENTAGE};
    public static BasicConfiguration configuration;
}
