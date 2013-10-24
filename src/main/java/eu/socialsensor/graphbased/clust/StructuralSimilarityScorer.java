package eu.socialsensor.graphbased.clust;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.algorithms.scoring.EdgeScorer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

public class StructuralSimilarityScorer<V,E> implements EdgeScorer<E, Double> {

	private Graph<V, E> g = null;
	
	private Map<E, Double> scoreMap = new HashMap<E, Double>();
	
	
	public StructuralSimilarityScorer(Graph<V, E> g){
		this.g = g;
	}
	
	public void evaluate() {
		if (g == null){
			System.err.println("ERROR: Graph is null!");
			return;
		}
		Collection<E> edges = g.getEdges();
		for (E e : edges){
			getEdgeScore(e);
		}
	}

	public Double getEdgeScore(E e) {
		Double score = scoreMap.get(e);
		if (score != null) return score;
		Pair<V> ends = g.getEndpoints(e);
		Set<V> vmap = new HashSet<V>(g.getNeighbors(ends.getFirst()));
		vmap.add(ends.getFirst());
		Collection<V> v1Neighbors = g.getNeighbors(ends.getSecond());
		int countCommon = 0;
		if (vmap.contains(ends.getSecond())){
			countCommon++;
		}
		for (V neighbor : v1Neighbors){
			if (vmap.contains(neighbor)){
				countCommon++;
			}
		}
		Double structureSimilarity = 
			(double)countCommon/Math.sqrt(vmap.size()*(v1Neighbors.size()+1));
		scoreMap.put(e, structureSimilarity);
		return structureSimilarity;
	}

}
