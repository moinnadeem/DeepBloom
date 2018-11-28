package edu.mit.course6828.BloomFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Main {
	
	public static Set<String> getUrls(boolean badOnly) throws IOException {
		File file = new File("model_training/data.csv");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		Set<String> lines = new HashSet<String>();
		int maxBits = Integer.MIN_VALUE;
		int minBits = Integer.MAX_VALUE;
		try {
			String line = reader.readLine();
			
			while(line != null) {
				if (!badOnly || (badOnly && line.endsWith("bad"))) {
					String url = line;
					if (line.endsWith("good")) {
						url = line.substring(0, line.lastIndexOf("good"));
					} else if (line.endsWith("bad")) {
						url = line.substring(0, line.lastIndexOf("bad"));
					}
					lines.add(url);
					maxBits = Math.max(maxBits, line.getBytes().length * 8);
					minBits = Math.min(minBits, line.getBytes().length * 8);
				}
				line = reader.readLine();
			}
			System.out.println("number of URLs (bad only = " + badOnly + "): " + lines.size());
			System.out.println("bits: " + minBits + ", " + maxBits);
			return lines;
		} catch (IOException e) {
			throw new IOException();
		} finally {
			reader.close();
		}
	}

	public static void main(String[] args) throws IOException {
		int m = 3941837;
		int k = 7;
		int bitsMin = 40; // 40 for data, 14 example
		int bitsMax = 18488; // 18488 for data, 23 example
		
		Random r = new SecureRandom();
		Set<String> badUrls = getUrls(true);
		Set<String> urls = getUrls(false);
//		final int noItems = 1 << 14;
//		final int noItems = badUrls.size();
//		final int NO_FALSE_POSITIVE_TESTS = badUrls.size();

		for (int log2bits = bitsMin; log2bits <= bitsMax; log2bits++) {
		  System.out.println("log2bits: " + log2bits);
		  
		  for (int noHashes = 6; noHashes <= k; noHashes++) {
			  System.out.println("log2bits: " + log2bits + ", noHashes: " + noHashes);
		    double noFalsePositives = 0;
		    int noNotIn = 0;

		    BloomFilter bf = new BloomFilterImpl(m, k, new HashFunctionFamilyImpl1(k));
		    Set<String> already = new HashSet<>();
		    // Add items to Bloom filter
		    for (String s : badUrls) {
		    		already.add(s);
		    		bf.add(s);
		    }
//		    for (int itemNo = 0; itemNo < urls.size(); itemNo++) {
//		      String s = randomString(r);
//		      already.add(s);
//		      bf.add(s);
//		    }
		    
		    // Now test for false positives
//		    for (int n = 0; n < NO_FALSE_POSITIVE_TESTS; n++) {
//		      String s = randomString(r);
		    for (String s: urls) {
		      if (!already.contains(s)) {
		        noNotIn++;
		        if (bf.contains(s)) noFalsePositives++;
		      }
		    }
		    double falsePositiveRate = noNotIn == 0 ? 0d :
		      noFalsePositives / noNotIn;
		    System.out.println("number not in: " + noNotIn + ", number false pos: " + noFalsePositives);
		    System.out.println("fpr: " + falsePositiveRate);
		    
		    FileOutputStream outputStream = new FileOutputStream("bfWrite.txt");
		    bf.writeTo(outputStream);
		  }
		}
	}
}
