package edu.mit.BloomFilter.LearnedBloomFilter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.feature.*;
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics;
import org.apache.spark.sql.*;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.sql.types.StructType;
import org.apache.zookeeper.KeeperException;
import scala.Tuple2;


public class LearnedFilterImp implements LearnedFilter{

	/**
	 * Constructor.
	 */
	public LearnedFilterImp() {

	}
	
	public void learn(File inputFile, double fpr, File negativeResultOutputFile) throws Exception {
		// Assuming the input file is a CSV
		StructType schema = new StructType()
				.add("url", "string")
				.add("labelString", "string");

		SparkConf configuration = new SparkConf()
				.setAppName("Your Application Name")
				.setMaster("local");
		SparkContext sc = new SparkContext(configuration);

		SparkSession spark = new SparkSession(sc);

		Dataset<Row> df = spark.read()
				.option("mode", "DROPMALFORMED")
                .option("header", "true")
				.schema(schema)
				.csv(inputFile.getPath());

		RegexTokenizer regexTokenizer = new RegexTokenizer()
				.setInputCol("url")
				.setOutputCol("tokenized_url")
				.setPattern("");

		Dataset<Row> tokenized = regexTokenizer.transform(df);

		NGram ngramTransformer = new NGram().setN(2).setInputCol("tokenized_url").setOutputCol("ngrams");
		tokenized = ngramTransformer.transform(tokenized);
		tokenized.show();

		CountVectorizerModel cvModel = new CountVectorizer()
				.setInputCol("ngrams")
				.setOutputCol("features")
				.setVocabSize(3)
				.setMinDF(2)
				.fit(tokenized);

		tokenized = cvModel.transform(tokenized);

		System.out.println("Newly vectorized model...");
		tokenized.show();

		StringIndexerModel indexer = new StringIndexer()
				.setInputCol("labelString")
				.setOutputCol("label")
				.fit(tokenized);


		Dataset<Row> final_df = indexer.transform(tokenized);

		LogisticRegression lr = new LogisticRegression()
				.setMaxIter(10)
				.setRegParam(0.3)
				.setElasticNetParam(0.8);

		LogisticRegressionModel model = lr.fit(final_df);
		double[] history = model.summary().objectiveHistory();
		for(double i: history) {
			System.out.println(i);
		}
		model.predict()
	}
	
	public boolean classify(String url) throws Exception {
		throw new UnsupportedOperationException();
		// Make message into test instance.
	}

	
	public String printSize() {
		throw new java.lang.UnsupportedOperationException("Not supported yet.");
	}
	
	public void save(OutputStream outputStream) throws Exception {
		throw new UnsupportedOperationException();
	}

	public void load(InputStream inputStream) throws Exception {
		throw new UnsupportedOperationException();
	}
}
