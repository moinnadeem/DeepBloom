package edu.mit.BloomFilter.SandwichedBloomFilter;

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

import edu.mit.BloomFilter.OracleModel.OracleModel;
import edu.mit.BloomFilter.OracleModel.OracleModelImpMock;
import edu.mit.BloomFilter.StandardBloomFilter.StandardBloomFilter;
import edu.mit.BloomFilter.StandardBloomFilter.StandardBloomFilterImpl;

public class SandwichedBloomFilterImp{
	
	public StandardBloomFilter initialFilter;
	public OracleModel learnedOracle;
	public StandardBloomFilter backupFilter;

	public SandwichedBloomFilterImp() {
		// will need to either call initAndLearn or load
	}
	
	public void initAndLearn(File inputDataFile, int approximateN, double fprForTheInitialFilter, double fprForTheBackupFilter) throws IOException {
		int n1 = approximateN;
		double fpr1 = fprForTheInitialFilter; 
		initialFilter = new StandardBloomFilterImpl(n1, fpr1);
		
		String labelPositive = "good";
		String labelNegative = "bad";
		int totalLines = 0;
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
			totalLines =  lineNo;
		}
		System.out.println("numberOfItemsInInitialFilter:" + numberOfItemsInInitialFilter);
		
		
		// Go through the input file again to find false positive items from the initial filter
		int numberOfFalsePositiveItemsFromTheInitialFilter = 0;
		try(BufferedReader reader = new BufferedReader(new FileReader(inputDataFile));){
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
//						writer.write(url+"\n");
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

		//try(BufferedReader reader = new BufferedReader(new FileReader(inputDataFile))){
		//	String line;
		//	int lineNo = 0;
		//	while((line = reader.readLine())!= null) {
		//		lineNo += 1;
		//		// each line has a format: "url,bad/good"
		//		// line = line.substring(1, line.length()-1); // remove the double quotes at both ends
		//		int index = line.lastIndexOf(',');
		//		String url = line.substring(0, index);
		//		String label = line.substring(index+1, line.length());

		//		if (label.equals(labelPositive)) {
		//			try {
		//				if (learnedOracle.classify(url) == false) {  // not in set, implies a label of 1
		//					numberOfItemsInBackupFilter++;
		//				}
		//			} catch (Exception e) {
		//				e.printStackTrace();
		//			}
		//		}
		//	}
		//}

		// initialize backup Filter
		int n3 = numberOfItemsInBackupFilter;
		double fpr3 = fprForTheBackupFilter;
		backupFilter = new StandardBloomFilterImpl(n3, fpr3);
		// Go through the input file to add items to the backup filter
		try {
            try(BufferedReader reader = new BufferedReader(new FileReader(inputDataFile))){
                String line;
                HashSet<String> set = learnedOracle.classifyFile(inputDataFile, false);
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
            }
		} catch (Exception e) {
			e.printStackTrace();
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

	public ArrayList<Boolean> contains(File inputDataFile) {
        ArrayList<Boolean> classifications = new ArrayList<Boolean>();

        try(BufferedReader reader = new BufferedReader(new FileReader(inputDataFile))){
            HashSet<String> learnedURLs = learnedOracle.classifyFile(inputDataFile, false);

            String line;
            while((line = reader.readLine())!= null) {
                // each line has a format: "url,bad/good"
                // line = line.substring(1, line.length()-1); // remove the double quotes at both ends
                int index = line.lastIndexOf(',');
                String url = line.substring(0, index);
                String label = line.substring(index+1, line.length());

                if (!initialFilter.contains(url)) {
                    classifications.add(false);
                    continue;
                }

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

        return classifications;
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
		initialFilter.save(new File(initialFilterFile));
		
		try {
			learnedOracle.save(learnedOracleFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		backupFilter.save(new File(backupFilterFile));
		
	}

	public void load(String initialFilterFile, String learnedOracleFile, String backupFilterFile) throws IOException {
		initialFilter = new StandardBloomFilterImpl();
		initialFilter.load(new File(initialFilterFile));
		
		learnedOracle = new OracleModelImpMock();
		try {
			learnedOracle.load(learnedOracleFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		backupFilter = new StandardBloomFilterImpl();
		backupFilter.load(new File(backupFilterFile));
	}
	
}
