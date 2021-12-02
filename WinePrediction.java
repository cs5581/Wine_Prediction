package com.mycompany.app;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.File;

import static com.mycompany.app.Constants.*;

public class WinePrediction {

    public static void main(String[] args) {

        Logger.getLogger("org").setLevel(Level.ERROR);
        Logger.getLogger("akka").setLevel(Level.ERROR);
        Logger.getLogger("breeze.optimize").setLevel(Level.ERROR);
        Logger.getLogger("com.amazonaws.auth").setLevel(Level.DEBUG);
        Logger.getLogger("com.github").setLevel(Level.ERROR);


        SparkSession spark = SparkSession.builder()
                .appName("Wine-quality-test")
                .master()
                .config("spark.executor.memory", "2147480000")
                .config("spark.driver.memory", "2147480000")
                .config("spark.testing.memory", "2147480000")
                .getOrCreate();

        if (StringUtils.isNotEmpty(ACCESS_KEY_ID) && StringUtils.isNotEmpty(SECRET_KEY)) {

            spark.sparkContext().hadoopConfiguration().set("fs.s3a.access.key", ACCESS_KEY_ID);
            spark.sparkContext().hadoopConfiguration().set("fs.s3a.secret.key", SECRET_KEY);

        }

        File tempFile = new File("TestDataset.csv");
        boolean exists = tempFile.exists();
        if(exists){
            WinePrediction parser = new WinePrediction();
            parser.logisticRegression(spark);
            System.out.println("..");
        }else{
            System.out.println("TestDataset.csv");
            System.out.print(" doesn't exist");
        }



    }


    public void logisticRegression(SparkSession spark) {
        System.out.println("TestingDataSet Metrics \n");
        PipelineModel pipelineModel = PipelineModel.load("TrainingModel");
        Dataset<Row> testDf = getDataFrame(spark, true, "TestDataset.csv").cache();
        Dataset<Row> predictionDF = pipelineModel.transform(testDf).cache();
        predictionDF.select("features", "label", "prediction").show(5, false);
        printMertics(predictionDF);

    }

    public Dataset<Row> getDataFrame(SparkSession spark, boolean transform, String name) {

        Dataset<Row> validationDf = spark.read().format("csv").option("header", "true")
                .option("multiline", true).option("sep", ";").option("quote", "\"")
                .option("dateFormat", "M/d/y").option("inferSchema", true).load(name);

        Dataset<Row> lblFeatureDf = validationDf.withColumnRenamed("quality", "label").select("label",
                "alcohol", "sulphates", "pH", "density", "free sulfur dioxide", "total sulfur dioxide",
                "chlorides", "residual sugar", "citric acid", "volatile acidity", "fixed acidity");

        lblFeatureDf = lblFeatureDf.na().drop().cache();

        VectorAssembler assembler =
                new VectorAssembler().setInputCols(new String[]{"alcohol", "sulphates", "pH", "density",
                        "free sulfur dioxide", "total sulfur dioxide", "chlorides", "residual sugar",
                        "citric acid", "volatile acidity", "fixed acidity"}).setOutputCol("features");

        if (transform)
            lblFeatureDf = assembler.transform(lblFeatureDf).select("label", "features");


        return lblFeatureDf;
    }


    public void printMertics(Dataset<Row> predictions) {
        System.out.println();
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator();
        evaluator.setMetricName("accuracy");
        System.out.println("The accuracy of the model is " + evaluator.evaluate(predictions));

        evaluator.setMetricName("f1");
        double f1 = evaluator.evaluate(predictions);

        System.out.println("F1: " + f1);
    }
}
