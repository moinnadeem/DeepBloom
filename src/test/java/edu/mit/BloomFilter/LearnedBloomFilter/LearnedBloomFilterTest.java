package edu.mit.BloomFilter.LearnedBloomFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

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
		double fprForBackupFilter = 0.01;
		try {
			filter.initAndLearn(inputDataFile, fprForOracleModel, fprForBackupFilter);
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

	@Test
	public void checkFilterHasZeroFalseNegative() {
		
		File inputDataFile = new File("model_training/data.csv");

		// Learn and setup learned filter
		LearnedBloomFilterImp filter = new LearnedBloomFilterImp();
		double fprForOracleModel = 0.05;
		double fprForBackupFilter = 0.01;
		try {
			filter.initAndLearn(inputDataFile, fprForOracleModel, fprForBackupFilter);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Exception: "+ e.getMessage());
		}
		
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
				
				boolean isContain = filter.contains(url);
				
				if (label.equals(labelPositive)) {
					numberOfItemsChecked++;
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
}
