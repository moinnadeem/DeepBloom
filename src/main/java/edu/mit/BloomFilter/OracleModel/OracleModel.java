package edu.mit.BloomFilter.OracleModel;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;

public interface OracleModel {
	
	
	public void learn(File inputFile, double fpr) throws Exception;
	
	public boolean classify(String s) throws Exception;
	
	public int getSize(); // return the number of bytes used in the model
	
	public String printSize();

	public void save(String s) throws Exception;
	
	public void load(String s) throws Exception;

	public int numFalsePositive(File f) throws Exception;

	public HashSet<String> getClassifications(boolean isTrue) throws Exception;
}
