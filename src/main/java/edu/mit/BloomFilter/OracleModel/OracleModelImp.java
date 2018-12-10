package edu.mit.BloomFilter.OracleModel;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
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
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.ml.feature.NGram;
import org.apache.spark.ml.feature.RegexTokenizer;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.StringIndexerModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.zookeeper.KeeperException;
import weka.core.tokenizers.NGramTokenizer;

public class OracleModelImp implements OracleModel {

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
        SparkConf configuration = new SparkConf()
        .setAppName("Learned Oracle")
        .setMaster("local")
        .set("spark.default.parallelism", "300")
        .set("spark.sql.shuffle.partitions", "300");

        SparkContext sc = new SparkContext(configuration);
        sc.setLogLevel("ERROR");

        spark = new SparkSession(sc);
    }

    public void learn(File inputFile) throws Exception {
        // Assuming the input file is a CSV
        StructType schema = new StructType()
                .add("url", "string")
                .add("labelString", "string");

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
        Dataset<Row> ngram_df = ngramTransformer.transform(tokenized);

        cvModel = new CountVectorizer()
                .setInputCol("ngrams")
                .setOutputCol("features")
                .setVocabSize(2500)
                .setMinDF(2)
                .fit(ngram_df);

        Dataset<Row> cv_df = cvModel.transform(ngram_df);

        cv_df.select("labelString").distinct().show();

        indexer = new StringIndexer()
                .setInputCol("labelString")
                .setOutputCol("label")
                .fit(cv_df);

        Dataset<Row> final_df = indexer.transform(cv_df);
        final_df.show();

        LogisticRegression lr = new LogisticRegression().setMaxIter(500).setStandardization(false).setTol(1e-8).setElasticNetParam(0.8).setRegParam(0.0);
        int[] layers = new int[]{2500, 500, 200, 2};
        // MultilayerPerceptronClassifier lr = new MultilayerPerceptronClassifier().setLayers(layers).setBlockSize(128);

        // RandomForestClassifier lr = new RandomForestClassifier().set;
        System.out.println("About to load model...");
        this.load("./learned");
//        model = lr.fit(final_df);

//        if (model instanceof LogisticRegressionModel) {
//            double[] history = model.summary().objectiveHistory();
//            BinaryLogisticRegressionSummary binarySummary = (BinaryLogisticRegressionSummary) model.summary();
//            double auc = binarySummary.areaUnderROC();
//            System.out.println("AuC: " + String.valueOf(auc));
//
//            System.out.println("PR");
//            binarySummary.pr().show();
//
//            Dataset<Row> fMeasure = binarySummary.fMeasureByThreshold();
//            double maxFMeasure = fMeasure.select(functions.max("F-Measure")).head().getDouble(0);
//            double bestThreshold = fMeasure.where(fMeasure.col("F-Measure").equalTo(maxFMeasure))
//                    .select("threshold").head().getDouble(0);
//            lr.setThreshold(bestThreshold);
//        }

        this.df = model.transform(final_df);

        String[] cols = new String[]{"prediction", "url"};
        Dataset<Row> df_to_write = this.df.select("label", cols);
        String timeAsString = new Timestamp(System.currentTimeMillis()).toString();
        String filename = String.format("./final_df_model_%s_%s", model.getClass().toString().replace(" ", "_"), timeAsString.replace(" ", "_"));
        df_to_write.write().option("header", "true").csv(filename);
    }

    public HashSet<String> classifyFile(File f, boolean isTrue) throws Exception {
         StructType schema = new StructType()
                .add("url", "string")
                .add("labelString", "string");

        Dataset<Row> df = spark.read()
                .option("mode", "DROPMALFORMED")
                .option("header", "true")
                .schema(schema)
                .csv(f.getPath());

        df = regexTokenizer.transform(df);
        df = ngramTransformer.transform(df);
        df = cvModel.transform(df);
        df = model.transform(df);

        // bad predictions are 1, good predictions are 0.
        Dataset<Row> s = df.where("prediction==" + String.valueOf(isTrue));
        Dataset<Row> a = s.select("url");
        a.write().option("header", "true").csv("good_predictions_" + String.valueOf(Math.random() * 100));
        Iterator<Row> iter = a.toLocalIterator();
        HashSet<String> set = new HashSet<>();
        while (iter.hasNext()) {
            Row r = iter.next();
            set.add((String) r.get(0));
        }
        return set;
    }

    public boolean classify(String url) throws Exception {
        long startTime = System.nanoTime();

        List<Row> data = Arrays.asList(
                RowFactory.create(url)
        );
        System.out.println("Total time taken to run array creation: " + String.valueOf(System.nanoTime() - startTime));

        startTime = System.nanoTime();
        StructType schema = new StructType(new StructField[]{
                new StructField("url", DataTypes.StringType, false, Metadata.empty())
        });
        System.out.println("Total time taken to run structure creation: " + String.valueOf(System.nanoTime() - startTime));

        startTime = System.nanoTime();
        Dataset<Row> df = spark.createDataFrame(data, schema);
        System.out.println("Total time taken to run create dataframe: " + String.valueOf(System.nanoTime() - startTime));

        startTime = System.nanoTime();
        df = regexTokenizer.transform(df);
        df = ngramTransformer.transform(df);
        df = cvModel.transform(df);
        System.out.println("Total time taken to preprocess dataset: " + String.valueOf(System.nanoTime() - startTime));

        
        startTime = System.nanoTime();
        df = model.transform(df);
        
        Row results = df.select("prediction").first();
        System.out.println("Total time taken to run predictions: " + String.valueOf(System.nanoTime() - startTime));

        return ((double) results.get(0) >= 0.5);
    }

    public int getNumberOfFalseNegative(File inputFile) throws Exception {
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
        int fn = 0;
        while (iter.hasNext()) {
            Row r = iter.next();
            int pred = (int) ((double) r.get(predIdx));
            String label = (String) r.get(labelIdx);
            if (label.equals("bad")) {
                if (pred == 0) // label says good
                {
                    fn += 1;
                }
            }
        }
        return fn;
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
                if (pred == 1) {
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
        File dir = new File(s + "/");
        dir.mkdirs();
        model.write().overwrite().save(s + "/model");
        regexTokenizer.write().overwrite().save(s + "/regexTokenizer");
        ngramTransformer.write().overwrite().save(s + "/ngramTransformer");
        cvModel.write().overwrite().save(s + "/cvModel");
        indexer.write().overwrite().save(s + "/indexer");
    }

    public void load(String s) throws Exception {
        model = LogisticRegressionModel.load(s + "/model");
        regexTokenizer = RegexTokenizer.load(s + "/regexTokenizer");
        ngramTransformer = NGram.load(s + "/ngramTransformer");
        cvModel = CountVectorizerModel.load(s + "/cvModel");
        indexer = StringIndexerModel.load(s + "/indexer");
    }

    @Override
    public int getSize() {
        return model.coefficients().size();
    }
}
