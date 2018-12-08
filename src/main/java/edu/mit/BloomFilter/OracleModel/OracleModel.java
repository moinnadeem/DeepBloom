package edu.mit.BloomFilter.OracleModel;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public interface OracleModel {
	
	
	public void learn(File inputFile, double fpr, File falseNegativeItemsOutputFile) throws Exception;
	
	public boolean classify(String s) throws Exception;
	
	public int getSize(); // return the number of bytes used in the model
	
	public String printSize();

	public void save(OutputStream outputStream) throws Exception;
	
	public void load(InputStream inputStream) throws Exception;
}
