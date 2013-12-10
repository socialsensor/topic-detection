package eu.socialsensor.trendslabeler;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.logging.Level;
import org.apache.log4j.Logger;

public class URLDeshortener {

    private static DefaultHttpClient client;

    // fixtures

//    @Before
//    public final void before() {
    static{
        final HttpParams httpParameters = new BasicHttpParams();
        httpParameters.setParameter("http.protocol.handle-redirects", false);
        client = new DefaultHttpClient(httpParameters);
    }

    // API
    public static String expand(final String urlArg) throws IOException {
        String originalUrl = urlArg;
        String newUrl = expandSingleLevel(originalUrl);
        while (!originalUrl.equals(newUrl)) {
            originalUrl = newUrl;
            newUrl = expandSingleLevel(originalUrl);
        }

        return newUrl;
    }

    public static String expandFast(String url_str){
        String expandedURL=url_str;
        HttpURLConnection connection=null;
        try{
            URL url=new URL(url_str);
            connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            Logger.getRootLogger().info("Title extractor  (case 1) : connecting ("+url_str+")");
            connection.connect();
            expandedURL = connection.getHeaderField("Location");
            Logger.getRootLogger().info("Title extractor  (case 1) : closing connection");
//            connection.getInputStream().close(); 
        }
        catch(Exception ex){
            Logger.getRootLogger().info("URL expander could not expand the following URL: "+url_str);
        }
        finally{
            if(connection!=null){
                try {
                    connection.getInputStream().close();
                } catch (IOException ex) {
                    Logger.getRootLogger().info("Could not close connection object...");
                }
                
            }
        }
        return expandedURL;
    }   
    
    
    final static String expandSafe(final String urlArg) throws IOException {
        String originalUrl = urlArg;
        String newUrl = expandSingleLevelSafe(originalUrl).getRight();
        final List<String> alreadyVisited = Lists.newArrayList(originalUrl, newUrl);
        while (!originalUrl.equals(newUrl)) {
            originalUrl = newUrl;
            final Pair<Integer, String> statusAndUrl = expandSingleLevelSafe(originalUrl);
            newUrl = statusAndUrl.getRight();
            final boolean isRedirect = statusAndUrl.getLeft() == 301 || statusAndUrl.getLeft() == 302;
            if (isRedirect && alreadyVisited.contains(newUrl)) {
                throw new IllegalStateException("Likely a redirect loop");
            }
            alreadyVisited.add(newUrl);
        }

        return newUrl;
    }

    final static Pair<Integer, String> expandSingleLevelSafe(final String url) throws IOException {
        HttpGet request = null;
        HttpEntity httpEntity = null;
        InputStream entityContentStream = null;

        try {
            request = new HttpGet(url);
            final HttpResponse httpResponse = client.execute(request);

            httpEntity = httpResponse.getEntity();
            entityContentStream = httpEntity.getContent();

            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != 301 && statusCode != 302) {
                return new ImmutablePair<Integer, String>(statusCode, url);
            }
            final Header[] headers = httpResponse.getHeaders(HttpHeaders.LOCATION);
            Preconditions.checkState(headers.length == 1);
            final String newUrl = headers[0].getValue();

            return new ImmutablePair<Integer, String>(statusCode, newUrl);
        } catch (final IllegalArgumentException uriEx) {
            return new ImmutablePair<Integer, String>(500, url);
        } finally {
            if (request != null) {
                request.releaseConnection();
            }
            if (entityContentStream != null) {
                entityContentStream.close();
            }
            if (httpEntity != null) {
                EntityUtils.consume(httpEntity);
            }
        }
    }

    final static String expandSingleLevel(final String url) throws IOException {
        HttpGet request = null;
        HttpEntity httpEntity = null;
        InputStream entityContentStream = null;

        try {
            request = new HttpGet(url);
            if(request==null) System.out.println("request is null");
            final HttpResponse httpResponse = client.execute(request);

            httpEntity = httpResponse.getEntity();
            entityContentStream = httpEntity.getContent();

            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != 301 && statusCode != 302) {
                return url;
            }
            final Header[] headers = httpResponse.getHeaders(HttpHeaders.LOCATION);
            Preconditions.checkState(headers.length == 1);
            final String newUrl = headers[0].getValue();

            return newUrl;
        } catch (final IllegalArgumentException uriEx) {
            return url;
        } finally {
            if (request != null) {
                request.releaseConnection();
            }
            if (entityContentStream != null) {
                entityContentStream.close();
            }
            if (httpEntity != null) {
                EntityUtils.consume(httpEntity);
            }
        }
    }

}