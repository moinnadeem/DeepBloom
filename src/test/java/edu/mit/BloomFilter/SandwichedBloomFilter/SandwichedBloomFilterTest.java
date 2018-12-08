package edu.mit.BloomFilter.SandwichedBloomFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import edu.mit.BloomFilter.Utils.InputFileInfo;

public class SandwichedBloomFilterTest {
	
	@Test
	public void testBasicSandwichedBloomFilter() {
		
		try {
			InputFileInfo.printStatisticForInputFile(new File("model_training/data.csv"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SandwichedBloomFilterImp filter = new SandwichedBloomFilterImp();
		File inputDataFile = new File("model_training/data.csv");
		int approximateN = 350000;
		double fprForTheInitialFilter = 0.10;
		File falsePositiveItemsFromInitialFilterFile = new File("./falsePositiveItemsFromInitialFilterFile_sandwichedFilter");
		File falseNegativeItemsFromLearnedOracleFile = new File("./falseNegativeItemsFromLearnedOracleFile_sandwichedFilter");
		double fprForTheBackupFilter = 0.01;
		try {
			filter.initAndLearn(inputDataFile, approximateN, fprForTheInitialFilter, falsePositiveItemsFromInitialFilterFile, falseNegativeItemsFromLearnedOracleFile, fprForTheBackupFilter);
			
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Exception: "+ e.getMessage());
		}
	}
}
