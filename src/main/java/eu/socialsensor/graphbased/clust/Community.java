/*
 * @(#) Community.java	1.0,	01/12/2008
 * 
 * Copyright (c) CERTH, WeKnowIt 2008
 */
package eu.socialsensor.graphbased.clust;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import edu.uci.ics.jung.graph.Graph;


/**
 * Data class that models a node community.  
 * The community is identified by an id field and is always defined
 * with reference to a Graph object (which shouldn't be modified)
 * To specify the members of the community, a list of objects is
 * necessary. Obviously, the objects need to be contained in the graph.
 *  
 *   
 * @author papadop
 *
 */
public class Community<V, E> {

	private int id = -1;
	private String name = null;
	private Set<V> members = new HashSet<V>();
	private final Graph<V, E> referenceGraph;
	
	/**
	 * When this constructor is used, it is assumed that a new
	 * empty community is needed
	 * @param id
	 * @param graph
	 */
	public Community(int id, Graph<V, E> graph){
		if (id < 0){
			throw new IllegalArgumentException("Community id should be a non-negative integer!");
		}
		this.id = id;
		this.referenceGraph = graph;
	}

	/**
	 * When this constructor is used, it is assumed that an already
	 * existing community is needed, so NO tag network initialization
	 * takes place. 
	 * 
	 * @param id
	 * @param net
	 * @param members
	 */
	public Community(int id, Graph<V, E> net, List<V> members){
		if (id < 0){
			throw new IllegalArgumentException("Community id should be a non-negative integer!");
		}
		this.id = id;
		this.referenceGraph = net;
		for (int i = 0; i < members.size(); i++){
			addMember(members.get(i));
		}
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}
	
	public void addMember(V t){
		if (referenceGraph == null){
			throw new IllegalStateException("The community object has not been properly initialized!");
		}
		if (!referenceGraph.containsVertex(t)){
			throw new IllegalStateException("You attempt to add to the community a member, which does not exist in the graph: " + t);
		}
		members.add(t);
	}
	
	public void removeMember(V t){
		members.remove(t);
	}
	
	public int getNumberOfMembers(){
		return members.size();
	}
	public List<V> getMembers(){
		List<V> comElements = new ArrayList<V>(members);
		return comElements;
	}
	public void setMembers(List<V> comElements){
		for (int i = 0; i < comElements.size(); i++){
			addMember(comElements.get(i));
		}
	}
	
	public boolean contains(V t){
		return members.contains(t);
	}
	
	/**
	 * Check whether the community is connected (single-component).
	 * @return true if connected, false otherwise
	 */
	public boolean isConnected(){
		if (members.size() < 1) return false;
		
		/* visit all possible nodes starting from an arbitrary node of the community */
		Set<V> reachableNodes = new HashSet<V>();
		Set<V> visited = new HashSet<V>();
		V seed = members.iterator().next();
		if (!referenceGraph.containsVertex(seed)){
			throw new IllegalStateException("Reference graph does not contain community member: " + seed.toString());
		}
		Stack<V> frontier = new Stack<V>();
		frontier.push(seed);
		while (!frontier.isEmpty()){
			V cand = frontier.pop();
			visited.add(cand);
			reachableNodes.add(cand);
			Iterator<V> nIter = referenceGraph.getNeighbors(cand).iterator();
			while(nIter.hasNext()){
				V ni = nIter.next();
				if (visited.contains(ni)){
					continue;
				}
				if (members.contains(ni)){
					if (!reachableNodes.contains(ni)){
						reachableNodes.add(ni);
					}
					frontier.add(ni);
				}
			}
		}
		/* if the reachable nodes (starting by a seed) are not equal to all community
		 * nodes then the community is not connected */
		if (reachableNodes.size() < members.size()){
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Check that all IDs contained in the community correspond to actual
	 * nodes in the underlying graph
	 * @return true if community is valid, false otherwise
	 */
	public boolean isValid(){
		Iterator<V> idIter = members.iterator();
		while (idIter.hasNext()){
			V v = idIter.next();
			if (! referenceGraph.containsVertex(v)){
				//System.out.println(v + " " + referenceGraph.getVertexCount());
				return false;
			}
		}
		return true;
	}
	
	public Graph<V, E> getReferenceGraph(){
		return referenceGraph;
	}
	
	
}
