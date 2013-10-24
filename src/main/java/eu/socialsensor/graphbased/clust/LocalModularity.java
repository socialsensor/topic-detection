package eu.socialsensor.graphbased.clust;

public class LocalModularity {
	
	private int inDegree;
	private int outDegree;
	
	public LocalModularity(int inDegree, int outDegree){
		this.inDegree = inDegree;
		this.outDegree = outDegree;
	}
	
	public double getValue(){
		if ((outDegree == 0) && (inDegree > 0)){
			return Double.MAX_VALUE;
		} else if ((inDegree == 0) && (outDegree == 0)){
			/* depending on whether we consider an isolated node as a community or not
			 * the returned value should be 1.0 or 0.0 respectively */
			return 0.0;	
		} else {
			return (double)inDegree/(double)outDegree;
		}
	}
	
	public int getInDegree() {
		return inDegree;
	}
	public int getOutDegree() {
		return outDegree;
	}
}
