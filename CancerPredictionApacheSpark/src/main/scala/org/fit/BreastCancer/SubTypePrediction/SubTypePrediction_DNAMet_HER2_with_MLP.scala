package org.fit.BreastCancer.SubTypePrediction

import org.apache.spark._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql._
import org.apache.spark.ml.feature.{ VectorAssembler, Normalizer }
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.VectorIndexer
import org.apache.spark.ml.tuning.{ ParamGridBuilder, CrossValidator }
import org.apache.spark.ml.classification.{ MultilayerPerceptronClassifier, MultilayerPerceptronClassificationModel }
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.PCA
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.feature.ChiSqSelector

object SubTypePrediction_DNAMet_HER2_with_MLP {
  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .master("local[4]")
      .config("spark.sql.warehouse.dir", "data/")
      .appName("Cancer prediction")
      .getOrCreate()
    import spark.implicits._

    spark.conf.set("spark.sql.crossJoin.enabled", "true")
    val inputDataDF = spark.read.
      option("inferSchema", "true")
      .option("delimiter", ",")
      .format("com.databricks.spark.csv")
      .option("header", "false")
      .load("data/input_csv/type/DNA_Methylation/input_csv_met_type_her2.csv")

    inputDataDF.printSchema()

    val inputLabelDF = spark.read.
      option("inferSchema", "true")
      .option("delimiter", "\n")
      .format("com.databricks.spark.csv")
      .option("header", "false")
      .load("data/labels_csv/type/label_type_her2.csv")

    val LabelDF = inputLabelDF.withColumnRenamed("_c0", "label")
    inputLabelDF.show()

    val featureCols = inputDataDF.columns

    val assembler = new VectorAssembler()
      .setInputCols(featureCols)
      .setOutputCol("features")

    val assembleDF = assembler.transform(inputDataDF).select("features")
    assembleDF.show()
    
    import org.apache.spark.sql.functions._
    val assembleDFwithLabel = LabelDF.join(assembleDF)
    assembleDFwithLabel.printSchema()
    
    val selector = new ChiSqSelector()
      .setNumTopFeatures(1000) //
      .setFeaturesCol("features")
      .setLabelCol("label")
      .setOutputCol("selectedFeatures")
      
    val bestFeatureDF = selector.fit(assembleDFwithLabel).transform(assembleDFwithLabel)
    val filteredFeatureDF = bestFeatureDF.select("label", "selectedFeatures")
    val filteredFeatureDF2 = filteredFeatureDF.withColumnRenamed("selectedFeatures", "features")
    filteredFeatureDF2.printSchema()   
    /*

    val pca = new PCA()
      .setInputCol("features")
      .setOutputCol("pcaFeatures")
      .setK(100)
      .fit(assembleDF)

    val pcaDF = pca.transform(assembleDF).select("pcaFeatures").withColumnRenamed("pcaFeatures", "features")
    pcaDF.show()

    import org.apache.spark.sql.functions._
    val inputDF = LabelDF.join(pcaDF)
    inputDF.printSchema()
    inputDF.coalesce(4)
    inputDF.show(5)
    */

    val Array(trainingDF, testDF) = filteredFeatureDF2.randomSplit(Array(0.75, 0.25), 12345L)

    val layers = Array[Int](100, 64, 128, 256, 4)
    val mlp = new MultilayerPerceptronClassifier()
      .setLayers(layers)
      .setBlockSize(128)
      .setTol(1E-2)
      .setMaxIter(1000)

    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")

    val mlpModel = mlp.fit(trainingDF)

    val predictions = mlpModel.transform(testDF)

    val evaluator1 = evaluator.setMetricName("accuracy")
    val evaluator2 = evaluator.setMetricName("weightedPrecision")
    val evaluator3 = evaluator.setMetricName("weightedRecall")
    val evaluator4 = evaluator.setMetricName("f1")

    // compute the classification accuracy, precision, recall, f1 measure and error on test data.
    val accuracy = evaluator1.evaluate(predictions)
    val precision = evaluator2.evaluate(predictions)
    val recall = evaluator3.evaluate(predictions)
    val f1 = evaluator4.evaluate(predictions)

    // Print the performance metrics
    println("Accuracy = " + accuracy);
    println("Precision = " + precision)
    println("Recall = " + recall)
    println("F1 = " + f1)
    println(s"Test Error = ${1 - accuracy}")
  }
}