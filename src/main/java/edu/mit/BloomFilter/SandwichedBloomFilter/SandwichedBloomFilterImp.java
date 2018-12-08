package edu.mit.BloomFilter.SandwichedBloomFilter;

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

import edu.mit.BloomFilter.OracleModel.OracleModel;
import edu.mit.BloomFilter.OracleModel.OracleModelImp;
import edu.mit.BloomFilter.OracleModel.OracleModelImpMock;
import edu.mit.BloomFilter.StandardBloomFilter.StandardBloomFilter;
import edu.mit.BloomFilter.StandardBloomFilter.StandardBloomFilterImpl;

public class SandwichedBloomFilterImp{
	
	private StandardBloomFilter initialFilter;
	private OracleModel learnedOracle;
	private StandardBloomFilter backupFilter;

	public SandwichedBloomFilterImp() {
		// will need to either call initAndLearn or load
	}
	
	public void initAndLearn(File inputDataFile, int approximateN, double fprForTheInitialFilter, File falsePositiveItemsFromInitialFilterFile, File falseNegativeItemsFromLearnedOracleFile, double fprForTheBackupFilter) throws FileNotFoundException, IOException {
		int n1 = approximateN;
		double fpr1 = fprForTheInitialFilter; 
		initialFilter = new StandardBloomFilterImpl(n1, fpr1);
		
		String labelPositive = "good";
		String labelNegative = "bad";
		
		// Go through the input file to add items to the initial filter
		int numberOfItemsInInitialFilter = 0;
		try(BufferedReader reader = new BufferedReader(new FileReader(inputDataFile))){
			int lineNo = 0;
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
				// line = line.substring(1, line.length()-1); // remove the double quotes at both ends
				int index = line.lastIndexOf(',');
				String url = line.substring(0, index);
				String label = line.substring(index+1, line.length());
				
				if (label.equals(labelNegative)) {
					if (initialFilter.contains(url)) {
//						url = url.replace("%","");
//						writer.write(url+"," + label + "\n");
						writer.write(url+"\n");
						numberOfFalsePositiveItemsFromTheInitialFilter++;
					}
				}
			}		
		}
		System.out.println("numberOfFalsePositiveItemsFromTheInitialFilter:" + numberOfFalsePositiveItemsFromTheInitialFilter);
		
		// learn the oracle function
		learnedOracle = new OracleModelImpMock();
		double fpr2 = fprForTheInitialFilter; // Need to change
		try {
			learnedOracle.learn(inputDataFile, fpr2, falseNegativeItemsFromLearnedOracleFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// count the number of items that would be in backupFilter 
		int numberOfItemsInBackupFilter = 0;
		try(BufferedReader reader = new BufferedReader(new FileReader(inputDataFile))){
			String line;
			while((line = reader.readLine())!= null) {
				// each line has a format: "url,bad/good"
				// line = line.substring(1, line.length()-1); // remove the double quotes at both ends
				int index = line.lastIndexOf(',');
				String url = line.substring(0, index);
				String label = line.substring(index+1, line.length());

				if (label.equals(labelPositive)) {
					try {
						if (learnedOracle.classify(url) == false) {
							numberOfItemsInBackupFilter++;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
			}	
		}	
		System.out.println("numberOfItemsInBackupFilter:" + numberOfItemsInBackupFilter);

		// initialize backup Filter
		int n3 = numberOfItemsInBackupFilter;
		double fpr3 = fprForTheBackupFilter;
		backupFilter = new StandardBloomFilterImpl(n3, fpr3);
		// Go through the input file to add items to the backup filter
		try(BufferedReader reader = new BufferedReader(new FileReader(inputDataFile))){
			String line;
			while((line = reader.readLine())!= null) {
				// each line has a format: "url,bad/good"
				// line = line.substring(1, line.length()-1); // remove the double quotes at both ends
				int index = line.lastIndexOf(',');
				String url = line.substring(0, index);
				String label = line.substring(index+1, line.length());

				if (!label.equals(labelPositive) && !label.equals(labelNegative)) {
//					System.out.println("Wrong format for line #"+ lineNo + ". Line = " + line);
					continue;
				}
				if (label.equals(labelPositive)) {
					try {
						if (learnedOracle.classify(url) == false) {
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
		boolean isContain = initialFilter.contains(s);
		if (!isContain) {
			return false;
		}
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
	 * @return size of the initial filter (in byte)
	 */
	public int getSizeOfInitialFilter() {
		return initialFilter.getM()/8;
	}

	/**
	 * 
	 * @return size of the backup filter (in byte)
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
		return getSizeOfInitialFilter() + getSizeOfOracleModel() + getSizeOfBackupFilter();
	}
	

	public void save(String initialFilterFile, String learnedOracleFile, String backupFilterFile) throws IOException {
		OutputStream outputStream1 = new FileOutputStream(initialFilterFile);
		initialFilter.save(outputStream1);
		outputStream1.close();
		
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

	public void load(String initialFilterFile, String learnedOracleFile, String backupFilterFile) throws IOException {
		initialFilter = new StandardBloomFilterImpl();
		InputStream inputStream1 = new FileInputStream(initialFilterFile);
		initialFilter.load(inputStream1);
		inputStream1.close();
		
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
