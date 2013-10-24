package eu.socialsensor.graphbased.clust;
 
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.graph.Graph;

public class ScanCommunityStructure<V,E> {
	
	
	/* map of vertex-ID to community-ID */
	private Map<V, Integer> cMap = new HashMap<V, Integer>();
	
	/* list of communities */
	private Map<Integer, Community<V,E>> communities = new HashMap<Integer, Community<V,E>>();
	
	private List<V> hubs = new ArrayList<V>();
	private Map<V, Integer> availableHubs = new HashMap<V, Integer>();
	
	private List<V> outliers = new ArrayList<V>();
	private Set<V> availableOutliers = new HashSet<V>();
	
	private Graph<V, E> graph = null;
	
	public ScanCommunityStructure(Graph<V, E> g){
		this.graph = g;
	}
	
	
	public void setHubsAndOutliers(ScanCommunityStructure<V,E> structure){
		List<V> listOfHubs = structure.getHubs();
		for (int i = 0; i < listOfHubs.size(); i++){
			this.addHub(listOfHubs.get(i), structure.getHubAdjacentCommunities(listOfHubs.get(i)));
		}
		
		List<V> listOfOutliers = structure.getOutliers();
		for (int i = 0; i < listOfOutliers.size(); i++){
			this.addOutlier(listOfOutliers.get(i));
		}
	}
	
	public boolean addCommunity(Community<V,E> community){
		
		int maxID = -1;
		
		for (Integer cID: communities.keySet()){
			if (cID > maxID){
				maxID = cID;
			}
		}
		
		maxID++;
		
		communities.put(maxID, community);
		for (V member : community.getMembers()){
			cMap.put(member, maxID);
		}
		return true;
	}
	
	public boolean addVertexToCommunity(V toAdd, int cId){

		Community<V,E> c = communities.get(cId);
		if (c == null){
			c = new Community<V, E>(cId, graph);
			communities.put(cId, c);
		}
		c.addMember(toAdd);
		cMap.put(toAdd, cId);
		return true;
	}
	public int getCommunityIndex(V vertex){
		Integer cId = cMap.get(vertex);
		if (cId == null){
			return -1;
		}
		return cId;
	}
	
	
	
	public int getNumberOfMembers(){
		return cMap.size();
	}
	
	public int getNumberOfCommunities(){
		return communities.size();
	}
	public Community<V,E> getCommunity(int cId){
		return communities.get(cId);
	}
	
	public boolean addHub(V hubToAdd, int nrAdjacentCommunities){
		if (availableHubs.containsKey(hubToAdd) ||
				availableOutliers.contains(hubToAdd) ||
				cMap.containsKey(hubToAdd)){
			return false;
		}
		hubs.add(hubToAdd);
		availableHubs.put(hubToAdd, nrAdjacentCommunities);
		return true;
	}
	public List<V> getHubs(){
		return hubs;
	}
	public boolean isHub(V hubCandidate){
		return availableHubs.containsKey(hubCandidate);
	}
	
	
	public int getHubAdjacentCommunities(V hubIdx){
		if (availableHubs.containsKey(hubIdx)){
			return availableHubs.get(hubIdx);
		} else {
			return 0;
		}
	}
	
	public boolean addOutlier(V outlierToAdd){
		if (availableHubs.containsKey(outlierToAdd) ||
				availableOutliers.contains(outlierToAdd) ||
				cMap.containsKey(outlierToAdd)){
			return false;
		}
		outliers.add(outlierToAdd);
		availableOutliers.add(outlierToAdd);
		return true;
	}
	
	// this is slow
	public boolean removeOutlier(V outlierToRemove){
		if (! availableOutliers.contains(outlierToRemove)){
			return false;
		}
		availableOutliers.remove(outlierToRemove);
		return outliers.remove(outlierToRemove);
	}
	public boolean isOutlier(V candidateOutlier){
		return availableOutliers.contains(candidateOutlier);
	}
	
	
	public List<V> getOutliers(){
		return outliers;
	}

	
	public void printCommunitySummary(){
		
		System.out.println(communities.size() + " communities");
		System.out.println(graph.getVertexCount() - hubs.size() - outliers.size() + " members");
		System.out.println(hubs.size() + " hubs");
		System.out.println(outliers.size() + " outliers");
		
		int maxToShow = 10;
		Iterator<Community<V, E>> cIter = communities.values().iterator();
		int count = 0;
		while (cIter.hasNext()){
			if (++count > maxToShow){
				break;
			}
			Community<V, E> c = cIter.next();
			System.out.print(c.getId() + "(" + c.getNumberOfMembers() + " members): ");
			int Nmembers = Math.min(c.getNumberOfMembers(), 10);
//			System.out.print("[");
			for (int i = 0; i < Nmembers; i++){
				V m = c.getMembers().get(i);
				int d = graph.degree(m);
//				System.out.print("(" + m + ", " + d + ") ");
			}
//			System.out.println("]");
			//System.out.println(c.getMembers().subList(0, Nmembers));
		}
		
		int Nhubs = Math.min(hubs.size(), 10);
//		System.out.print("[");
//		for (int i = 0; i < Nhubs; i++){
//			System.out.print("(" + hubs.get(i) + "," + availableHubs.get(hubs.get(i))+"), ");
//		}
//		System.out.println("]");
		//System.out.println(hubs.subList(0, Nhubs).toString());
		int Noutliers = Math.min(outliers.size(), 10);
		System.out.println(outliers.subList(0, Noutliers).toString());
	}
	
