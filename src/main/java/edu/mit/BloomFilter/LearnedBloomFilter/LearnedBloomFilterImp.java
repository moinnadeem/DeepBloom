package edu.mit.BloomFilter.LearnedBloomFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.mit.BloomFilter.OracleModel.OracleModel;
import edu.mit.BloomFilter.OracleModel.OracleModelImp;
import edu.mit.BloomFilter.StandardBloomFilter.StandardBloomFilter;
import edu.mit.BloomFilter.StandardBloomFilter.StandardBloomFilterImpl;

public class LearnedBloomFilterImp {
	
	public OracleModel learnedOracle;
	public StandardBloomFilter backupFilter;

	public LearnedBloomFilterImp() {
	    // empty constructor.
	}
	
	public void initAndLearn(File inputDataFile, double fprForBackupFilter) throws FileNotFoundException, IOException {
		
		// learn the oracle function
		learnedOracle = new OracleModelImp();
		try {
			learnedOracle.learn(inputDataFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

        // count the number of items that would be in backupFilter
        int numberOfItemsInBackupFilter = 0;
		try {
			numberOfItemsInBackupFilter = learnedOracle.getNumberOfFalseNegative(inputDataFile);
			System.out.println("numberOfItemsInBackupFilter:" + numberOfItemsInBackupFilter);
		} catch (Exception e) {
			e.printStackTrace();
        }

		System.out.println("numberOfFalseNegativeItems:" + numberOfItemsInBackupFilter);


		// initialize backup Filter
		String labelPositive = "good";
		String labelNegative = "bad";

		backupFilter = new StandardBloomFilterImpl(numberOfItemsInBackupFilter, fprForBackupFilter);
		// Go through the falseNegativeItemsFromLearnedOracleFile to add items to the backup filter
		int lineNo = 0;
		try(BufferedReader reader = new BufferedReader(new FileReader(inputDataFile))){
			String line;
			HashSet<String> set = learnedOracle.contains(inputDataFile, false);
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
                        if (!set.contains(url)) {
                            backupFilter.add(url);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
		} catch (Exception e) {
			e.printStackTrace();
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

	public List[] contains(File dataFile) {
        ArrayList<Boolean> classifications = new ArrayList<Boolean>();
		ArrayList<String> urls = new ArrayList();
        try(BufferedReader reader = new BufferedReader(new FileReader(dataFile))){
            HashSet<String> learnedURLs = learnedOracle.contains(dataFile, false);

            String line;
            while((line = reader.readLine())!= null) {
                // each line has a format: "url,bad/good"
                // line = line.substring(1, line.length()-1); // remove the double quotes at both ends
                int index = line.lastIndexOf(',');
                String url = line.substring(0, index);
                String label = line.substring(index+1, line.length());
                urls.add(url);
                if (learnedURLs.contains(url)) {
                    classifications.add(true);
                    continue;
                }

                classifications.add(backupFilter.contains(url));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return new List[] {urls, classifications};
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
		
		try {
			learnedOracle.save(learnedOracleFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		backupFilter.save(new File(backupFilterFile));
	}

	public void load(String learnedOracleFile, String backupFilterFile) throws IOException {
		
		learnedOracle = new OracleModelImp();
		try {
			learnedOracle.load(learnedOracleFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		backupFilter = new StandardBloomFilterImpl();
		backupFilter.load(new File(backupFilterFile));
	}
}
