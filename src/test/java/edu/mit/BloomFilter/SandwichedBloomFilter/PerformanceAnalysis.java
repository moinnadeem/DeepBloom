package edu.mit.BloomFilter.SandwichedBloomFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Assert;

import edu.mit.BloomFilter.Utils.InputFileInfo;

public class PerformanceAnalysis {

	public static void main(String args[]) {
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
		SandwichedBloomFilterImp filter = new SandwichedBloomFilterImp();
		int approximateN = 344821;
		double fprForOracleModel = 0.05;
		File falsePositiveItemsFromInitialFilterFile = new File("./falsePositiveItemsFromInitialFilterFile_sandwichedFilter");
		File falseNegativeItemsFromLearnedOracleFile = new File("./falseNegativeItemsFromLearnedOracle_sandwichedFilter");
		double fprForBackupFilter = 0.01;
		try {
			filter.initAndLearn(inputDataFile, approximateN, fprForOracleModel, falsePositiveItemsFromInitialFilterFile, falseNegativeItemsFromLearnedOracleFile,
					fprForBackupFilter);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Exception: " + e.getMessage());
		}
		
		// Determining FPR and query time
		String labelPositive = "good";
		String labelNegative = "bad";
		int numberOfFalsePositiveItems = 0;
		int numberOfLinesWithWrongFormat = 0;
		int lineNo = 0;
		boolean isContain;
		long startTime;
		long totalQueryTime = 0;
		
		try(BufferedReader reader = new BufferedReader(new FileReader(inputDataFile))){
			String line;
			while((line = reader.readLine())!= null) {
				lineNo ++;
				// each line has a format: "url,bad/good"
				// line = line.substring(1, line.length()); // remove the double quotes at both ends
				int index = line.lastIndexOf(',');
				String url = line.substring(0, index);
				String label = line.substring(index+1, line.length());
				if (!(label.equals(labelPositive) || label.equals(labelNegative))) {
//					System.out.println("Wrong format for line #"+ lineNo + ". Line = " + line);
					numberOfLinesWithWrongFormat++;
					continue;
				}
				
				startTime = System.nanoTime();
				isContain = filter.contains(url);
				totalQueryTime += (System.nanoTime() - startTime);
				
				if (label.equals(labelPositive)) {
					if (!isContain) {
						System.err.println("False Negative: This cannot happen. Something is off: url=" + url);
					}
				}else if (label.equals(labelNegative)) {
					if (isContain) {
						numberOfFalsePositiveItems++;
//						System.out.println("False positive: "+ url);
					}
				}
				
			}
			System.out.println("numberOfFalsePositiveItems:" + numberOfFalsePositiveItems);
//			System.out.println("numberOfLinesWithWrongFormat:" + numberOfLinesWithWrongFormat);
			int numberOfValidItems = (lineNo-numberOfLinesWithWrongFormat);
			System.out.println("Total number of valid items:" + numberOfValidItems);
			System.out.println("FPR for the filter: " +  (double)numberOfFalsePositiveItems/(double)(numberOfValidItems));
			System.out.println("Size of the initial filter (in bytes): "+ filter.getSizeOfInitialFilter());
			System.out.println("Size of the oracle model (in bytes): "+ filter.getSizeOfOracleModel());
			System.out.println("Size of the backup filter (in bytes): "+ filter.getSizeOfBackupFilter());
			System.out.println("Size of the filter (in bytes): "+ filter.getSize());

//			System.out.println("Total query time (in nanosecond): "+ totalQueryTime);
			System.out.println("Average time per query (in nanosecond): "+ totalQueryTime/(double)(numberOfValidItems));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
