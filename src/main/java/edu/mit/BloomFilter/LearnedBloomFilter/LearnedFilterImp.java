package edu.mit.BloomFilter.LearnedBloomFilter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import weka.classifiers.Classifier;
import weka.core.converters.CSVLoader;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;

public class LearnedFilterImp implements LearnedFilter{

	private Classifier clf;
	private Instances dataSet;

	/** 
	 * Constructor.
	 */
	public LearnedFilterImp() {
		this.clf = null;
	}
	
	public void learn(File inputFile, double fpr, File negativeResultOutputFile) throws Exception {
		// Assuming the input file is a CSV
		CSVLoader loader = new CSVLoader();
		loader.setSource(inputFile);
		Instances dataSet = loader.getDataSet();

		clf = new weka.classifiers.functions.Logistic();
		clf.buildClassifier(dataSet);
		Evaluation eval = new Evaluation(dataSet);
		eval.evaluateModel(clf, dataSet);
		System.out.println("** Linear Regression Evaluation with Datasets **");
		System.out.println(eval.toSummaryString());
		System.out.print(" the expression for the input data as per alogorithm is ");
		System.out.println(clf);
	}
	
	public boolean classify(String url) throws Exception {
		Instances testset = dataSet.stringFreeStructure();

		// Make message into test instance.
		Instance instance = makeInstance(url, testset);
		double output = clf.classifyInstance(instance);
		System.out.println("Class: " + String.valueOf(output));
		return output==1.0;
	}

	/**
	 * Method that converts a text message into an instance.
	 */
	private Instance makeInstance(String text, Instances data) {

		// Create instance of length two.
		Instance instance = new Instance(2);

		// Set value for message attribute
		Attribute messageAtt = data.attribute("Message");
		instance.setValue(messageAtt, messageAtt.addStringValue(text));

		// Give instance access to attribute information from the dataset.
		instance.setDataset(data);
		return instance;
	}
	
	public String printSize() {
		throw new java.lang.UnsupportedOperationException("Not supported yet.");
	}
	
	public void save(OutputStream outputStream) throws Exception {
	    SerializationHelper.write(outputStream, clf);
	}

	public void load(InputStream inputStream) throws Exception {
	    clf = (Classifier) SerializationHelper.read(inputStream);
	}
}
