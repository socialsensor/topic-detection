/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.socialsensor.documentpivot.preprocessing;


import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.NamedEntity;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

/**
 *
 * @author gpetkos
 */
public class TweetPreprocessor {
    private static StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);

    private static final Pattern urlPattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);  

    private static final Pattern hashtagPattern = 
    Pattern.compile("(?:^|\\s|[\\p{Punct}&&[^/]])(#[\\p{L}0-9-_]+)");    

    private static final Pattern usermentionPattern = 
    Pattern.compile("(?:^|\\s|[\\p{Punct}&&[^/]])(@[\\p{L}0-9-_]+)");    
    
    public static List<String> Tokenize(Item item,boolean include_user_mentions,boolean include_hashtags){
        List<String> tokensList = new ArrayList<String>();
        try {
//            String tmp_content=item.getText();
            String tmp_content=item.getTitle();
            if(tmp_content==null) tmp_content="";
//            List<String> urls=getURLs(tmp_content);
            List<String> urls=getURLsFromItem(item);
            for(String url : urls) {
                tmp_content=tmp_content.replace(url, "");
            }

            if(!include_hashtags){
                List<String> hashtags=getHashtags(tmp_content);
                for(String hashtag : hashtags) 
                    tmp_content = tmp_content.replace(hashtag, "");
            }

            if(!include_user_mentions){
                List<String> usermentions=getUsermentions(tmp_content);
                for(String usermention : usermentions) 
                    tmp_content = tmp_content.replace(usermention, "");
            }
            
            if(tmp_content==null) tmp_content="";
            TokenStream stream  = analyzer.tokenStream(null, new StringReader(tmp_content));
            stream.reset();
            while (stream.incrementToken()) {
                tokensList.add(stream.getAttribute(CharTermAttribute.class).toString());
            }
/*          
            System.out.println("Tweet :"+item.getTitle());
            System.out.print("Tokens: ");
            for(String tmp_tkn:tokensList){
                System.out.print(tmp_tkn+" ");
            }
            System.out.println("");
            
            */
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tokensList;
    }

    public static List<String> TokenizeForSentimentAnalysis(Item item){
        return null;
    }
    
    public static List<NamedEntity> ExtractNamedEntities(Item item){
        return null;
        //Need to include some code for the Stanford named entity extractor.
    }

    public static List<URL> ExtractURLs(Item item){
        return null;
    }

    public static List<String> getURLs(String originalString){
        List<String> urlsSet=new ArrayList<String>();
        Matcher matcher = urlPattern.matcher(originalString);
        while (matcher.find()) {
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end();
            String tmpUrl=originalString.substring(matchStart,matchEnd);
            urlsSet.add(tmpUrl);
            // now you have the offsets of a URL match
            originalString=originalString.replace(tmpUrl,"");
            matcher = urlPattern.matcher(originalString);
        }
        return urlsSet;
    }

    public static List<String> getURLsFromItem(Item item){
        List<String> urlsSet=new ArrayList<String>();
        URL[] urls=item.getLinks();
        for(int i=0;i<urls.length;i++)
            urlsSet.add(urls[i].toString());
        return urlsSet;
    }
    
    
    public static List<String> getHashtags(String originalString){
        List<String> hashtagSet=new ArrayList<String>();
        Matcher matcher = hashtagPattern.matcher(originalString);
        while (matcher.find()) {
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end();
            String tmpHashtag=originalString.substring(matchStart,matchEnd);
            hashtagSet.add(tmpHashtag);
            System.out.println("++ "+tmpHashtag);
            originalString=originalString.replace(tmpHashtag,"");
            System.out.println("-- "+originalString);
            matcher = hashtagPattern.matcher(originalString);
        }
        return hashtagSet;
    }
    
    public static List<String> getUsermentions(String originalString){
        List<String> usermentionsSet=new ArrayList<String>();
        Matcher matcher = usermentionPattern.matcher(originalString);
        while (matcher.find()) {
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end();
            String tmpUsermention=originalString.substring(matchStart,matchEnd);
            usermentionsSet.add(tmpUsermention);
            System.out.println("++ "+tmpUsermention);
            originalString=originalString.replace(tmpUsermention,"");
            System.out.println("-- "+originalString);
            matcher = usermentionPattern.matcher(originalString);
        }
        return usermentionsSet;
    }
    
    
    
}

