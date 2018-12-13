package edu.mit.BloomFilter.SandwichedBloomFilter;

import java.io.*;
import java.util.ArrayList;

import edu.mit.BloomFilter.OracleModel.OracleModel;
import edu.mit.BloomFilter.OracleModel.OracleModelImp;
import edu.mit.BloomFilter.StandardBloomFilter.StandardBloomFilter;
import edu.mit.BloomFilter.StandardBloomFilter.StandardBloomFilterImpl;
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
		double fprForTheInitialFilter = 0.01;
		double fprForTheBackupFilter = 0.01;

        System.out.println("FPR for initial: " + String.valueOf(fprForTheInitialFilter));

		try {
			filter.initAndLearn(inputDataFile, approximateN, fprForTheInitialFilter, fprForTheBackupFilter);
			filter.save("./initial.pkl", "./learned", "./total");
//            filter.load("./initial.pkl", "./learned", "./total");
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Exception: "+ e.getMessage());
		}


        String labelPositive = "good";
        String labelNegative = "bad";
        int numberOfFalsePositiveItems = 0;
        int numberOfLinesWithWrongFormat = 0;
        int numberOfItemsWithNegativeLabel = 0;
        int lineNo = -1;
        boolean isContain;
        long startTime;
        long totalQueryTime = 0;
        startTime = System.currentTimeMillis();
        ArrayList<Boolean> classifications = filter.contains(inputDataFile);
        totalQueryTime = System.currentTimeMillis() - startTime;
        try(BufferedReader reader = new BufferedReader(new FileReader(inputDataFile))){
            String line;
            while((line = reader.readLine())!= null) {
                lineNo++;
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

                isContain = classifications.get(lineNo);

                if (label.equals(labelPositive)) {
                    if (!isContain) {
                        System.err.println("False Negative: This cannot happen. Something is off: url=" + url);
                    }
                }else if (label.equals(labelNegative)) {
                    numberOfItemsWithNegativeLabel++;
                    if (isContain) {
                        numberOfFalsePositiveItems++;
                        //						System.out.println("False positive: "+ url);
                    }
                }

            }
            System.out.println("numberOfFalsePositiveItems:" + numberOfFalsePositiveItems);
            try {
                System.out.println("Number of false positives from filter: " + String.valueOf(filter.learnedOracle.getNumberOfFalsePos(inputDataFile)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("numberOfItemsWithNegativeLabel:" + numberOfItemsWithNegativeLabel);
            System.out.println("numberOfLinesWithWrongFormat:" + numberOfLinesWithWrongFormat);
            int numberOfValidItems = (lineNo-numberOfLinesWithWrongFormat);
            System.out.println("Total number of valid items:" + numberOfValidItems);
            System.out.println("FPR for the filter: " +  (double)numberOfFalsePositiveItems/(double)(numberOfItemsWithNegativeLabel));
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

	@Test
	public void testBasicSandwichedBloomFilterWorking() {
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
		double fprForTheInitialFilter = 0.25;
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

	@Test
	public void regularTest() {
	File inputDataFile = new File("model_training/data.csv");
    StandardBloomFilter model = new StandardBloomFilterImpl();
		try {
			int n1 = 430000;

            double fpr1 = 0.01;
            model = new StandardBloomFilterImpl(n1, fpr1);

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
                        model.add(url);
                        numberOfItemsInInitialFilter++;
                    }
                }
                totalLines =  lineNo;
            }
            System.out.println("numberOfItemsInInitialFilter:" + numberOfItemsInInitialFilter);

            model.save(new File("standardFilter"));

            long initialTime = System.nanoTime();
            model.contains("https://moinnadeem.com");
            System.out.println("Time taken to index in a regular filter: " + String.valueOf(System.nanoTime() - initialTime));
		} catch (Exception e) {
			System.out.println("An exception as occurred");
			e.printStackTrace();
		}
	}
}
