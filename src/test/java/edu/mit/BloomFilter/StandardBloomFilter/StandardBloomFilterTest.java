package edu.mit.BloomFilter.StandardBloomFilter;

import org.junit.Assert;
import org.junit.Test;

import edu.mit.BloomFilter.StandardBloomFilter.StandardBloomFilter;
import edu.mit.BloomFilter.StandardBloomFilter.StandardBloomFilterImpl;
import edu.mit.BloomFilter.StandardBloomFilter.StandardBloomFilterUtils;

public class StandardBloomFilterTest {
	
	@Test
	public void testBasicBloomFilter1() {
		
		int n = 1500000; // approximate number of elements in the dataset
		double fpr = 0.01;
		StandardBloomFilter bloomFilter = new StandardBloomFilterImpl(n, fpr);
		bloomFilter.add("Hello World");		
		boolean isContain = bloomFilter.contains("Hello World");
		Assert.assertTrue("BloomFilter cannot have False Negatives", (isContain == true));
		Assert.assertTrue("The chance this fails should be very rare", (bloomFilter.contains("Hello World 2") == false));
		Assert.assertTrue("The chance this fails should be very rare", (bloomFilter.contains("adfasdfa") == false));

		System.out.println("Size of the filter: " + bloomFilter.getM());

	}
	
	@Test
	public void testBasicBloomFilter2() {
		
		int n = 1500000; // approximate number of elements in the dataset
		int m = 15000000;
		int k = StandardBloomFilterUtils.calculateOptimalK(m, n);
		System.out.println("Value of optimal k is: "+ k);

		StandardBloomFilter bloomFilter = new StandardBloomFilterImpl(m, k);
		bloomFilter.add("Hello World");		
		boolean isContain = bloomFilter.contains("Hello World");
		Assert.assertTrue("BloomFilter cannot have False Negatives", (isContain == true));
		Assert.assertTrue("The chance this fails should be very rare", (bloomFilter.contains("Hello World 2") == false));
		Assert.assertTrue("The chance this fails should be very rare", (bloomFilter.contains("adfasdfa") == false));

		Assert.assertTrue("The size of the filter is incorrect", (bloomFilter.getM() == m));

	}
	
}