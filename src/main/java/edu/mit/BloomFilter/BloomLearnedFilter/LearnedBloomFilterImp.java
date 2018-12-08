package edu.mit.BloomFilter.BloomLearnedFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.mit.BloomFilter.OracleModel.OracleModel;
import edu.mit.BloomFilter.OracleModel.OracleModelImpMock;
import edu.mit.BloomFilter.StandardBloomFilter.StandardBloomFilter;
import edu.mit.BloomFilter.StandardBloomFilter.StandardBloomFilterImpl;

public class LearnedBloomFilterImp {
	
	private OracleModel learnedOracle;
	private StandardBloomFilter backupFilter;

	public LearnedBloomFilterImp() {
		// will need to either call initAndLearn or load
	}
	
	public void initAndLearn(File inputDataFile, double fprForOracleModel, File falseNegativeItemsFromLearnedOracleFile, double fprForBackupFilter) throws FileNotFoundException, IOException {
		
		// learn the oracle function
		learnedOracle = new OracleModelImpMock();
		try {
			learnedOracle.learn(inputDataFile, fprForOracleModel, falseNegativeItemsFromLearnedOracleFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// count the number of lines in the falseNegativeItemsFromLearnedOracleFile
		int numberOfItemsInBackupFilter = 0;
		try(BufferedReader reader = new BufferedReader(new FileReader(falseNegativeItemsFromLearnedOracleFile))){
			while((reader.readLine())!= null) {
				numberOfItemsInBackupFilter++;
			}		
		}
		System.out.println("numberOfItemsInBackupFilter:" + numberOfItemsInBackupFilter);

		// initialize backup Filter
		backupFilter = new StandardBloomFilterImpl(numberOfItemsInBackupFilter, fprForBackupFilter);
		// Go through the falseNegativeItemsFromLearnedOracleFile to add items to the backup filter
		try(BufferedReader reader = new BufferedReader(new FileReader(falseNegativeItemsFromLearnedOracleFile))){
			String line;
			while((line = reader.readLine())!= null) {
				// each line has a format: url
				backupFilter.add(line);
			}		
		}	
	}

	public boolean contains(String s) {
		boolean isContain = false;
		try {
			isContain = learnedOracle.classify(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (isContain) {
			return true;
		}
		isContain = backupFilter.contains(s);
		return isContain;	
	}

	/**
	 * 
	 * @return size of backup filter (in byte)
	 */
	public int getSizeOfBackupFilter() {
		return backupFilter.getM()/8;
	}
	
	/**
	 * 
	 * @return size of backup filter (in byte)
	 */
	public int getSizeOfOracleModel() {
		return learnedOracle.getSize();
	}
	
	/**
	 * 
	 * @return total size of the filter
	 */
	public int getSize() {
		return getSizeOfBackupFilter() + getSizeOfOracleModel();
	}
	
	public void save(String learnedOracleFile, String backupFilterFile) throws IOException {
		
		OutputStream outputStream2 = new FileOutputStream(learnedOracleFile);
		try {
			learnedOracle.save(outputStream2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		outputStream2.close();
		
		OutputStream outputStream3 = new FileOutputStream(backupFilterFile);
		backupFilter.save(outputStream3);
		outputStream3.close();
		
	}

	public void load(String learnedOracleFile, String backupFilterFile) throws IOException {
		
		learnedOracle = new OracleModelImpMock();
		InputStream inputStream2 = new FileInputStream(learnedOracleFile);
		try {
			learnedOracle.load(inputStream2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		inputStream2.close();
		
		backupFilter = new StandardBloomFilterImpl();
		InputStream inputStream3 = new FileInputStream(backupFilterFile);
		backupFilter.load(inputStream3);
		inputStream3.close();
	}
}
