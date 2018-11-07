package edu.mit.course6828.BloomFilter;

public class BloomFilterUtils {
	
	public static int calculateOptimalK(int m, int n) {
		
		int k = (int)((m/(double)n) * Math.log(2)); // optimal value of k is (m/n)ln(2)
		return k;
	}
}
