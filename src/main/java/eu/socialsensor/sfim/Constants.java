/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.socialsensor.sfim;

import eu.socialsensor.topicdetection.BasicConfiguration;
import eu.socialsensor.documentpivot.*;

/**
 *
 * @author gpetkos
 */
public class Constants {
    public static String SELECT_ALL_HASHTAGS = "SELECT_ALL_HASHTAGS";
    public static String SELECT_ALL_ENTITIES = "SELECT_ALL_ENTITIES";
    public static String TERM_SELECTION_METHOD="TERM_SELECTION_METHOD";
    public static String TERM_SELECTION_RATIO_THRESHOLD="TERM_SELECTION_RATIO_THRESHOLD";
    public static String TERM_SELECTION_TOP_N="TERM_SELECTION_TOP_N";
    public static String TERM_SELECTION_TOP_PERCENTAGE="TERM_SELECTION_TOP_PERCENTAGE";
    public static String MIN_TOPIC_SIZE="MIN_TOPIC_SIZE";
    public static String B_SIGMOID="B_SIGMOID";
    public static String C_SIGMOID="C_SIGMOID";
    public static enum TERM_SELECTION_TYPES {RATIO_THRESHOLD,TOP_N,TOP_PERCENTAGE};

    public static eu.socialsensor.topicdetection.BasicConfiguration configuration;
    
}
