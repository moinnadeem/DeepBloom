package edu.mit.course6828.SandwichedBloomFilter;

import java.io.IOException;

public interface SandwichedBloomFilter {
	
	public boolean contains(String s);
	
	public void save(String initialFilterFile, String learnedOracleFile, String backupFilterFile) throws IOException;
	
	public void load(String initialFilterFile, String learnedOracleFile, String backupFilterFile) throws IOException;
}
