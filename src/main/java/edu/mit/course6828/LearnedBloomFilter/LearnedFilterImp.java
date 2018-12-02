package edu.mit.course6828.LearnedBloomFilter;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class LearnedFilterImp implements LearnedFilter{
	
	/** 
	 * Constructor.
	 */
	public LearnedFilterImp() {
		
	}
	
	public void learn(File inputFile, double fpr, File negativeResultOutputFile) {
		throw new UnsupportedOperationException();
	}
	
	public boolean classify(String url) {
		throw new UnsupportedOperationException();
	}
	
	public String printSize() {
		throw new UnsupportedOperationException();
	}
	
	public void save(OutputStream outputStream) {
		throw new UnsupportedOperationException();
	}
	
	public void load(InputStream inputStream) {
		throw new UnsupportedOperationException();
	}
}
