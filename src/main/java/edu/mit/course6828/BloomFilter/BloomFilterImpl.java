package edu.mit.course6828.BloomFilter;

import java.util.BitSet;

public class BloomFilterImpl implements BloomFilter {

	private final int m; // number of bits in the filter array
	private final int k; // number of hash functions
	private final BitSet filterArray;
	private final int hashMask; // maximum index in the filter array, use to calculate the index into the filterArray
	private HashFunctionFamily hashFunctionFamily;

	public BloomFilterImpl(int m, int k, HashFunctionFamily hashFunctionFamily) {
		this.m = m;
		this.k = k;
		int log2ofM = (int)Math.ceil(Math.log(m)/Math.log(2));
		if (log2ofM > 31) {
			System.err.println("Overflow number, not supported");
			throw new IllegalArgumentException("m is too large");
		}
		
		this.hashMask = (1 << log2ofM) - 1;
		this.filterArray = new BitSet(1 << log2ofM);
		this.hashFunctionFamily = hashFunctionFamily;
		System.out.println("m: "+ this.m);
		System.out.println("k: "+ this.k);
//		System.out.println("hashMask: "+ String.format("0x%08X", this.hashMask));
	}

	/*
	 * The return number must be from 0 to 2^m -1
	 */
	private int calculateBitNumber(int hashFunctionNumber, String s) {
		long hc = hashFunctionFamily.hashCode(hashFunctionNumber, s);
		int bitNumber = (int) (hc) & this.hashMask;
		return bitNumber;
	}

	public void add(String s) {
		for (int i = 0; i < k; i++) {
			int bitNumber = calculateBitNumber(i, s);
			filterArray.set(bitNumber);
//			System.out.println("Set bit number "+ bitNumber);
		}
	}

	public boolean contains(String s) {
		for (int i = 0; i < k; i++) {
			int bitNumber = calculateBitNumber(i, s);
//			System.out.println("Look in bit number "+ bitNumber);

			if (!filterArray.get(bitNumber)) {
				return false;
			}
		}
		return true;
	}

}
