package eu.socialsensor.graphbased.clust;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import edu.uci.ics.jung.graph.Graph;

public class ScanCommunityDetector<V,E> {
	

	// default SCAN parameters
	private double epsilon = 0.5;
	private int mu = 5;
	
	private StructuralSimilarityScorer<V, E> ssScorer = null;
	private LWPCommunityDetector<V, E> lwp = null;
	private Graph<V,E> graph = null;
	
	public ScanCommunityDetector(){
	}
	public ScanCommunityDetector(double epsilon, int mu){
		this.epsilon = epsilon;
		this.mu = mu;
	}
	public ScanCommunityDetector(StructuralSimilarityScorer<V,E> scorer){
		this.ssScorer = scorer;
	}
	
	public ScanCommunityStructure<V, E> getRefinedCommunityStructure(Graph<V,E> graph) {
		return getRefinedCommunityStructure(graph, true);		
	}
	
	public ScanCommunityStructure<V, E> getRefinedCommunityStructure(Graph<V,E> graph, boolean doExpansion) {
		
		this.graph = graph;
		
		ScanCommunityStructure<V, E> cs = new ScanCommunityStructure<V, E>(graph);
		if (ssScorer == null){
	//		System.out.println("New structural-similarity-scorer!");
			ssScorer = new StructuralSimilarityScorer<V, E>(graph);
		}
		mu = graph.getVertexCount()/muScale;
		epsilon = maxEpsilon;
		
		//long t0 = System.currentTimeMillis();
		int cIdx = -1;
		int minimumCoverage = (int)(0.9 * graph.getVertexCount());
		Set<V> nonMembers = null;
		while (cs.getNumberOfMembers() < minimumCoverage){
			nonMembers = new HashSet<V>();
			Iterator<V> vIter = graph.getVertices().iterator();
			long iterT0 = System.currentTimeMillis();
			while (vIter.hasNext()){
				
				V seed = vIter.next();
				Integer seedCommunityIdx = cs.getCommunityIndex(seed); 
				if (seedCommunityIdx >= 0) continue;
				if (nonMembers.contains(seed)) continue;
				
				if (isCore(graph,seed,cs,-1)){
					cIdx++;
					Stack<V> Q = getEpsilonNeighborhood(graph, seed);
					Q.push(seed);
					while (!Q.isEmpty()){
						V cand = Q.pop();
						if (isCore(graph, cand, cs, cIdx)){
							Stack<V> R = getEpsilonNeighborhood(graph, cand);
							while (!R.isEmpty()){
								V toAdd = R.pop();
								Integer toAddCommunityIndex = cs.getCommunityIndex(toAdd);
								if (toAddCommunityIndex >= 0) continue;
								if (!nonMembers.contains(toAdd)){
									Q.push(toAdd);
								}
								cs.addVertexToCommunity(toAdd, cIdx);
							}
						}
						else {
							nonMembers.add(cand);
						}
					}
				} else {
					nonMembers.add(seed);
				}
			}
			double iterationTime = (System.currentTimeMillis()-iterT0)/1000.0;
			System.out.println(mu + " " + epsilon + " " + 
					cs.getNumberOfMembers() + " " + cs.getNumberOfCommunities() + ", " +
					iterationTime + " secs");
			
			mu = getNextMu(mu);
			if (mu == minMu && epsilon > minEpsilon) {
				epsilon -= epsilonStep;
				mu = graph.getVertexCount()/muScale;
			}
			if (mu == minMu && epsilon <= minEpsilon){
				break;
			}
		}
		//System.out.println("MULTI-SCAN: " + (System.currentTimeMillis() - t0)/1000.0 + " secs");
		//t0 = System.currentTimeMillis();
		
		//System.out.println("#members (after SCAN): " + cs.getNumberOfMembers());
		
		if (doExpansion){
			lwp = new LWPCommunityDetector<V, E>();
			ScanCommunityStructure<V, E> expandedCommunityStructure = 
				new ScanCommunityStructure<V, E>(graph);
			for (int i = 0; i < cs.getNumberOfCommunities(); i++){
				Community<V, E> community = cs.getCommunity(i);
				Community<V, E> expandedCommunity = expandCommunitySeedSet(community);
				expandedCommunityStructure.addCommunity(expandedCommunity);
			}
			//System.out.println("EXPANSION: " + (System.currentTimeMillis() - t0)/1000.0 + " secs");
			//System.out.println("#members (after EXPANSION): " + expandedCommunityStructure.getNumberOfMembers());
			findHubsOutliers(graph, nonMembers, expandedCommunityStructure);
			return expandedCommunityStructure;
		} else {
			findHubsOutliers(graph, nonMembers, cs);
			return cs;
		}
	}
	
	
	private int getNextMu(int mu){
		int newMu = mu;
		if (mu > 100000){
			newMu -= 1000;
		} else if (mu > 10000){
			newMu -= 500;
		} else if (mu > 5000){
			newMu -= 200;
		} else if (mu > 1000){
			newMu -= 50;
		} else if (mu > 500){
			newMu -= 20;
		} else if (mu > 250){
			newMu -= 10;
		} else if (mu > 100){
			newMu -= 5;
		} else if (mu > 50){
			newMu -= 2;
		} else {
			newMu--;
		}
		return newMu;
	}
	
	
	/*
	public ScanCommunityStructure<V, E> getRefinedCommunityStructure(Graph<V,E> graph) {
		
		lwp = new LWPCommunityDetector<V, E>();
		this.graph = graph;
		
		ScanCommunityStructure<V, E> cs = new ScanCommunityStructure<V, E>(graph);
		ssScorer = new StructuralSimilarityScorer<V, E>(graph);
		
		mu = graph.getVertexCount()/muScale;
		epsilon = maxEpsilon;
		
		long t0 = System.currentTimeMillis();
		int cIdx = -1;
		int minimumCoverage = (int)(0.9 * graph.getVertexCount());
		Set<V> nonMembers = null;
		while (cs.getNumberOfMembers() < minimumCoverage){
			nonMembers = new HashSet<V>();
			Iterator<V> vIter = graph.getVertices().iterator();
			
			while (vIter.hasNext()){
				
				V seed = vIter.next();
				Integer seedCommunityIdx = cs.getCommunityIndex(seed); 
				if (seedCommunityIdx >= 0) continue;
				if (nonMembers.contains(seed)) continue;
				
				if (isCore(graph,seed,cs)){
					cIdx++;
					Stack<V> Q = getEpsilonNeighborhood(graph, seed);
					Q.push(seed);
					while (!Q.isEmpty()){
						V cand = Q.pop();
						if (isCore(graph, cand, cs)){
							Stack<V> R = getEpsilonNeighborhood(graph, cand);
							while (!R.isEmpty()){
								V toAdd = R.pop();
								Integer toAddCommunityIndex = cs.getCommunityIndex(toAdd);
								if (toAddCommunityIndex >= 0) continue;
								if (!nonMembers.contains(toAdd)){
									Q.push(toAdd);
								}
								cs.addVertexToCommunity(toAdd, cIdx);
							}
						}
						else {
							nonMembers.add(cand);
						}
					}
				} else {
					nonMembers.add(seed);
				}
			}
			
			//System.out.println(mu + " " + epsilon + " " + cs.getNumberOfMembers() + " " + cs.getNumberOfCommunities());
			
			mu--;
			if (mu == minMu && epsilon > minEpsilon) {
				epsilon -= epsilonStep;
				mu = graph.getVertexCount()/muScale;
			}
			if (mu == minMu && epsilon <= minEpsilon){
				break;
			}
		}
		System.out.println("MULTI-SCAN: " + (System.currentTimeMillis() - t0)/1000.0 + " secs");
		t0 = System.currentTimeMillis();
		
		System.out.println("#members (after SCAN): " + cs.getNumberOfMembers());
		
		ScanCommunityStructure<V, E> expandedCommunityStructure = 
			new ScanCommunityStructure<V, E>(graph);
		for (int i = 0; i < cs.getNumberOfCommunities(); i++){
			Community<V, E> community = cs.getCommunity(i);
			Community<V, E> expandedCommunity = expandCommunitySeedSet(community);
			expandedCommunityStructure.addCommunity(expandedCommunity);
		}
		System.out.println("EXPANSION: " + (System.currentTimeMillis() - t0)/1000.0 + " secs");
		System.out.println("#members (after EXPANSION): " + expandedCommunityStructure.getNumberOfMembers());
		
		findHubsOutliers(graph, nonMembers, expandedCommunityStructure);
		return expandedCommunityStructure;
	}
	*/
	private double maxEpsilon = 0.9;
	private double minEpsilon = 0.4;
	private double epsilonStep = 0.05;
	private int muScale = 10;
	private int minMu = 3;
	
	
	public ScanCommunityStructure<V, E> getCommunityStructure(Graph<V,E> graph) {
		
		if (ssScorer == null){
//			System.out.println("New structural-similarity-scorer!");
			ssScorer = new StructuralSimilarityScorer<V, E>(graph);
		}
		ScanCommunityStructure<V, E> cs = new ScanCommunityStructure<V, E>(graph);
		int cIdx = -1;
		
		Set<V> nonMembers = new HashSet<V>();
	
		Iterator<V> vIter = graph.getVertices().iterator();
		while (vIter.hasNext()){
			V seed = vIter.next();
			Integer seedCommunityIdx = cs.getCommunityIndex(seed); 
			if (seedCommunityIdx >= 0) continue;
			if (nonMembers.contains(seed)) continue;
			
			if (isCore(graph, seed, cs, cIdx)){
  				cIdx++;
				if (cIdx > 0){
					if (cs.getCommunity(cIdx-1) != null) {
//						System.out.println(cs.getCommunity(cIdx-1).getMembers());
					}
				}
  				Stack<V> Q = getEpsilonNeighborhood(graph, seed);
				Q.push(seed);
//				System.out.println(seed);
  				while (!Q.isEmpty()){
					V cand = Q.pop();
					if (isCore(graph, cand, cs, cIdx)){
						Stack<V> R = getEpsilonNeighborhood(graph, cand);
						while (!R.isEmpty()){
							V toAdd = R.pop();
							Integer toAddCommunityIndex = cs.getCommunityIndex(toAdd);
							if (toAddCommunityIndex >= 0) continue;
							if (!nonMembers.contains(toAdd)){
								Q.push(toAdd);
							}
							cs.addVertexToCommunity(toAdd, cIdx);
						}
					}
					else {
						nonMembers.add(cand);
					}
				}
			} else {
				nonMembers.add(seed);
			}
  		}
	
		findHubsOutliers(graph, nonMembers, cs);
		
		return cs;
	}
	
