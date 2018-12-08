package edu.mit.BloomFilter.LearnedBloomFilter;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.*;
import weka.core.converters.CSVLoader;
import weka.core.tokenizers.CharacterNGramTokenizer;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class OldLearnedFilterImp implements LearnedFilter{

	private Classifier clf;
	private Instances dataSet;

	/**
	 * Constructor.
	 */
	public OldLearnedFilterImp() {
		this.clf = null;
	}

	public void learn(File inputFile, double fpr, File negativeResultOutputFile) throws Exception {
		// Assuming the input file is a CSV
		CSVLoader loader = new CSVLoader();
		loader.setSource(inputFile);
		Instances dataSet = loader.getDataSet();
		dataSet.setClassIndex(dataSet.numAttributes() - 1);

		CharacterNGramTokenizer tokenizer = new CharacterNGramTokenizer();
		StringToWordVector filter = new StringToWordVector();
		filter.setAttributeIndices("0");
		filter.setTokenizer(tokenizer);
		filter.setLowerCaseTokens(true);

		clf = new weka.classifiers.functions.Logistic();
		FilteredClassifier fclf = new FilteredClassifier();
		fclf.setFilter(filter);
		fclf.setClassifier(clf);

		fclf.buildClassifier(dataSet);
		Evaluation eval = new Evaluation(dataSet);
		eval.evaluateModel(fclf, dataSet);
		System.out.println("** Linear Regression Evaluation with Datasets **");
		System.out.println(eval.toSummaryString());
		System.out.print(" the expression for the input data as per alogorithm is ");
		System.out.println(fclf);
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
		Instance instance = new SparseInstance(2);

		// Set value for message attribute
		Attribute messageAtt = data.attribute("Message");
		instance.setValue(messageAtt, messageAtt.addStringValue(text));

		// Give instance access to attribute information from the dataset.
		instance.setDataset(data);
		return instance;
	}

	public String printSize() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public void save(OutputStream outputStream) throws Exception {
	    SerializationHelper.write(outputStream, clf);
	}

	public void load(InputStream inputStream) throws Exception {
	    clf = (Classifier) SerializationHelper.read(inputStream);
	}
}
