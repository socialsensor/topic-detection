Graph-based
===========

This is a feature-pivot method, i.e. it groups together terms appearing in the corpus according to their cooccurrence patterns. In particular, it implements the following procedure:

    1. Selects the terms that will be clustered.
	2. Organizes the terms in a graph, on which the existence of an edge implies some sort of cooccurrence pattern between the connected terms.
	3. Application of [SCAN](http://www.ualr.edu/nxyuruk/publications/kdd07.pdf), a community detection algorithm.
	
At the end of the process, each community of terms is treated as a topic and a DySCO is created for each of them.
	
There are various parameters that determine how the steps above are performed. These are set in the file graph_parameters.properties, under resources. These are:

For term selection, we compute the likelihood of appearance of each term in the current corpus and in a reference corpus (in the file resources/vocabulary_corpus.txt) and then compute the ratio of these two quantities. The parameter TERM_SELECTION_METHOD determines the strategy that will be used for selection of terms based on the ratio of likelihoods:

    1. The RATIO_THRESHOLD option is selected. In that case, an explicitly defined threshold is used (in the parameter TERM_SELECTION_RATIO_THRESHOLD)
	2. The TOP_N option is selected. In that case, the N terms with the highest ratio are selected. The parameter TERM_SELECTION_TOP_N determines the number N.
	3. The TOP_PERCENTAGE option is selected. In that case, the fraction of terms with the highest ratio as determined by the parameter TERM_SELECTION_TOP_PERCENTAGE is used.
	
In the graph construction step, there are two important sets of parameters. The first, TERM_SIMILARITY_METHOD determines the similarity measure that is used between two terms. It may have the following values:

    1. NO_OF_COOCCURRENCES. Absolute number of cooccurrences.
	2. NO_OF_COOCCURRENCES_REGULARIZED_MIN. Absolute number of cooccurrences divided by the smaller number of occurrences of the two terms.
	3. NO_OF_COOCCURRENCES_REGULARIZED_MAX. Absolute number of cooccurrences divided by the larger number of occurrences of the two terms.
	4. NO_OF_COOCCURRENCES_REGULARIZED_SUM. Absolute number of cooccurrences divided by the sum of occurrences of the two terms.
	5. NO_OF_COOCCURRENCES_REGULARIZED_TIMES. Absolute number of cooccurrences divided by the product of occurrences of the two terms.
	6. JACCARD. The Jaccard similarity between the sets of documents in which the two terms appear.
	7. COSINE. The cosine similarity between the binary vectors that represent the occurrences of the two terms.

Based on the similarity measure, which always expresses some notion of cooccurrence between terms, there are different methods for constructing the graph, as determined by the CORRELATION_SELECTION_TYPE parameter: 

    1. GLOBAL_PERCENTAGE. The percentage of edges determined by the parameter CORRELATION_SELECTION_GLOBAL_RATIO with the highest value in the similarity measure over all possible edges in the graph is used.
	2. GLOBAL_N. The N edges determined by the parameter CORRELATION_SELECTION_GLOBAL_N with the highest value in the similarity measure over all possible edges in the graph is used.
	3. LOCAL_PERCENTAGE. The percentage of edges determined by the parameter CORRELATION_SELECTION_LOCAL_RATIO with the highest value in the similarity measure over all possible edges for each node is used.
	4. LOCAL_N. The N edges determined by the parameter CORRELATION_SELECTION_LOCAL_N with the highest value in the similarity measure over all possible edges for each node is used.
	5. FULL. A fully connected graph.
	6. THRESHOLD. An explicit threshold is used to determine the existence of edges. The threshold is determined by the parameter CORRELATION_THRESHOLD. 
	7. GLOBAL_AVERAGE_DEGREE. The top edges are selected, so that the graph has overall average degree as determined by the parameter CORRELATION_SELECTION_AVERAGE_DEGREE.
	
In the third step, SCAN is applied on the formed graph. SCAN has two parameters mu (SCAN_MU in the parameters file) and epsilon (SCAN_EPSILON in the parameters file). Mu determines the minimum number of nodes a community may have and epsilon determines how tight a produced cluster must be (the higher the value, the more tight the clusters should be). Finally, SCAN also produces a set of hubs, nodes that are linked to more than one community. We utilize a threshold on the number of connections to its adjacent clusters (HUB_LINKING_THRESHOLD) to determine if it should be included as part of that community.