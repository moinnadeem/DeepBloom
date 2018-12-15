package edu.mit.BloomFilter.LearnedBloomFilter;

import edu.mit.BloomFilter.LearnedBloomFilter.LearnedBloomFilterImp;
import edu.mit.BloomFilter.Utils.InputFileInfo;

import java.io.*;
import java.util.ArrayList;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import edu.mit.BloomFilter.OracleModel.OracleModel;
import edu.mit.BloomFilter.OracleModel.OracleModelImp;
import edu.mit.BloomFilter.StandardBloomFilter.StandardBloomFilter;
import edu.mit.BloomFilter.StandardBloomFilter.StandardBloomFilterImpl;
import org.junit.Assert;
import org.junit.Test;

import edu.mit.BloomFilter.Utils.InputFileInfo;

public class LearnedPerformanceTest {
    public static void main(String args[]) {
        System.out.println("\n" +
                " ██████╗     █████╗ ██████╗  ██████╗                                         \n" +
                "██╔════╝    ██╔══██╗╚════██╗██╔═████╗                                        \n" +
                "███████╗    ╚█████╔╝ █████╔╝██║██╔██║                                        \n" +
                "██╔═══██╗   ██╔══██╗ ╚═══██╗████╔╝██║                                        \n" +
                "╚██████╔╝██╗╚█████╔╝██████╔╝╚██████╔╝                                        \n" +
                " ╚═════╝ ╚═╝ ╚════╝ ╚═════╝  ╚═════╝                                         \n" +
                "                                                                             \n" +
                "                                                                             \n" +
                "                                                                             \n" +
                "██████╗ ███████╗███████╗██████╗ ██████╗ ██╗      ██████╗  ██████╗ ███╗   ███╗\n" +
                "██╔══██╗██╔════╝██╔════╝██╔══██╗██╔══██╗██║     ██╔═══██╗██╔═══██╗████╗ ████║\n" +
                "██║  ██║█████╗  █████╗  ██████╔╝██████╔╝██║     ██║   ██║██║   ██║██╔████╔██║\n" +
                "██║  ██║██╔══╝  ██╔══╝  ██╔═══╝ ██╔══██╗██║     ██║   ██║██║   ██║██║╚██╔╝██║\n" +
                "██████╔╝███████╗███████╗██║     ██████╔╝███████╗╚██████╔╝╚██████╔╝██║ ╚═╝ ██║\n" +
                "╚═════╝ ╚══════╝╚══════╝╚═╝     ╚═════╝ ╚══════╝ ╚═════╝  ╚═════╝ ╚═╝     ╚═╝\n" +
                "                                                                             \n");
		try {
			InputFileInfo.printStatisticForInputFile(new File("model_training/ez_data.csv"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		LearnedBloomFilterImp filter = new LearnedBloomFilterImp();
		File inputDataFile = new File("model_training/data.csv");
		int approximateN = 350000;
		double fprForTheBackupFilter = 0.01;

        System.out.println("FPR for backup: " + String.valueOf(fprForTheBackupFilter));

		try {
			filter.initAndLearn(inputDataFile, fprForTheBackupFilter);
			filter.save( "./standard_learned", "./backup_filter");
//            filter.load("./initial.pkl", "./learned", "./total");
		} catch (IOException e) {
			e.printStackTrace();
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
        totalQueryTime = System.currentTimeMillis() - startTime;
        File testDataFile = new File("model_training/data.csv");
        List[] filterSet = filter.contains(testDataFile);
        ArrayList<String> urls = (ArrayList<String>) filterSet[0];
        ArrayList<Boolean> classifications = (ArrayList<Boolean>) filterSet[1];
//        ArrayList<Boolean> classifications = filter.contains(testDataFile);
        try(BufferedReader reader = new BufferedReader(new FileReader(testDataFile))){
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

                if (!url.equals(urls.get(lineNo))) {
                    System.out.println("Something is wrong, fileURL=" + url + ", contains URL=" + urls.get(lineNo));
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
            System.out.println("FPR for the filter: " +  (((double)numberOfFalsePositiveItems)/((double)(numberOfItemsWithNegativeLabel))));
            System.out.println("Size of the oracle model (in bytes): "+ filter.getSizeOfOracleModel());
            System.out.println("Size of the backup filter (in bytes): "+ filter.getSizeOfBackupFilter());
            System.out.println("Size of the filter (in bytes): "+ filter.getSize());

            System.out.println("Total query time (in milliseconds): "+ totalQueryTime);
            System.out.println("Average time per query (in milliseconds): "+ totalQueryTime/(double)(numberOfValidItems));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
