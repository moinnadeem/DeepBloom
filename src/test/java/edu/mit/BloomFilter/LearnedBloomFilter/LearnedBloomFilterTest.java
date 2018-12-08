package edu.mit.BloomFilter.LearnedBloomFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import edu.mit.BloomFilter.BloomLearnedFilter.LearnedBloomFilterImp;
import edu.mit.BloomFilter.Utils.InputFileInfo;

public class LearnedBloomFilterTest {
	
	@Test
	public void testBasicLearnedBloomFilter() {
		
		// print information for input file
		File inputDataFile = new File("model_training/data.csv");
		try {
			InputFileInfo.printStatisticForInputFile(inputDataFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Learn and setup learned filter
		LearnedBloomFilterImp filter = new LearnedBloomFilterImp();
		double fprForOracleModel = 0.05;
		File falseNegativeItemsFromLearnedOracleFile = new File("./falseNegativeItemsFromLearnedOracle");
		double fprForBackupFilter = 0.01;
		try {
			filter.initAndLearn(inputDataFile, fprForOracleModel, falseNegativeItemsFromLearnedOracleFile, fprForBackupFilter);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Exception: "+ e.getMessage());
		}
		
		// save the learned filter
		String learnedOracleFile = "learnedOracleFile2";
		String backupFilterFile = "backupFilterFile2";
		try {
			filter.save(learnedOracleFile, backupFilterFile);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Exception: "+ e.getMessage());
		}
		
		// load the learned filter
		LearnedBloomFilterImp filter2 = new LearnedBloomFilterImp();
		try {
			filter2.load(learnedOracleFile, backupFilterFile);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Exception: "+ e.getMessage());
		}
	}
}
