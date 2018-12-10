package edu.mit.BloomFilter.StandardBloomFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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

		// Set up the filter
		int n = 344821; // approximate number of elements in the dataset
		int m = 1656902;
		int k = StandardBloomFilterUtils.calculateOptimalK(m, n);
		System.out.println("Value of optimal k is: " + k);
		StandardBloomFilter filter = new StandardBloomFilterImpl(m, k);

		String labelPositive = "good";
		String labelNegative = "bad";
		int lineNo = 0;
		try (BufferedReader reader = new BufferedReader(new FileReader(inputDataFile))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lineNo++;
				// each line has a format: "url,bad/good"
				// line = line.substring(1, line.length()); // remove the double quotes at both
				// ends
				int index = line.lastIndexOf(',');
				String url = line.substring(0, index);
				String label = line.substring(index + 1, line.length());
				if (!(label.equals(labelPositive) || label.equals(labelNegative))) {
					// System.out.println("Wrong format for line #"+ lineNo + ". Line = " + line);
					continue;
				}
				if (label.equals(labelPositive)) {
					filter.add(url);
				}
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Determining FPR and query time
		int numberOfFalsePositiveItems = 0;
		int numberOfLinesWithWrongFormat = 0;
		int numberOfItemsWithNegativeLabel = 0;
		lineNo = 0;
		boolean isContain;
		long startTime;
		long totalQueryTime = 0;

		try (BufferedReader reader2 = new BufferedReader(new FileReader(inputDataFile))) {
			String line;
			while ((line = reader2.readLine()) != null) {
				lineNo++;
				// each line has a format: "url,bad/good"
				// line = line.substring(1, line.length()); // remove the double quotes at both
				// ends
				int index = line.lastIndexOf(',');
				String url = line.substring(0, index);
				String label = line.substring(index + 1, line.length());
				if (!(label.equals(labelPositive) || label.equals(labelNegative))) {
					// System.out.println("Wrong format for line #"+ lineNo + ". Line = " + line);
					numberOfLinesWithWrongFormat++;
					continue;
				}

				startTime = System.nanoTime();
				isContain = filter.contains(url);
				totalQueryTime += (System.nanoTime() - startTime);

				if (label.equals(labelPositive)) {
					if (!isContain) {
						System.err.println("False Negative: This cannot happen. Something is off");
					}
				} else if (label.equals(labelNegative)) {
					numberOfItemsWithNegativeLabel++;
					if (isContain) {
						numberOfFalsePositiveItems++;
						// System.out.println("False positive: "+ url);
					}
				}

			}
			System.out.println("numberOfFalsePositiveItems:" + numberOfFalsePositiveItems);
			System.out.println("numberOfItemsWithNegativeLabel:" + numberOfItemsWithNegativeLabel);
			// System.out.println("numberOfLinesWithWrongFormat:" +
			// numberOfLinesWithWrongFormat);
			int numberOfValidItems = (lineNo - numberOfLinesWithWrongFormat);
			System.out.println("Total number of valid items:" + numberOfValidItems);
			System.out.println("FPR for the filter: " + (double) numberOfFalsePositiveItems / (double) (numberOfItemsWithNegativeLabel));
			System.out.println("Size of the filter (in bytes): " + (filter.getM() / 8));

//			System.out.println("Total query time (in nanosecond): " + totalQueryTime);
			System.out.println(
					"Average time per query (in nanosecond): " + totalQueryTime / (double) (numberOfValidItems));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
