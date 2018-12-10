package edu.mit.BloomFilter.OracleModel;

import java.io.File;
import java.util.HashSet;

public interface OracleModel {
	
	
	public void learn(File inputFile) throws Exception;
	
	public boolean classify(String s) throws Exception;
	
	public int getSize(); // return the number of bytes used in the model
	
	public String printSize();

	public void save(String file) throws Exception;
	
	public void load(String file) throws Exception;

	public int getNumberOfFalsePos(File f) throws Exception;

	public HashSet<String> getClassifications(boolean isTrue) throws Exception;
}
