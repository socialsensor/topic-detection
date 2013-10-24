package eu.socialsensor.documentpivot.utils;

import eu.socialsensor.documentpivot.model.Vocabulary;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

public class InitListener implements StatusListener {
	
	private Vocabulary vocabulary;
	
	public InitListener(Vocabulary vocabulary) {
		this.vocabulary = vocabulary;
	}
	@Override
	public void onStatus(Status status) {
		String text = Tokenizer.getTweetText(status, false, false);
		String[] tokens = Tokenizer.tokenize(text);
		vocabulary.update(tokens);
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