	private void findHubsOutliers(Graph<V,E> graph, Set<V> nonMembers, ScanCommunityStructure<V, E> cs){
		Iterator<V> nmIter = nonMembers.iterator();
		while (nmIter.hasNext()){
			V nonMember = nmIter.next();
			if (!graph.containsVertex(nonMember)){
				throw new IllegalStateException("Reference graph does not contain community member: " + nonMember.toString());
			}
			
			Collection<V> neighbors = graph.getNeighbors(nonMember);
			if (neighbors == null){
				cs.addOutlier(nonMember);
			} else {
				Iterator<V> neighborIter = neighbors.iterator();
				Set<Integer> neighborCommunities = new HashSet<Integer>();
				while (neighborIter.hasNext()){
					Integer neighbourIdx = cs.getCommunityIndex(neighborIter.next());
					if (neighbourIdx >= 0){
						neighborCommunities.add(neighbourIdx);
					}
				}
				if (neighborCommunities.size() > 1){
					cs.addHub(nonMember, neighborCommunities.size());
				} else {
					cs.addOutlier(nonMember);
				}
			}
		}
	}
	
	private Stack<V> getEpsilonNeighborhood(Graph<V, E> g, V seed){
		Stack<V> nQ = new Stack<V>();
		Collection<V> neighbors = g.getNeighbors(seed);
		if (neighbors == null)	return nQ;
		Iterator<V> nIter = neighbors.iterator();
		while (nIter.hasNext()){
			V n = nIter.next();
			E e = g.findEdge(seed, n);
			double ss = ssScorer.getEdgeScore(e);
			if (ss > epsilon){
				nQ.push(n);
			}
		}
		return nQ;
	}
	
