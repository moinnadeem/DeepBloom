package edu.mit.course6828.SandwichedBloomFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class SandwichedBloomFilterTest {
	
	@Test
	public void testBasicSandwichedBloomFilter() {
		
		try {
			SandwichedBloomFilterUtils.printStatisticForInputFile(new File("/Users/edo/Dropbox (MIT)/6.830 Final Project/data.csv"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SandwichedBloomFilterImp filter = new SandwichedBloomFilterImp();
		File inputDataFile = new File("/Users/edo/Dropbox (MIT)/6.830 Final Project/data.csv");
		int approximateN = 350000;
		double fpr = 0.01;
		File falsePositiveItemsFromInitialFilterFile = new File("/Users/edo/tmp/falsePositiveItemsFromInitialFilterFile");
		File negativeResultFromLearnedOracleFile = new File("/Users/edo/tmp/negativeResultFromLearnedOracleFile");
		try {
			filter.initAndLearn(inputDataFile, approximateN, fpr, falsePositiveItemsFromInitialFilterFile, negativeResultFromLearnedOracleFile);
			
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Exception: "+ e.getMessage());
		}
	}
}
