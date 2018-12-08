package edu.mit.BloomFilter.StandardBloomFilter;

import java.io.InputStream;
import java.io.OutputStream;

public interface StandardBloomFilter {
	
	public void add(String s);

	public boolean contains(String s);
	
	public void save(OutputStream outputStream);
	
	public void load(InputStream inputStream);
	
	public int getM(); // Get number of bits used in the filter array
	
}