	public void writeSizesToFile(String file) throws IOException {
		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
		for (int i = 0; i < getNumberOfCommunities(); i++){
			writer.append(String.valueOf(getCommunity(i).getNumberOfMembers()));
			writer.newLine();
		}
		writer.close();
	}
	
	public void writeStructureToFile(String file) throws IOException {
		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
                writer.append("--COMMNUNITIES--");
		writer.newLine();
		writer.append(String.valueOf(communities.size()));
		writer.newLine();
		for (Community<V,E> community : communities.values()) {
			writer.append(String.valueOf(community.getNumberOfMembers()));
			for (V member : community.getMembers()){
				writer.append("\t" + member.toString());
			}
			writer.newLine();
		}
		
                writer.append("--HUBS--");
		writer.newLine();
		writer.append(String.valueOf(hubs.size()));
		writer.newLine();
		for (V hub : hubs) {
			writer.append(hub.toString() + "\t" + getHubAdjacentCommunities(hub));
			writer.newLine();
		}
                writer.append("--OUTLIERS--");
		writer.newLine();
		writer.append(String.valueOf(outliers.size()));
		writer.newLine();
		for (V outlier : outliers){
			writer.append(outlier.toString());
			writer.newLine();
		}
		
		writer.close();
	}

        /*
	public static Iterator<List<FreqTag>> readFreqTagLists(final String communityFile){
		return new Iterator<List<FreqTag>>(){
			
			private BufferedReader reader;
			private int nrCommunities = 0;
			private int counter = 0;
			{
				try {
					reader = new BufferedReader(
							new InputStreamReader(new FileInputStream(communityFile),StringConstants.UTF8));
					String line = reader.readLine();
					nrCommunities = Integer.parseInt(line);
				} catch (IOException e){
					e.printStackTrace();
				}
			}
			
			@Override
			public boolean hasNext() {
				if (counter < nrCommunities){
					return true;
				} else {
					return false;
				}
			}

			@Override
			public List<FreqTag> next() {
				String line = null;
				try {
					if (reader != null){
						if (reader.ready()){
							line = reader.readLine();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (line == null){
					return null;
				}
				String[] parts = line.split(StringConstants.TAB);
				int members = Integer.parseInt(parts[0]);
				List<FreqTag> memberList = new ArrayList<FreqTag>(members);
				for (int x = 1; x <= members; x++){
					FreqTag ftag = parseFreqTagMember(parts[x]);
					if (ftag != null){
						memberList.add(ftag);
					}
				}
				counter++;
				if (counter == nrCommunities){
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return memberList;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("This iterator implementation does not support removal!");
			}	
		};
		
	}
	
	
	public static ScanCommunityStructure<FreqTag, Cooccurrence> readFromFile(String file, Graph<FreqTag,Cooccurrence> refGraph) throws IOException {
		ScanCommunityStructure<FreqTag, Cooccurrence> tagCommunityStructure =
			new ScanCommunityStructure<FreqTag, Cooccurrence>(refGraph);
		
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file),StringConstants.UTF8));
		String line = reader.readLine();
		int nrCommunities = Integer.parseInt(line);
		for (int i = 0; i < nrCommunities; i++){
			line = reader.readLine();
			if (line == null){
				throw new IllegalStateException("This is not an appropriately formated community file!");
			}
			String[] parts = line.split(StringConstants.TAB);
			int members = Integer.parseInt(parts[0]);
			Community<FreqTag, Cooccurrence> community = new Community<FreqTag, Cooccurrence>(i, refGraph);
			for (int x = 1; x <= members; x++){
				FreqTag ftag = parseFreqTagMember(parts[x]);
				if (ftag == null){
					continue;
				}
				community.addMember(ftag);
			}
			tagCommunityStructure.addCommunity(community);
		}
		
		line = reader.readLine();
		if (line == null){
			return tagCommunityStructure;
		}
		int nrHubs = Integer.parseInt(line);
		for (int i = 0; i < nrHubs; i++){
			line = reader.readLine();
			String[] parts = line.split(StringConstants.TAB);
			if (parts.length != 2){
				continue;
			}
			FreqTag hubTag = parseFreqTagMember(parts[0]);
			int nrOfCommunities = Integer.parseInt(parts[1]);
			tagCommunityStructure.addHub(hubTag, nrOfCommunities);
		}
		line = reader.readLine();
		int nrOutliers = Integer.parseInt(line);
		for (int i = 0; i < nrOutliers; i++){
			line = reader.readLine();
			FreqTag outlierTag = parseFreqTagMember(line);
			if (outlierTag == null){
				continue;
			}
			tagCommunityStructure.addOutlier(outlierTag);
		}
		reader.close();
		return tagCommunityStructure;
	}
	
	protected static FreqTag parseFreqTagMember(String text){
		int index = text.lastIndexOf(StringConstants.COLON);
		if (index < 0){
			return null;
		}
		int freq = Integer.parseInt(text.substring(index+1, text.length()-1));
		return new FreqTag(text.substring(1, index), freq);
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(communities.size() + " communities, ");
		buf.append(hubs.size() + " hubs, ");
		buf.append(outliers.size() + " outliers");
		return buf.toString();
	}
	
	*/
}
