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
import edu.mit.BloomFilter.OracleModel.OracleModelImp;
import edu.mit.BloomFilter.OracleModel.OracleModelImpMock;
import edu.mit.BloomFilter.StandardBloomFilter.StandardBloomFilter;
import edu.mit.BloomFilter.StandardBloomFilter.StandardBloomFilterImpl;

public class LearnedBloomFilterImp {
	
	private OracleModel learnedOracle;
	private StandardBloomFilter backupFilter;

	public LearnedBloomFilterImp() {
		// will need to either call initAndLearn or load
	}
	
	public void initAndLearn(File inputDataFile, double fprForOracleModel, double fprForBackupFilter) throws FileNotFoundException, IOException {
		
		// learn the oracle function
		learnedOracle = new OracleModelImp();
		try {
			learnedOracle.learn(inputDataFile, fprForOracleModel);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// count number of false Negative Items
		String labelPositive = "good";
		String labelNegative = "bad";
		int numberOfFalseNegativeItems = 0;
		int lineNo = 0;
		try(BufferedReader reader = new BufferedReader(new FileReader(inputDataFile))){
			String line;
			while((line = reader.readLine())!= null) {
				lineNo ++;
				// each line has a format: "url,bad/good"
				// line = line.substring(1, line.length()-1); // remove the double quotes at both ends
				int index = line.lastIndexOf(',');
				String url = line.substring(0, index);
				String label = line.substring(index+1, line.length());

				if (!label.equals(labelPositive) && !label.equals(labelNegative)) {
					System.out.println("Wrong format for line #"+ lineNo + ". Line = " + line);
					continue;
				}
				if (label.equals(labelPositive)) {
					try {
						if (!learnedOracle.classify(url)) {
							numberOfFalseNegativeItems++;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
			}		
		}
		System.out.println("numberOfFalseNegativeItems:" + numberOfFalseNegativeItems);

		// initialize backup Filter
		backupFilter = new StandardBloomFilterImpl(numberOfFalseNegativeItems, fprForBackupFilter);
		// Go through the falseNegativeItemsFromLearnedOracleFile to add items to the backup filter
		lineNo = 0;
		try(BufferedReader reader = new BufferedReader(new FileReader(inputDataFile))){
			String line;
			while((line = reader.readLine())!= null) {
				lineNo ++;
				// each line has a format: "url,bad/good"
				// line = line.substring(1, line.length()-1); // remove the double quotes at both ends
				int index = line.lastIndexOf(',');
				String url = line.substring(0, index);
				String label = line.substring(index+1, line.length());

				if (label.equals(labelPositive)) {
					try {
						if (!learnedOracle.classify(url)) {
							backupFilter.add(url);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
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
		
		learnedOracle = new OracleModelImp();
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
