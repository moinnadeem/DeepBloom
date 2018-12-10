package edu.mit.BloomFilter.Utils;

public class ModelResults {

	// number of items in backup filter
	public int numItemsBackup;
	
	// m and k for standard bloom filter implementation
	public int standardBfM;
	public int standardBfK;
	
	// number of false positives from filter
	public int numFalsePos;
	
	// number of items with negative label
	public int numNegLabels;
	
	// number of lines with wrong format
	public int numWrongFormatLines;
	
	// total number of valid items
	public int numValidItems;
	
	// fpr for the filter
	public double filterFpr;
	
	// size of initial filter in bytes
	public int initialFilterSize;
	
	// size of oracle model in bytes
	public int oracleModelSize;
	
	// size of backup filter in bytes
	public int backupFilterSize;
	
	// average time per query in nanoseconds
	public double avgTimePerQuery;
	
	public ModelResults(int numBackup, int m, int k, int falsePos, int negLbl, int wrongFormat, 
			int validItems, double fpr, int initialSize, int oracleSize, int backupSize, double avgTime) {
		this.numItemsBackup = numBackup;
		this.standardBfM = m;
		this.standardBfK = k;
		this.numFalsePos = falsePos;
		this.numNegLabels = negLbl;
		this.numWrongFormatLines = wrongFormat;
		this.numValidItems = validItems;
		this.filterFpr = fpr;
		this.initialFilterSize = initialSize;
		this.oracleModelSize = oracleSize;
		this.backupFilterSize = backupSize;
		this.avgTimePerQuery = avgTime;
	}
	
}
