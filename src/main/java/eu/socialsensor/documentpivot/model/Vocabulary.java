package eu.socialsensor.documentpivot.model;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.documentpivot.preprocessing.TweetPreprocessor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;


public class Vocabulary {

	static Vocabulary vocabulary = null;
	
	private Vocabulary() {
		voc = new HashMap<String, TermInfo>();
	}
	
	public static Vocabulary getInstance() {
		if(vocabulary==null) {
			vocabulary = new Vocabulary();
		}
		return vocabulary;
	}
	
	private long documents = 0;
	private Map<String, TermInfo> voc;
	
	public void addTerm(String term) {
		if(voc.containsKey(term)) {
			voc.get(term).inc();
		}
		else {
			voc.put(term, new TermInfo(voc.size(), 1L));
		}
	}
	
	public double idf(String term) {
		TermInfo info = voc.get(term);
		if(info==null)
			return 0;
		return Math.log10(((double)documents) / ((double)info.df)); 
	}
	
	public int index(String term) {
		TermInfo info = voc.get(term);
		if(info==null)
			return -1;
		return info.index;
	}
	
	public void update(String terms[]) {
		documents++;
		for(String term : terms) {
			addTerm(term);
		}
	}

	public void update(List<String> terms) {
		documents++;
		for(String term : terms) {
			addTerm(term);
		}
	}
        
        
	public long documents(){
		return documents;
	}
	
	private static class TermInfo {
		
		public int index;
		public long df;
		
		public TermInfo(int index, long df) {
			this.index = index;
			this.df = df;
		}
		
		public void inc() {
			df++;
		}
	}
	
	public int size() {
		return voc.size();
	}
	
	public void writeToFile(String outputFile) throws IOException {
		BufferedWriter outputFileWriter = new BufferedWriter( new OutputStreamWriter( 
				new FileOutputStream(outputFile), "UTF8"));
		outputFileWriter.append(documents + "\n");
		for(Entry<String, TermInfo> cluster : voc.entrySet()) {
			StringBuffer strbf = new StringBuffer();
			strbf.append(new String(cluster.getKey().getBytes(), "UTF8") + " ");
			strbf.append(cluster.getValue().df + " ");
			strbf.append(cluster.getValue().index + " ");
			strbf.append("\n");
			try {
				outputFileWriter.write(strbf.toString());
			} catch (IOException e) {
				continue;
			}
		}
		outputFileWriter.close();
	}
	
	public static Vocabulary loadFromFile(String inputFile) throws IOException {
		Vocabulary vocabulary = Vocabulary.getInstance();
		BufferedReader inputFileReader = new BufferedReader( new InputStreamReader( 
				new FileInputStream(inputFile), "UTF8"));
		
		String line = inputFileReader.readLine();
		Vocabulary.vocabulary.documents = Integer.parseInt(line);
		while( (line =inputFileReader.readLine()) != null){
			String[] parts = line.split(" ");
			TermInfo info = new TermInfo(Integer.parseInt(parts[2]), Long.parseLong(parts[1]));
			Vocabulary.vocabulary.voc.put(parts[0], info);
		}
		
		inputFileReader.close();
		return vocabulary;
	}

 
	public static Vocabulary createVocabulary(Iterator<Item> postsIterator) {
		Vocabulary vocabulary = Vocabulary.getInstance();
                Item tmp_post=null;
                while(postsIterator.hasNext()){
                    tmp_post=postsIterator.next();
                    List<String> tokens = TweetPreprocessor.Tokenize(tmp_post.getTitle());
                    vocabulary.update(tokens);
                }
		return vocabulary;
	}
         
	
	public static void main(String[] argv) throws IOException {
		
		File root = new File("/home/manosetro/Desktop/corpus");
		
		Vocabulary vocabulary = new Vocabulary();
		
		for(File doc : root.listFiles()) {
			Set<String> terms = new HashSet<String>();
			FileInputStream fstream = new FileInputStream(doc);
			 
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			String line = null;
			while ((line=br.readLine()) != null) {
				StringTokenizer tokenizer = new StringTokenizer(line);
				while(tokenizer.hasMoreTokens())
					terms.add(tokenizer.nextToken());
			}	
			br.close();
			in.close();
			fstream.close();
			System.out.println(doc.getName()+" terms: "+terms.size());
			vocabulary.update(terms.toArray(new String[terms.size()]));
		}

		System.out.println(vocabulary.documents);

	}
	
}
