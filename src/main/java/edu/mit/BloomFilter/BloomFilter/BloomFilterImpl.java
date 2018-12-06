package edu.mit.BloomFilter.BloomFilter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.BitSet;

public class BloomFilterImpl implements BloomFilter {

	private int m; // number of bits in the filter array
	private int k; // number of hash functions
	private BitSet filterArray;
	private int hashMask; // maximum index in the filter array, use to calculate the index into the filterArray
	private HashFunctionFamily hashFunctionFamily;
	
	public BloomFilterImpl(int n, double fpr) {
		// m = ceil((n * log(p)) / log(1 / pow(2, log(2))));
		// k = round((m / n) * log(2));
		this.m = (int)Math.ceil((n * Math.log(fpr)) / Math.log(1 / Math.pow(2, Math.log(2))));
		this.k = (int)Math.round(((double)m /(double) n) * Math.log(2));
		this.hashFunctionFamily = new HashFunctionFamilyImpl1(k);
		int log2ofM = (int)Math.ceil(Math.log(m)/Math.log(2));
		if (log2ofM > 31) {
			System.err.println("Overflow number, not supported");
			throw new IllegalArgumentException("m is too large");
		}
		
		this.hashMask = (1 << log2ofM) - 1;
		this.filterArray = new BitSet(1 << log2ofM);
		System.out.println("m: "+ this.m);
		System.out.println("k: "+ this.k);
//		System.out.println("hashMask: "+ String.format("0x%08X", this.hashMask));
	}
	
	public BloomFilterImpl(int m, int k) {
		this.m = m;
		this.k = k;
		this.hashFunctionFamily = new HashFunctionFamilyImpl1(k);
		int log2ofM = (int)Math.ceil(Math.log(m)/Math.log(2));
		if (log2ofM > 31) {
			System.err.println("Overflow number, not supported");
			throw new IllegalArgumentException("m is too large");
		}
		
		this.hashMask = (1 << log2ofM) - 1;
		this.filterArray = new BitSet(1 << log2ofM);
		System.out.println("m: "+ this.m);
		System.out.println("k: "+ this.k);
//		System.out.println("hashMask: "+ String.format("0x%08X", this.hashMask));
	}
	
	public BloomFilterImpl() {
		// Will need to load the filter from persistent storage
	}
	
	/*
	 * The return number must be from 0 to 2^m -1
	 */
	private int calculateBitNumber(int hashFunctionNumber, String s) {
		long hc = hashFunctionFamily.hashCode(hashFunctionNumber, s);
		int bitNumber = (int) (hc) & this.hashMask;
		return bitNumber;
	}

	@Override
	public void add(String s) {
		for (int i = 0; i < k; i++) {
			int bitNumber = calculateBitNumber(i, s);
			filterArray.set(bitNumber);
//			System.out.println("Set bit number "+ bitNumber);
		}
	}

	@Override
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

	@Override
	public void save(OutputStream out) {
		DataOutputStream dout = new DataOutputStream(out);
		try {
			dout.writeByte(m);
			dout.writeByte(k);
			dout.writeByte(hashMask);
			dout.writeInt(this.filterArray.length());
			for (int i = 0; i < this.filterArray.size(); i++) {
				dout.writeBoolean(this.filterArray.get(i));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void load(InputStream inputStream) {
		DataInputStream din = new DataInputStream(inputStream);
		try {
			m = din.readByte();
			k = din.readByte();
			hashFunctionFamily = new HashFunctionFamilyImpl1(k);
			hashMask = din.readByte();
			int lenFilterArray = din.readInt();
			this.filterArray = new BitSet(lenFilterArray);
			for (int i = 0; i < lenFilterArray; i++) {
				this.filterArray.set(i, din.readBoolean());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
