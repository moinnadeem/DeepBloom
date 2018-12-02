package edu.mit.course6828.BloomFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BloomFilterUtils {
	
	public static int calculateOptimalK(int m, int n) {
		
		int k = (int)((m/(double)n) * Math.log(2)); // optimal value of k is (m/n)ln(2)
		return k;
	}
	
	/** Get all the URLs in the data file and put into a set. */
	public static Set<String> getUrls(boolean badOnly) throws IOException {
		File file = new File("model_training/data.csv");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		Set<String> lines = new HashSet<String>();
		int maxBits = Integer.MIN_VALUE;
		int minBits = Integer.MAX_VALUE;
		try {
			String line = reader.readLine();
			int i = 0; int j = 0;
			while(line != null) {
				if (badOnly && line.endsWith("bad")) {
					String url = line.substring(0, line.lastIndexOf("bad") - 1);
					j += lines.contains(url) ? 1 : 0;
					lines.add(url);
					i++;
				} else if (!badOnly) {
					String url = line;
					if (line.endsWith("good")) {
						url = line.substring(0, line.lastIndexOf("good") - 1);
					} else if (line.endsWith("bad")) {
						url = line.substring(0, line.lastIndexOf("bad") - 1);
					}
					j += lines.contains(url) ? 1 : 0;
					lines.add(url);
					i++;
				}
				maxBits = Math.max(maxBits, line.getBytes().length * 8);
				minBits = Math.min(minBits, line.getBytes().length * 8);
				line = reader.readLine();
			}
			System.out.println("number of " + (badOnly ? "bad " : "") + "URLs: " + lines.size());
			System.out.println("min, max bits: " + minBits + ", " + maxBits);
			System.out.println("noUrls: " + i + ", noDuplicates: " + j);
			return lines;
		} catch (IOException e) {
			throw new IOException();
		} finally {
			reader.close();
		}
	}
	
	/** 
	 * Gets false positive rate of regular Bloom Filter and 
	 * size of filter by checking file that URLs are written to. 
	 */
	public static void getFpr() throws IOException {
		int m = 3348531;
		int k = 4;
		int bitsMin = 40; // 40 for data, 14 example
		int bitsMax = 18488; // 18488 for data, 23 example
		
		Random r = new SecureRandom();
		Set<String> badUrls = getUrls(true);
		Set<String> urls = getUrls(false);
//		final int noItems = 1 << 14;
//		final int noItems = badUrls.size();
//		final int NO_FALSE_POSITIVE_TESTS = badUrls.size();

		for (int log2bits = bitsMin; log2bits <= bitsMax; log2bits++) {
//		for (int log2bits = bitsMin; log2bits <= bitsMin+2; log2bits++) {
		  for (int noHashes = 1; noHashes <= k; noHashes++) {
			System.out.println("log2bits: " + log2bits + ", noHashes: " + noHashes);
		    int noFalsePositives = 0;
		    int noNotIn = 0;

		    BloomFilter bf = new BloomFilterImpl(m, k);
		    Set<String> already = new HashSet<>();
		    // Add items to Bloom filter
		    for (String s : badUrls) {
		    		already.add(s);
		    		bf.add(s);
		    		if (!bf.contains(s)) {
		    			System.out.println("contains");
		    		}
		    }

		    // Now test for false positives
		    PrintWriter writer = new PrintWriter("outputs/positives.txt", "UTF-8");
		    for (String s: urls) {
		      if (!already.contains(s)) {
		        noNotIn++;
		        if (bf.contains(s)) noFalsePositives++;
		      }
		      
		      if (bf.contains(s)) {
		    	  	writer.println(s);
		      }
		    }
		    
		    writer.close();
		    double falsePositiveRate = noNotIn == 0 ? 0d : (double) noFalsePositives / (noFalsePositives + noNotIn);
		    System.out.println("numNotIn: " + noNotIn + ", numFalsePos: " + noFalsePositives + ", fpr: " + falsePositiveRate);
		    
		    FileOutputStream outputStream = new FileOutputStream("outputs/bfWrite.txt");
		    bf.save(outputStream);
		  }
		}
		System.out.println("done");
	}
	
	public static void main(String[] args) throws IOException {
		BloomFilterUtils.getFpr();
	}
}
