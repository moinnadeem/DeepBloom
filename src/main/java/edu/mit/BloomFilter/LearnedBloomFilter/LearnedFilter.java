package edu.mit.BloomFilter.LearnedBloomFilter;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public interface LearnedFilter {
	
	
	public void learn(File inputFile, double fpr, File outputFile) throws Exception;
	
	public boolean classify(String s) throws Exception;
	
	public String printSize();
	
	public void save(OutputStream outputStream) throws Exception;
	
	public void load(InputStream inputStream) throws Exception;
}
