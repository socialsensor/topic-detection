package eu.socialsensor.documentpivot.LSH;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map.Entry;

import eu.socialsensor.documentpivot.model.VectorSpace;

import org.apache.commons.io.IOUtils;
import org.jblas.DoubleMatrix;

import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister64;
import eu.socialsensor.documentpivot.model.VectorSpace;

public final class HashFamily extends DoubleMatrix {

	/**
	 *  
	 */
	private static final long serialVersionUID = -8257034887576567822L;
	
	private static double MEAN = 0f; 
	private static double VARIANCE = 1.0f;
	
	private Normal normal = new Normal(MEAN, VARIANCE, new MersenneTwister64((int)(System.currentTimeMillis()* (int)this.hashCode())));
	
	int k; // Number of random vectors
	int d; // Dimension of vectors
	
	public HashFamily(int k, int d) {
		super(new double[k][d]);
		this.k = k;
		this.d = d;
		for(int i=0; i<k; i++) {
			double length = 0;
			DoubleMatrix r = new DoubleMatrix(new double[d]);
			for(int j=0; j<d; j++) {
				r.put(j, getGaussian());
				length += Math.pow(r.get(j), 2);
			}
			r.div(Math.sqrt(length));
			this.putRow(i, r);
		}
	}
	
	public Signature hr(double u[]) {
		if(u.length != this.d)
			return null;
		Signature bitVector = new Signature(this.k);
		DoubleMatrix product = this.mmul(new DoubleMatrix(u));
		for(int i=0;i<product.length;i++){
			if(product.get(i)>=0)
				bitVector.set(i);
		}
		return bitVector;
	}
	
	public Signature hr(VectorSpace vsm) {
		
		Signature bitVector = new Signature(this.k);
		Iterator<Entry<Integer, Double>> it = vsm.iterator();
		double product[] = new double[k];
		for(int i=0;i<k;i++) 
			product[i] = 0;
		while(it.hasNext()) {
			Entry<Integer, Double> feature = it.next();
			Integer index = feature.getKey();
			Double weight = feature.getValue();
			for(int i=0;i<k;i++)
				product[i] += this.get(i, index) * weight;
		}
		for(int i=0;i<k;i++)
			if(product[i]>=0) bitVector.set(i);
	
		return bitVector;
	}
	
	public void writeToFile(String filename) throws IOException {
		OutputStream output = new FileOutputStream(new File(filename));
		IOUtils.write(k + " " + d + "\n", output);
		for(int i=0; i<k; i++) {
			for(int j=0; j<d; j++) {
				IOUtils.write(this.get(i, j)+" ", output);
			}
			IOUtils.write("\n", output);
		}
	}
	
	private double getGaussian() {
		return normal.nextDouble();
	}
	
	public static void main(String... aArgs) throws IOException {
		
		/*
		long total_time = System.currentTimeMillis();
		
		String vectorsFile = "/media/sda3/vectors2.txt";
		
		int K = 4;
		int d = 1024;
	
		long t = System.currentTimeMillis();
		System.out.print("Create Hash Functions Family... ");
		HashFamily hf = new HashFamily(K, d);
		System.out.print("END ");
		System.out.println("Hash funtions created in "+(System.currentTimeMillis()-t)+" milliseconds");
		
		List<Vector> features = new ArrayList<Vector>();
		System.out.print("Load features... ");
		BufferedReader in = new BufferedReader(new FileReader(vectorsFile));
		String line = null;
		t = System.currentTimeMillis();
		while((line=in.readLine()) != null) {
			String[] vector = line.split(" ");
			String imageId = vector[0].trim();
			if(vector.length != (d+1))
				continue;
			double u[] = new double[vector.length-1];
			for(int i=1;i<vector.length;i++) {
				u[i-1] = Double.parseDouble(vector[i]);
			}

			//features.add(new Image(imageId, u));
		}
		in.close();
		System.out.print("END ");
		System.out.println(features.size()+ " features loaded in "+(System.currentTimeMillis()-t)+" milliseconds");
		
		System.out.print("Compute signatures... ");
		t = System.currentTimeMillis();
		DoubleMatrix vectors = new DoubleMatrix(new double[1024][features.size()]);
		int column = 0;
		for(Vector imageFeature : features) {
			vectors.putColumn(column++, new DoubleMatrix(imageFeature.v));
		}
		
		long hr_time = System.currentTimeMillis();
		DoubleMatrix result = hf.mmul(vectors);
		System.out.println(result.rows+"x"+result.columns);
		hr_time = System.currentTimeMillis() - hr_time;
		List<Signature> signatures = new ArrayList<Signature>(result.columns);
		for(int i=0;i<result.columns;i++) {
			Vector image = features.get(i);
			Signature signature = new Signature(result.rows);
			for(int j=0;j<result.rows;j++) {
				if(result.get(j, i)>=0)
					signature.set(j);
			}
			//lsh.insert(signature);
			signatures.add(signature);
		}
		System.out.print("END ");
		System.out.println("Signatures computed in "+(System.currentTimeMillis()-t)+" milliseconds. Hash time : " + hr_time);
		System.out.println("Mean time per signature "+((System.currentTimeMillis()-t)/(double)signatures.size())+" milliseconds.");
		
		System.out.println("Compute 20 NN using LSH buckets... ");
		t = System.currentTimeMillis();
		for(Vector imageFeature : features) {
			//BoundedPriorityQueue<RankedImage> nns = lsh.getNN(imageFeature);
			//System.out.println(imageFeature.imageId +" "+ nns.poll() + nns.poll() + nns.poll() + nns.poll() + nns.poll());
		}
		System.out.print("END ");
		System.out.println("NN computed in "+(System.currentTimeMillis()-t)+" milliseconds.");
		System.out.println("Mean time per query "+((System.currentTimeMillis()-t)/(double)features.size())+" milliseconds.");
		
		System.out.println("Total time: "+(System.currentTimeMillis()-total_time));
		*/
	}
	
	public static double testCosine(double[] u1, double u2[]) {
		double m1=0, m2=0, num=0;
		for(int i=0;i<u1.length;i++) {
			m1 += (u1[i]*u1[i]);
			m2 += (u2[i]*u2[i]);
			num += (u1[i]*u2[i]);
		}
		return num/(Math.sqrt(m1)*Math.sqrt(m2));
	}
} 