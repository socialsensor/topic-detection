LDA
===

The provided implementation for Latent Dirichlet Allocation (LDA) is actually a wrapper around the mallet implementation. LDA is the most common probabilistic topic model. For more details for LDA please see this [paper](http://jmlr.org/papers/v3/blei03a.html). 

There is a small number of parameters for this wrapper. These can be set in the file lda_parameters.properties , under resources.

    * The number of topics that will be returned by LDA (NO_OF_TOPICS)
	* The number of iterations for which LDA will be trained (NO_OF_ITERATIONS)
	* The number of keywords that will be returned (NO_OF_KEYWORDS)
    
