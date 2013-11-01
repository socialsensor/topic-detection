/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.socialsensor.topicdetection;

/**
 *
 * @author gpetkos
 */
public class MainConstants {
    public static String TOPIC_DETECTION_METHOD="TOPIC_DETECTION_METHOD";
    public static String TWEETS_DIRECTORY="TWEETS_DIRECTORY";
    public static String TWEETS_FILE="TWEETS_FILE";
    public static String TEXT_BASED_DYSCO_CREATION_METHOD="TEXT_BASED_DYSCO_CREATION_METHOD";

    public static String TERM_SIMILARITY_METHOD="TERM_SIMILARITY_METHOD";
    
    public static enum TOPIC_DETECTIONS_METHODS {LDA,DOC_PIVOT,GRAPH_BASED,SOFT_FIM};
    public static enum TERM_SIMILARITY_METHODS {NO_OF_COOCCURRENCES,NO_OF_COOCCURRENCES_REGULARIZED_MIN,NO_OF_COOCCURRENCES_REGULARIZED_MAX,NO_OF_COOCCURRENCES_REGULARIZED_SUM,NO_OF_COOCCURRENCES_REGULARIZED_TIMES,JACCARD,COSINE};

}
