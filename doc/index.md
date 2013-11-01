topic-detection
===============

This project includes Java implementations for a set of methods for automatically detecting trending topics in streams of short texts (e.g. tweets). Produced topics come in the form of Dynamic Social Containers (DySCos). There are currently four topic detection methods implemented, for more details on each of them, please see the relevant section.

    1. [LDA](lda.md)
    2. [Document-pivot](docp.md)
    3. [Graph-based feature-pivot](graph.md)
    4. [Soft frequent itemset mining](sfim.md)
	
Each of them has its own package which along other classes, contains a class named DyscoCreator. The DyscoCreator class has a method with the following signature: 

> public List<Dysco> createDyscos(List<Item> items)

In order to use any of these algorithms one has to import the DyscoCreator class from the package that contains the implementation of the algorithm and call the createDyscos method using as input a list of Items. An Item is an object that represents a single document. In the context of the overall SocialSensor code, one could retrieve such a list of items from the database to which the SocialSensor stream manager stores collected items using code like the following:

> ItemDAO itemdao=new ItemDAOImpl();
> List<Item> items=itemdao.getLatestItems(1000);

Alternatively, one can create a list of items with custom code, that is, create some new Item objects and set their title to some text. Also, for testing reasons, we provide the Tester class in the eu.socialsensor.topicdetection package. This class, contains the method

> List<Item> loadItemsFromFile(String filename)

which loads a list of Items from a file that contains json Strings that represent tweets in the form returned by the Twitter API. The main function in the Tester class presents a simple example of using the provided code. It should be noted that there is a number of parameters for each topic detection algorithm and for the test main function. All of these parameters are set in the respective properties file in the resources folder. The parameters for each of the algorithms are listed in the documentation files listed above. The parameters for the test main class (in the file main_parameters.properties) simply determine which of the four algorithms will be executed and where the file with the json records is located.

Finally, it is important to note that some of the algorithms utilize a vocabulary of terms in order to be able to compute tf-idf vectors and appearance likelihoods. A vocabulary is provided with this package and can be found in the file vocabulary_corpus.txt in the resources folder. It has been generated from a random collection of tweets. Of course, one can replace this with a different vocabulary.