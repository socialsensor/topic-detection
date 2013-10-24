package eu.socialsensor.documentpivot.utils;

import eu.socialsensor.documentpivot.LSH.HashTables;
import eu.socialsensor.documentpivot.model.VectorSpace;
import eu.socialsensor.documentpivot.model.Vocabulary;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import eu.socialsensor.documentpivot.utils.Tokenizer;
import twitter4j.StallWarning;

public class LSHListener implements StatusListener {
	
	int c = 0;
	private Vocabulary vocabulary;
	private HashTables hashTables;
	
	public LSHListener(Vocabulary vocabulary, HashTables hashTable) {
		this.vocabulary = vocabulary;
		this.hashTables = hashTables;
	}
	@Override
	public void onStatus(Status status) {
		c++;
		String text = Tokenizer.getTweetText(status, false, false);
		VectorSpace vsm = new VectorSpace(Long.toString(status.getId()), text);
		vocabulary.update(vsm.tokens());
		hashTables.add(vsm);
		
		if(c>10000) {
			VectorSpace[] nn = hashTables.get(vsm);
			System.out.println(nn.length);
		}
    }
	
	@Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
    @Override
	public void onScrubGeo(long arg0, long arg1) {
	}
    @Override
    public void onException(Exception ex) {
        ex.printStackTrace();    
    }

    @Override
    public void onStallWarning(StallWarning sw) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
	
}
