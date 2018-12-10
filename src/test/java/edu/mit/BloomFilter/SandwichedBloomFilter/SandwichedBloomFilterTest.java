package edu.mit.BloomFilter.SandwichedBloomFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import edu.mit.BloomFilter.OracleModel.OracleModel;
import edu.mit.BloomFilter.OracleModel.OracleModelImp;
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
		double fprForTheBackupFilter = 0.01;
		try {
			filter.initAndLearn(inputDataFile, approximateN, fprForTheInitialFilter, fprForTheBackupFilter);
			filter.save("./initial.pkl", "./learned", "./total");
			
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Exception: "+ e.getMessage());
		}
	}
	
	@Test
	public void checkFilterHasZeroFalseNegative() {
		
		File inputDataFile = new File("model_training/data.csv");

		// Learn and setup learned filter
		SandwichedBloomFilterImp filter = new SandwichedBloomFilterImp();
		int approximateN = 350000;
		double fprForTheInitialFilter = 0.10;
		double fprForTheBackupFilter = 0.01;
		try {
			filter.initAndLearn(inputDataFile, approximateN, fprForTheInitialFilter, fprForTheBackupFilter);
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

	@Test
	public void classificationTest() {
		File inputDataFile = new File("model_training/data.csv");
		OracleModel model = new OracleModelImp();
		try {
			model.learn(inputDataFile);
			model.save("full_model");
			model.classify("https://moinnadeem.com");
		} catch (Exception e) {
			System.out.println("An exception as occurred");
			e.printStackTrace();
		}
	}
}
