package edu.mit.BloomFilter.SandwichedBloomFilter;

public class SandwichedBloomFilterUtils {

	
	/**
	 * 
	 * @param Fn the False Negative Rate for the oracle learned function
	 * @param Fp the False Positive Rate for the oracle learned function
	 * @param alpha For convenience, we consider alpha=0.5, which corresponds to perfect hash function based Bloom filters
	 */
	public static double determineOptimalValueForB2(double Fn, double Fp, double alpha) { // b2 is number of bits per key for the backup filtler
		double b2 = Fn * Math.log(Fp/((1-Fp)*(1/Fn - 1)))/Math.log(alpha);
		return b2;
	}
	
	public static void main(String[] args) {
		double b2 = determineOptimalValueForB2(1.0/2, 1.0/100, 0.5);
		System.out.println(b2);
	}
	
}
