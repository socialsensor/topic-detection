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
    public static String MIN_NO_OF_DOCUMENTS_PER_CLUSTER="MIN_NO_OF_DOCUMENTS_PER_CLUSTER";
    public static String FILTER_HASHTAGS = "FILTER_HASHTAGS";
    public static String FILTER_USER_MENTIONS = "FILTER_USER_MENTIONS";
    public static String FILTER_URLS = "FILTER_URLS";
    public static String BOOST_ENTITIES="BOOST_ENTITIES";
    public static String BOOST_ENTITIES_FACTOR="BOOST_ENTITIES_FACTOR";
    public static String BOOST_HASHTAGS="BOOST_HASHTAGS";
    public static String BOOST_HASHTAGS_FACTOR="BOOST_HASHTAGS_FACTOR";
    public static String PERFORM_FULL_INDEXING="PERFORM_FULL_INDEXING";
    public static enum TERM_SIMILARITY_METHODS {NO_OF_COOCCURRENCES,NO_OF_COOCCURRENCES_REGULARIZED_MIN,NO_OF_COOCCURRENCES_REGULARIZED_MAX,NO_OF_COOCCURRENCES_REGULARIZED_SUM,NO_OF_COOCCURRENCES_REGULARIZED_TIMES,JACCARD,COSINE};
    public static enum TERM_SELECTION_TYPES {RATIO_THRESHOLD,TOP_N,TOP_PERCENTAGE};
    public static BasicConfiguration configuration;
}
