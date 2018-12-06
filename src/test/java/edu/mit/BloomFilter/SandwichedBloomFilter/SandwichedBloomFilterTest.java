package edu.mit.BloomFilter.SandwichedBloomFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class SandwichedBloomFilterTest {
	
	@Test
	public void testBasicSandwichedBloomFilter() {
		
		try {
			SandwichedBloomFilterUtils.printStatisticForInputFile(new File("/home/moinnadeem/Documents/MIT/6.830/DeepBloom/DeepBloom/model_training/data.csv"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SandwichedBloomFilterImp filter = new SandwichedBloomFilterImp();
		File inputDataFile = new File("/home/moinnadeem/Documents/MIT/6.830/DeepBloom/DeepBloom/model_training/data.csv");
		int approximateN = 350000;
		double fpr = 0.05;
		File falsePositiveItemsFromInitialFilterFile = new File("./falsePositiveItemsFromInitialFilterFile");
		File negativeResultFromLearnedOracleFile = new File("./negativeResultFromLearnedOracleFile");
		try {
			filter.initAndLearn(inputDataFile, approximateN, fpr, falsePositiveItemsFromInitialFilterFile, negativeResultFromLearnedOracleFile);
			
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Exception: "+ e.getMessage());
		}
	}
}
