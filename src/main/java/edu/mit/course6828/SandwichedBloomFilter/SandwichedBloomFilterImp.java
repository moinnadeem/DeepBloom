package edu.mit.course6828.SandwichedBloomFilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.mit.course6828.BloomFilter.BloomFilter;
import edu.mit.course6828.BloomFilter.BloomFilterImpl;
import edu.mit.course6828.LearnedBloomFilter.LearnedFilter;
import edu.mit.course6828.LearnedBloomFilter.LearnedFilterImp;

public class SandwichedBloomFilterImp implements SandwichedBloomFilter{
	
	private BloomFilter initialFilter;
	private LearnedFilter learnedOracle;
	private BloomFilter backupFilter;
	
	public SandwichedBloomFilterImp() {
		// will need to either call initAndLearn or load
	}
	
	public void initAndLearn(File inputDataFile, int approximateN, double fpr, File falsePositiveItemsFromInitialFilterFile, File negativeResultFromLearnedOracleFile) throws FileNotFoundException, IOException {
		int n1 = approximateN;
		double fpr1 = fpr; // Need to change
		initialFilter = new BloomFilterImpl(n1, fpr1);
		
		String labelNegative = "bad";
		String labelPositive = "good";
		
		// Go through the input file to add items to the initial filter
		int numberOfItemsInInitialFilter = 0;
		try(BufferedReader reader = new BufferedReader(new FileReader(inputDataFile))){
			int lineNo = 0;
			String line;
			while((line = reader.readLine())!= null) {
				lineNo ++;
				// each line has a format: "url,bad/good"
				line = line.substring(1, line.length()-1); // remove the double quotes at both ends
				int index = line.lastIndexOf(',');
				String url = line.substring(0, index);
				String label = line.substring(index+1, line.length());

				if (!label.equals(labelPositive) && !label.equals(labelNegative)) {
					System.out.println("Wrong format for line #"+ lineNo + ". Line = " + line);
					continue;
				}
				if (label.equals(labelPositive)) {
					initialFilter.add(url);
					numberOfItemsInInitialFilter++;
				}	
			}		
		}
		System.out.println("numberOfItemsInInitialFilter:" + numberOfItemsInInitialFilter);
		
		
		// Go through the input file again to find false positive items from the initial filter
		int numberOfFalsePositiveItemsFromTheInitialFilter = 0;
		try(BufferedReader reader = new BufferedReader(new FileReader(inputDataFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(falsePositiveItemsFromInitialFilterFile))){
			String line;
			while((line = reader.readLine())!= null) {
				// each line has a format: "url,good"
				line = line.substring(1, line.length()-1); // remove the double quotes at both ends
				int index = line.lastIndexOf(',');
				String url = line.substring(0, index);
				String label = line.substring(index+1, line.length());
				
				if (label.equals(labelNegative)) {
					if (initialFilter.contains(url)) {
						writer.write(url+"\n");
						numberOfFalsePositiveItemsFromTheInitialFilter++;
					}
				}
			}		
		}
		System.out.println("numberOfFalsePositiveItemsFromTheInitialFilter:" + numberOfFalsePositiveItemsFromTheInitialFilter);
		
		// initialize learnedOracle
		learnedOracle = new LearnedFilterImp();
		double fpr2 = fpr; // Need to change
		learnedOracle.learn(falsePositiveItemsFromInitialFilterFile, fpr2, negativeResultFromLearnedOracleFile);
		
		// count the number of lines in the negativeResultFromLearnedOracleFile
		int numberOfItemsInBackupFilter = 0;
		try(BufferedReader reader = new BufferedReader(new FileReader(negativeResultFromLearnedOracleFile))){
			String line;
			while((line = reader.readLine())!= null) {
				numberOfItemsInBackupFilter++;
			}		
		}
		System.out.println("numberOfItemsInBackupFilter:" + numberOfItemsInBackupFilter);

		// initialize backup Filter
		int n3 = numberOfItemsInBackupFilter;
		double fpr3 = fpr; // Need to change
		backupFilter = new BloomFilterImpl(n3, fpr3);
		// Go through the negativeResultFromLearnedOracleFile to add items to the backup filter
		try(BufferedReader reader = new BufferedReader(new FileReader(negativeResultFromLearnedOracleFile))){
			String line;
			while((line = reader.readLine())!= null) {
				// each line has a format: url
				backupFilter.add(line);
			}		
		}	
	}

	@Override
	public boolean contains(String s) {
		boolean isContain = initialFilter.contains(s);
		if (!isContain) {
			return false;
		}
		isContain = learnedOracle.classify(s);
		if (isContain) {
			return true;
		}
		isContain = backupFilter.contains(s);
		return isContain;	
	}

	@Override
	public void save(String initialFilterFile, String learnedOracleFile, String backupFilterFile) throws IOException {
		OutputStream outputStream1 = new FileOutputStream(initialFilterFile);
		initialFilter.save(outputStream1);
		outputStream1.close();
		
		OutputStream outputStream2 = new FileOutputStream(learnedOracleFile);
		learnedOracle.save(outputStream2);
		outputStream2.close();
		
		OutputStream outputStream3 = new FileOutputStream(backupFilterFile);
		backupFilter.save(outputStream3);
		outputStream3.close();
		
	}

	@Override
	public void load(String initialFilterFile, String learnedOracleFile, String backupFilterFile) throws IOException {
		initialFilter = new BloomFilterImpl();
		InputStream inputStream1 = new FileInputStream(initialFilterFile);
		initialFilter.load(inputStream1);
		inputStream1.close();
		
		learnedOracle = new LearnedFilterImp();
		InputStream inputStream2 = new FileInputStream(learnedOracleFile);
		learnedOracle.load(inputStream2);
		inputStream2.close();
		
		backupFilter = new BloomFilterImpl();
		InputStream inputStream3 = new FileInputStream(backupFilterFile);
		backupFilter.load(inputStream3);
		inputStream3.close();
	}
	
}
