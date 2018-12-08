package edu.mit.BloomFilter.OracleModel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;

public class OracleModelImpMock implements OracleModel{

	/** 
	 * Constructor.
	 */
	public OracleModelImpMock() {
	}
	
	public void learn(File inputDataFile, double fpr, File falseNegativeItemsOutputFile) throws Exception {
		String labelPositive = "good";
		String labelNegative = "bad";
		
		int numberOfFalseNegativeItems = 0;
		int lineNo = 0;
		try(BufferedReader reader = new BufferedReader(new FileReader(inputDataFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(falseNegativeItemsOutputFile))){
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
					if (url.hashCode() %2 !=0) { // false Negative item
						writer.write(url+"\n");
						numberOfFalseNegativeItems++;
					}
				}	
			}		
		}
		System.out.println("numberOfLines: " + lineNo);
		System.out.println("numberOfFalseNegativeItems: " + numberOfFalseNegativeItems);

	}
	
	public boolean classify(String url) throws Exception {
		return url.hashCode() % 2 == 0;
	}


	public String printSize() {
		throw new java.lang.UnsupportedOperationException("Not supported yet.");
	}
	
	public void save(OutputStream outputStream) throws Exception {
	}

	public void load(InputStream inputStream) throws Exception {
	}

	@Override
	public int getSize() {
		return 0;
	}
}
