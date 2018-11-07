package edu.mit.course6828.BloomFilter;

public interface BloomFilter {

	public void add(String s);

	public boolean contains(String s);
	
}