	private boolean isCore(Graph<V,E> g, V seed, ScanCommunityStructure<V, E> cs, int cId){
		Collection<V> neighbors = g.getNeighbors(seed);
		if (neighbors == null)	return false;
		Iterator<V> nIter = neighbors.iterator();
		int count = 0;
		while (nIter.hasNext()){
			V v = nIter.next();
			int cid = cs.getCommunityIndex(v);
			if (cid >= 0 && cid != cId){
				continue;
			}
			E e = g.findEdge(seed, v);
			double ss = ssScorer.getEdgeScore(e);
			if (ss > epsilon){
				count++;
				if (count >= mu){
					return true;
				}
			}
		}
		return false;
	}
	
//	private boolean isCore(Graph<V, E> g, V seed){
//		Collection<V> neighbors = g.getNeighbors(seed);
//		if (neighbors == null)	return false;
//		Iterator<V> nIter = neighbors.iterator();
//		int count = 0;
//		while (nIter.hasNext()){
//			E e = g.findEdge(seed, nIter.next());
//			double ss = ssScorer.getEdgeScore(e);
//			if (ss > epsilon){
//				count++;
//				if (count >= mu){
//					return true;
//				}
//			}
//		}
//		return false;
//	}
	
	private int ccounter = 0;
	public Community<V,E> expandCommunitySeedSet(Community<V,E> seed){
		System.out.println("C: " + (++ccounter) + " members " + seed.getNumberOfMembers() + " " +  seed.getMembers());
		
		Community<V,E> expandedCommunity = new Community<V,E>(seed.getId(), seed.getReferenceGraph(), seed.getMembers());
		
		LocalModularity M = lwp.getLWPModularity(seed);
		
		Set<V> frontier = getFrontier(seed);
		
		List<V> addedMembers = new ArrayList<V>();
		
		int iterCount = 0;
		while (!frontier.isEmpty()){
			iterCount++;
			if (iterCount > 1000){
				break;
			}
			LocalModularity maxM = M;
			V maxV = null;
			for (V candV : frontier){
				//seed.addMember(candV);
				LocalModularity Mnew = lwp.getIncrementalLWPModularity(seed, candV, M);
				if (Mnew.getValue() > maxM.getValue()){
					maxM = Mnew;
					maxV = candV;
				}
				//seed.removeMember(candV);
			}
			if (maxM.getValue() > M.getValue()){
				seed.addMember(maxV);
				addedMembers.add(maxV);
				//System.out.println("Added member: " + maxV + " -> newM = " + maxM.getValue());
				//System.out.println("\t " + maxM.getValue());
				M = maxM;
			} else {
				break;
			}
			//frontier = getFrontier(seed);
			updateFrontier(seed, frontier, maxV);
		}
		
		List<V> finalMembers = seed.getMembers();
		for (int i = 0; i < finalMembers.size(); i++){
			if (expandedCommunity.contains(finalMembers.get(i))){
				continue;
			} else {
				expandedCommunity.addMember(finalMembers.get(i));
			}
		}
		System.out.println("\t added " + addedMembers.size() + ": " + addedMembers);
		return expandedCommunity;
	}
	
