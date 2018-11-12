package edu.mit.course6828.BloomFilter;

import java.io.OutputStream;

public interface BloomFilter {

	public void add(String s);

	public boolean contains(String s);
	
	public void writeTo(OutputStream out);
}
