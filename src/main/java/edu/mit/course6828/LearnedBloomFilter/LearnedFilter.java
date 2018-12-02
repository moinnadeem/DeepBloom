package edu.mit.course6828.LearnedBloomFilter;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public interface LearnedFilter {
	
	
	public void learn(File inputFile, double fpr, File outputFile);
	
	public boolean classify(String s);
	
	public String printSize();
	
	public void save(OutputStream outputStream);
	
	public void load(InputStream inputStream);
}
