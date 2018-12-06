package edu.mit.BloomFilter.BloomFilter;

public class HashFunctionFamilyImpl1 implements HashFunctionFamily {

	private final int maxNumberOfHashFunction;
	private final long[] byteTable;
	private final long HSTART = 0xBB40E64DA205B064L;
	private final long HMULT = 7664345821815920749L;

	public HashFunctionFamilyImpl1(int maxNumberOfHashFunction) {
		this.maxNumberOfHashFunction = maxNumberOfHashFunction;
		byteTable = new long[256 * this.maxNumberOfHashFunction];
		long h = 0x544B2FBACAAF1684L;
		for (int i = 0; i < byteTable.length; i++) {
			for (int j = 0; j < 31; j++)
				h = (h >>> 7) ^ h;
			h = (h << 11) ^ h;
			h = (h >>> 10) ^ h;
			byteTable[i] = h;
		}
	}

	public long hashCode(int hashFunctionNumber, String s) {
		if (hashFunctionNumber < 0 || hashFunctionNumber > maxNumberOfHashFunction) {
			throw new IllegalArgumentException("hashFunctionNumber is invalid");			
		}
		long h = HSTART;
		final long hmult = HMULT;
		final long[] ht = byteTable;
		int startIx = 256 * hashFunctionNumber;
		for (int len = s.length(), i = 0; i < len; i++) {
			char ch = s.charAt(i);
			h = (h * hmult) ^ ht[startIx + (ch & 0xff)];
			h = (h * hmult) ^ ht[startIx + ((ch >>> 8) & 0xff)];
		}
		return h;
	}
}
