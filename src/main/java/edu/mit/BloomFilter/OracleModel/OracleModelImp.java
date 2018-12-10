package edu.mit.BloomFilter.OracleModel;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.classification.BinaryLogisticRegressionSummary;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.NaiveBayes;
import org.apache.spark.ml.classification.LinearSVC;
import org.apache.spark.ml.classification.LinearSVCModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.ml.classification.DecisionTreeClassifier;
import org.apache.spark.ml.classification.DecisionTreeClassificationModel;
import org.apache.spark.ml.classification.NaiveBayesModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.ml.classification.RandomForestClassificationModel;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.feature.*;
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics;
import org.apache.spark.sql.*;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.zookeeper.KeeperException;
import weka.classifiers.functions.LinearRegression;

public class OracleModelImp implements OracleModel{

	LogisticRegressionModel model;
	RegexTokenizer regexTokenizer;
	NGram ngramTransformer;
	CountVectorizerModel cvModel;
	StringIndexerModel indexer;
	SparkSession spark;
	Dataset<Row> df;

	/**
	 * Constructor.
	 */
	public OracleModelImp() {
	}
	
	public void learn(File inputFile, double fpr) throws Exception {
		// Assuming the input file is a CSV
		StructType schema = new StructType()
				.add("url", "string")
				.add("labelString", "string");

		SparkConf configuration = new SparkConf()
				.setAppName("Learned Oracle")
				.setMaster("local")
                .set("spark.default.parallelism", "300")
                .set("spark.sql.shuffle.partitions", "300");

		SparkContext sc = new SparkContext(configuration);
		sc.setLogLevel("ERROR");

		spark = new SparkSession(sc);

		Dataset<Row> df = spark.read()
				.option("mode", "DROPMALFORMED")
                .option("header", "true")
				.schema(schema)
				.csv(inputFile.getPath());

		regexTokenizer = new RegexTokenizer()
				.setInputCol("url")
				.setOutputCol("tokenized_url")
				.setPattern("");

		Dataset<Row> tokenized = regexTokenizer.transform(df);

		ngramTransformer = new NGram().setN(3).setInputCol("tokenized_url").setOutputCol("ngrams");
		tokenized = ngramTransformer.transform(tokenized);

		cvModel = new CountVectorizer()
				.setInputCol("ngrams")
				.setOutputCol("features")
				.setVocabSize(2500)
				.setMinDF(2)
				.fit(tokenized);

		tokenized = cvModel.transform(tokenized);

		indexer = new StringIndexer()
				.setInputCol("labelString")
				.setOutputCol("label")
				.fit(tokenized);


		Dataset<Row> final_df = indexer.transform(tokenized);

		LogisticRegression lr = new LogisticRegression().setMaxIter(500).setStandardization(false).setTol(1e-8).setElasticNetParam(0.8).setRegParam(0.0);
        int[] layers = new int[] {2500, 500, 200, 2};
        // MultilayerPerceptronClassifier lr = new MultilayerPerceptronClassifier().setLayers(layers).setBlockSize(128);


        // RandomForestClassifier lr = new RandomForestClassifier().set;
        System.out.println("About to start training...");
		model = lr.fit(final_df);

		if (model instanceof LogisticRegressionModel) {
            double[] history = model.summary().objectiveHistory();
            BinaryLogisticRegressionSummary binarySummary = (BinaryLogisticRegressionSummary) model.summary();
        	double auc = binarySummary.areaUnderROC();
            System.out.println("AuC: " + String.valueOf(auc));

            System.out.println("PR");
            binarySummary.pr().show();

            Dataset<Row> fMeasure = binarySummary.fMeasureByThreshold();
            double maxFMeasure = fMeasure.select(functions.max("F-Measure")).head().getDouble(0);
            double bestThreshold = fMeasure.where(fMeasure.col("F-Measure").equalTo(maxFMeasure))
                    .select("threshold").head().getDouble(0);
            lr.setThreshold(bestThreshold);
        }

        this.df = model.transform(final_df);

		String[] cols = new String[]{"prediction"};
		Dataset<Row> df_to_write = this.df.select("label", cols);
		String timeAsString = new Timestamp(System.currentTimeMillis()).toString();
		String filename = String.format("final_df_model_%s_%s", model.getClass().toString(), timeAsString);
        df_to_write.write().option("header", "true").csv(filename);
    }

    public HashSet<String> getClassifications(boolean isTrue) throws Exception {
        Dataset<Row> s = df.where("prediction==" + String.valueOf(isTrue));
        Dataset<Row> a = s.select("url");
        Iterator<Row> iter =  a.toLocalIterator();
        HashSet<String> set = new HashSet<>();
        while (iter.hasNext()) {
            Row r = iter.next();
            set.add((String) r.get(0));
        }
        return set;
    }

	public boolean classify(String url) throws Exception {
        List<Row> data = Arrays.asList(
                RowFactory.create(url)
        );

        StructType schema = new StructType(new StructField[]{
                new StructField("url", DataTypes.StringType, false, Metadata.empty())
        });

        Dataset<Row> df = spark.createDataFrame(data, schema);

        df = regexTokenizer.transform(df);
        df = ngramTransformer.transform(df);
        df = cvModel.transform(df);
        df = model.transform(df);
        Row results = df.select("prediction").first();
        return ((double) results.get(0) >= 0.5);
	}

	public int getNumberOfFalsePos(File inputFile) throws Exception {
	    StructType schema = new StructType()
				.add("url", "string")
				.add("labelString", "string");

	    Dataset<Row> df = spark.read()
				.option("mode", "DROPMALFORMED")
                .option("header", "true")
				.schema(schema)
				.csv(inputFile.getPath());

	    df = regexTokenizer.transform(df);
        df = ngramTransformer.transform(df);
        df = cvModel.transform(df);

        // bad is 1, good is 0
        df = indexer.transform(df);
        df = model.transform(df);
        Row firstRow = df.first();
        int predIdx = firstRow.fieldIndex("prediction");
        int labelIdx = firstRow.fieldIndex("labelString");
        Iterator<Row> iter = df.toLocalIterator();
        int fp = 0;
        while (iter.hasNext()) {
           Row r = iter.next();
           int pred = (int) ((double) r.get(predIdx));
           String label = (String) r.get(labelIdx);
           if (label.equals("good")) {
               if (pred==1) {
                fp += 1;
               }
           }
        }
        return fp;
    }

	
	public String printSize() {
	    return String.valueOf(this.getSize());
	}
	
	public void save(String s) throws Exception {
	    model.write().overwrite().save(s);
	}

	public void load(String s) throws Exception {
	    model = LogisticRegressionModel.load(s);
	}

	@Override
	public int getSize() {
		// return model.coefficients().size();
        return 0;
	}
}
