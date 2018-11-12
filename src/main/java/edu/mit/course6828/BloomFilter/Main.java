package edu.mit.course6828.BloomFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Main {
	
	public static Set<String> getBadUrls() throws IOException {
		File file = new File("model_training/data.csv");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		Set<String> lines = new HashSet<>();
		int maxBits = Integer.MIN_VALUE;
		int minBits = Integer.MAX_VALUE;
		System.out.println("start");
		try {
			System.out.println("reading");
			String line = reader.readLine();
			
			while(line != null) {
				if (line.endsWith("bad")) {
					lines.add(line);
					maxBits = Math.max(maxBits, line.getBytes().length * 8);
					minBits = Math.min(minBits, line.getBytes().length * 8);
				}
				line = reader.readLine();
			}
//			System.out.println("bits: " + minBits + ", " + maxBits);
			return lines;
		} catch (IOException e) {
			throw new IOException();
		} finally {
			reader.close();
		}
		
	}
	
	private static final String LETTERS =
			  "abcdefghijklmnopqrstuvexyABCDEFGHIJKLMNOPQRSTUVWYXZ+=-_@%&";
	private static String randomString(Random r) {
	  int wordLen;
	  do {
	    wordLen = 5 + 2 * (int) (r.nextGaussian() + 0.5d);
	  } while (wordLen < 1 || wordLen > 12);
	  StringBuilder sb = new StringBuilder(wordLen);
	  for (int i = 0; i < wordLen; i++) {
	    char ch = LETTERS.charAt(r.nextInt(LETTERS.length()));
	    sb.append(ch);
	  }
	  return new String(sb);
	}
	
	public static void main(String[] args) throws IOException {
		int m = 6000000;
		int k = 8;
		int bitsMin = 5; // 40 for data, 14 example
		int bitsMax = 15; // 18488 for data, 23 example
		
		Random r = new SecureRandom();
		Set<String> urls = getBadUrls();
//		final int noItems = 1 << 14;
		final int noItems = urls.size();
		final int NO_FALSE_POSITIVE_TESTS = urls.size();

		for (int log2bits = bitsMin; log2bits <= bitsMax; log2bits++) {
			System.out.println("log2bits: " + log2bits);
		  for (int noHashes = 1; noHashes <= k; noHashes++) {
		    double noFalsePositives = 0;
		    int noNotIn = 0;

		    BloomFilter bf = new BloomFilterImpl(m, k, new HashFunctionFamilyImpl1(k));
		    Set<String> already = new HashSet<>();
		    // Add items to Bloom filter
		    for (String s : urls) {
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
//		    System.out.println(noNotIn + ", " + noFalsePositives);
		    System.out.println(falsePositiveRate);
		  }
		}
	}
}
