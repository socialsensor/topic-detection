package eu.socialsensor.graphbased.clust;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.uci.ics.jung.graph.Graph;

/**
 * Class implementing the local community detection method by Luo, Wang and Promislow
 * appearing in the paper "Exploring Local Community Structures in Large Networks", WI 2006.
 * 
 * @author papadop
 *
 * @param <O>
 */
public class LWPCommunityDetector<V,E> {
	
	public Community<V,E> getCommunity(
			Graph<V, E> graph, V seed) {
		
		int cId = 1;
		Community<V,E> community = new Community<V,E>(cId, graph);
		community.addMember(seed);
		Set<V> neighbourSet = new HashSet<V>(graph.getNeighbors(seed));
		
		//int counter = 0;
		
		Set<V> Q = new HashSet<V>();
		
		do {
			Q = new HashSet<V>();
			LocalModularity lastModularity = getLWPModularity(community);
			
			//System.out.println(++counter + " " + lastModularity);
			
			/* addition step */
			Iterator<V> nIter = neighbourSet.iterator();
			Set<V> toRemove = new HashSet<V>();
			while (nIter.hasNext()){
				V uj = nIter.next();
				if (toRemove.contains(uj)){
					continue;
				}
				community.addMember(uj);
				LocalModularity newModularity = getLWPModularity(community);
				//System.out.println("\tADD " + newModularity + " " + community.getNumberOfMembers());
				if (newModularity.getValue() > lastModularity.getValue()){
					lastModularity = newModularity;
					Q.add(uj);
					toRemove.add(uj);
				} else {
					community.removeMember(uj);
				}
			}
			Iterator<V> removeIter = toRemove.iterator();
			while (removeIter.hasNext()){
				neighbourSet.remove(removeIter.next());
			}
			
			/* deletion step */
			Set<V> deleteQ = new HashSet<V>();
			do {
				deleteQ = new HashSet<V>();
				List<V> communityVertices = community.getMembers();
				for (int i = 0; i < communityVertices.size(); i++){
					V currentVertex = communityVertices.get(i);
					community.removeMember(currentVertex);
					LocalModularity newModularity = getLWPModularity(community);
					//System.out.println("\tDEL " + newModularity + " " + community.getNumberOfMembers());
					if ( (newModularity.getValue() > lastModularity.getValue()) && (community.isConnected()) ){
						lastModularity = newModularity;
						deleteQ.add(currentVertex);
						if (Q.contains(currentVertex)){
							Q.remove(currentVertex);
						}
					} else {
						community.addMember(currentVertex);
					}
				}
			} while (!deleteQ.isEmpty() );
			
			/* add vertices to neighbourSet */
			Iterator<V> nIterK = Q.iterator();
			while (nIterK.hasNext()){
				Iterator<V> candIterator = graph.getNeighbors(nIterK.next()).iterator();
				while (candIterator.hasNext()){
					V al = candIterator.next();
					if ((!community.contains(al)) && (!neighbourSet.contains(al))){
						neighbourSet.add(al);
					}
				}
			}
			
		} while (!Q.isEmpty());
		
		if (getLWPModularity(community).getValue() > 0.0 && community.contains(seed)){
			return community;
		} else {
			System.err.println("Empty community returned, because the output community does not" +
					" contain the seed node!");
			return new Community<V,E>(cId, graph);
		}		
	}

	/**
	 * Compute the LPW modularity measure introduced by Luo, Wang and Promislow. This method
	 * has been made public so that other algorithms can use the same measure (but a different
	 * search strategy).
	 * 
	 * @param community Input community.
	 * @return
	 */
	public LocalModularity getLWPModularity(Community<V,E> community){
		/* check community validity*/
		if (!community.isValid()) throw new IllegalArgumentException(
				"You should provide a valid community as argument to the algorithm!");

		Graph<V, E> graph = community.getReferenceGraph();
		
		List<V> communityMembers = community.getMembers();
		int M = community.getNumberOfMembers();
		int indS = 0;
		int outdS = 0;
		for (int i = 0; i < M; i++){
			V currentMember = communityMembers.get(i);
			int currentMemberInDegree = 0;
			for (int j = 0; j < M; j++){
				if (i==j) continue;
				if (graph.findEdge(currentMember, communityMembers.get(j))!= null){
					currentMemberInDegree++;
				}
			}
			/* update out- and in-degree counts of community */
			outdS += graph.degree(currentMember) - currentMemberInDegree;
			/* in-edges were counted twice so divide by 2 */
			indS += (int)Math.round(currentMemberInDegree/2.0); 
		}
		return new LocalModularity(indS, outdS);
	}
	
	public LocalModularity getIncrementalLWPModularity(Community<V,E> community, V candMember, LocalModularity mod) {
		int indS0 = mod.getInDegree();
		int outdS0 = mod.getOutDegree();
		
		Graph<V, E> graph = community.getReferenceGraph();
		List<V> communityMembers = community.getMembers();
		int M = community.getNumberOfMembers();
		
		int currentMemberInDegree = 0;
		for (int i = 0; i < M; i++){
			if (graph.findEdge(candMember, communityMembers.get(i))!= null){
				currentMemberInDegree++;
			} 
		}
		/* update out- and in-degree counts of community */
		outdS0 += graph.degree(candMember) - currentMemberInDegree;
		/* in-edges were counted twice so divide by 2 */
		//indS0 += (int)Math.round(currentMemberInDegree/2.0); // this is probably wrong!
		indS0 += currentMemberInDegree;
		
		return new LocalModularity(indS0, outdS0);

	}
	
	

}
