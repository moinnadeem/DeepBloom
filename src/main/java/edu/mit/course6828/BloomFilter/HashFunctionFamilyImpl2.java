package edu.mit.course6828.BloomFilter;

public class HashFunctionFamilyImpl2 implements HashFunctionFamily {

	public long hashCode(int hashFunctionNumber, String s) {
		
		if (hashFunctionNumber < 0) {
			throw new IllegalArgumentException("hashFunctionNumber is invalid");			
		}
		return (s.hashCode() * (hashFunctionNumber + 1)); 
	}
	
}
