package edu.mit.BloomFilter.StandardBloomFilter;

import java.io.File;
import java.io.IOException;

public interface StandardBloomFilter {
	
	public void add(String s);

	public boolean contains(String s);
	
	public void save(File file) throws IOException;
	
	public void load(File file) throws IOException;
	
	public int getM(); // Get number of bits used in the filter array
	
}
