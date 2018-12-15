package edu.mit.BloomFilter.OracleModel;

import java.io.File;

import org.junit.Test;


public class OracleModelTest {
	
	@Test
	public void classificationTest() {
		File inputDataFile = new File("model_training/data.csv");
		OracleModel model = new OracleModelImp();
		try {
			model.learn(inputDataFile);
			model.save("full_model");
			model.classify("https://moinnadeem.com");
		} catch (Exception e) {
			System.out.println("An exception as occurred");
			e.printStackTrace();
		}
	}
}
