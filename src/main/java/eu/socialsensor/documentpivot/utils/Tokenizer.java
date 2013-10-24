package eu.socialsensor.documentpivot.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

public class Tokenizer {
	static private Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
	

	public static String getTweetText(Status status, boolean include_hashtags, boolean include_mentions) {
		
		String text = status.getText();
		if(text == null)
			return text;
		
		if(!include_hashtags){
			HashtagEntity[] hashtags = status.getHashtagEntities();
			for(HashtagEntity hashtag : hashtags) 
				text = text.replace("#"+hashtag.getText(), "");
		}
		
		URLEntity[] urls = status.getURLEntities();
		for(URLEntity url : urls) {
			String urlStr = null;
			if((urlStr =url.getURL())!=null)
				text = text.replace(urlStr.toString(), "");
		}
		
		if(!include_mentions) {
			UserMentionEntity[] mentions = status.getUserMentionEntities();
			for(UserMentionEntity mention : mentions) 
				text = text.replace("@"+mention.getScreenName(), "");

			text = text.replaceFirst("^RT", "");
		}
		
		return text;
	}
	
	public static String[] tokenize(String text) {
            List<String> result = new ArrayList<String>();
            try {
                TokenStream stream  = analyzer.tokenStream("", new StringReader(text));
                while(stream.incrementToken())
                    result.add(stream.getAttribute(CharTermAttribute.class).toString());
            }
            catch(IOException e) { }
            return result.toArray(new String[result.size()]);
            }
	
	public static void main(String[] args) throws IOException{
		String[] tokens = Tokenizer.tokenize("This is a text string. Analyzer should analyze it correctly!");
		for(String token : tokens){
			System.out.println(token);
		}
	}
}
