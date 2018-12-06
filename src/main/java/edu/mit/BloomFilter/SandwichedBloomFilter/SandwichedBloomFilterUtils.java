package edu.mit.BloomFilter.SandwichedBloomFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SandwichedBloomFilterUtils {

	public static void printStatisticForInputFile(File inputDataFile) throws FileNotFoundException, IOException {
		
		String labelNegative = "bad";
		String labelPositive = "good";
		
		int numberOfItemsWithLabelNegative = 0;
		int numberOfItemsWithLabelPositive = 0;
		int numberOfLinesWithWrongFormat = 0;
		int lineNo = 0;

		try(BufferedReader reader = new BufferedReader(new FileReader(inputDataFile))){
			String line;
			while((line = reader.readLine())!= null) {
				lineNo ++;
				// each line has a format: "url,bad/good"
				// line = line.substring(1, line.length()); // remove the double quotes at both ends
				int index = line.lastIndexOf(',');
//				String url = line.substring(0, index);
				String label = line.substring(index+1, line.length());
				if (!(label.equals(labelPositive) || label.equals(labelNegative))) {
					System.out.println("Wrong format for line #"+ lineNo + ". Line = " + line);
					numberOfLinesWithWrongFormat++;
					continue;
				}
				if (label.equals(labelPositive)) {
					numberOfItemsWithLabelPositive++;
				}else if (label.equals(labelNegative)) {
					numberOfItemsWithLabelNegative++;
				}
				
			}		
		}
		System.out.println("numberOfItemsWithLabelPositive:" + numberOfItemsWithLabelPositive);
		System.out.println("numberOfItemsWithLabelNegative:" + numberOfItemsWithLabelNegative);
		System.out.println("numberOfLinesWithWrongFormat:" + numberOfLinesWithWrongFormat);
		System.out.println("Total number of lines:" + lineNo);

	}
	
}
