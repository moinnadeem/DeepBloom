package edu.mit.BloomFilter.StandardBloomFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
		Assert.assertTrue("BloomFilter cannot have False Negative", (isContain == true));
		Assert.assertTrue("The chance this fails should be very rare", (bloomFilter.contains("Hello World 2") == false));
		Assert.assertTrue("The chance this fails should be very rare", (bloomFilter.contains("adfasdfa") == false));

		Assert.assertTrue("The size of the filter is incorrect", (bloomFilter.getM() == m));

	}
	
	@Test
	public void checkFilterHasZeroFalseNegative() {
		File inputDataFile = new File("model_training/data.csv");

		int n = 344821; // approximate number of elements in the dataset
		double fpr = 0.01;
		StandardBloomFilter filter = new StandardBloomFilterImpl(n, fpr);
	
		// Check that the filter has no false negative
		String labelPositive = "good";
		int numberOfItemsChecked = 0;
		try(BufferedReader reader = new BufferedReader(new FileReader(inputDataFile))){
			String line;
			while((line = reader.readLine())!= null) {
				// each line has a format: "url,bad/good"
				// line = line.substring(1, line.length()); // remove the double quotes at both ends
				int index = line.lastIndexOf(',');
				String url = line.substring(0, index);
				String label = line.substring(index+1, line.length());
				
				if (label.equals(labelPositive)) {
					numberOfItemsChecked++;
					filter.add(url);		
					boolean isContain = filter.contains(url);
					if (!isContain) {
						Assert.fail("False Negative: This cannot happen. Something is off. url:" + url);
					}
				}
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Assert.fail("Exception: "+ e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Exception: "+ e.getMessage());
		}
		
		Assert.assertTrue("No item checked", numberOfItemsChecked > 0);
	}
	
	@Test
	public void testSaveLoadFunctions() {
		
		int n = 100; // approximate number of elements in the dataset
		double fpr = 0.01;
		StandardBloomFilter bloomFilter = new StandardBloomFilterImpl(n, fpr);
		bloomFilter.add("Hello World");		
		
		StandardBloomFilter bloomFilter2 = new StandardBloomFilterImpl();
		File filename;
		try {
			filename = File.createTempFile("testSaveLoadFunctions_StandardBloomFilter","");
			bloomFilter.save(filename);
			bloomFilter2.load(filename);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Exception: "+ e.getMessage());
		}
		
		Assert.assertTrue("BloomFilter cannot have False Negative", (bloomFilter2.contains("Hello World") == true));
		Assert.assertTrue("The chance this fails should be very rare", (bloomFilter2.contains("Hello World 2") == false));

	}
}