	private void updateFrontier(Community<V,E> seed, Set<V> frontier, V addedNode){
		frontier.remove(addedNode);
		for (V frontierCandidate : graph.getNeighbors(addedNode)){
			if (isHighDegree(frontierCandidate)){
				continue;
			}
			if (seed.contains(frontierCandidate)){
				continue;
			}
			frontier.add(frontierCandidate);
		}
	}
	
	private Set<V> getFrontier(Community<V,E> seed){
		Set<V> frontier = new HashSet<V>();
		for (V seedVertex : seed.getMembers()){
			if (isHighDegree(seedVertex)){
				continue;
			}
			for (V neighbor : graph.getNeighbors(seedVertex)){
				if (seed.contains(neighbor)){
					continue;
				}
				frontier.add(neighbor);
			}
		}
		return frontier;
	}

	private Set<V> highDegreeNodes;
	private boolean isHighDegree(V node){
		if (highDegreeNodes == null){
			List<Integer> degrees = new ArrayList<Integer>();
			for (V v : graph.getVertices()){
				degrees.add(graph.degree(v));
			}
			Collections.sort(degrees, Collections.reverseOrder());
			double p = 0.1; // top 10% of nodes will be considered high-degree nodes
			int idx = (int)Math.round(p*graph.getVertexCount());
			int degreeThreshold = degrees.get(idx);
			highDegreeNodes = new HashSet<V>();
			for (V v : graph.getVertices()){
				if (graph.degree(v) >= degreeThreshold){
					highDegreeNodes.add(v);
				}
			}
		}
		if (highDegreeNodes.contains(node)){
			return true;
		} else {
			return false;
		}
	}
	
	public double getEpsilon() {
		return epsilon;
	}
	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}
	public int getMu() {
		return mu;
	}
	public void setMu(int mu) {
		this.mu = mu;
	}

	
	
}
