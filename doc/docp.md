Document-pivot
==============

A document pivot method clusters together documents and treats each cluster as a distinct topic. In this implementation we utilize the approach presented in this [paper](http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.170.9438&rep=rep1&type=pdf). In particular, an incoming document is assigned to the same cluster as its best match as long as its similarity to it is above some threshold. Otherwise, a new cluster is created with the document being the single member of the cluster. Documents are compared using a tf-idf representation and cosine similarity. In order to rapidly find the nearest neighbours of an incoming document, a Locality Sensitive Hashing (LSH) scheme is utilized. 

There is a single parameter for this topic detection approach, that is set in the file doc_pivot_parameters.properties, under resources, and which determines the threshold that is used by the incremental clustering procedure.   
