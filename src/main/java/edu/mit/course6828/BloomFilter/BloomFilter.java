package edu.mit.course6828.BloomFilter;

import java.io.InputStream;
import java.io.OutputStream;

public interface BloomFilter {
	
	public void add(String s);

	public boolean contains(String s);
	
	public void save(OutputStream outputStream);
	
	public void load(InputStream inputStream);
	
}
