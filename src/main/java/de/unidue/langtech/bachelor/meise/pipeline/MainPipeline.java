package de.unidue.langtech.bachelor.meise.pipeline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;

import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.maltparser.MaltParser;
import de.unidue.langtech.bachelor.meise.files.RawJsonReviewReader;
import de.unidue.langtech.bachelor.meise.type.classifiers.*;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.SGD;
import weka.classifiers.functions.SimpleLinearRegression;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdditiveRegression;
import weka.classifiers.meta.CVParameterSelection;
import weka.core.SelectedTag;
import weka.packages.ThresholdSelector;
import de.unidue.langtech.bachelor.meise.evaluation.*;
import de.unidue.langtech.bachelor.meise.files.FileUtils;

public class MainPipeline {
	
	FileUtils fu;
	
	public MainPipeline() {
		fu = new FileUtils();
	}
	
	public static void main(String[] args) throws Exception {
		MainPipeline myPipeline = new MainPipeline();

		/* 
		 * Choose one of the following methods for executing the pipelines:
		 * Make sure to change the ClassifierGenerator for the corresponding models and set the model paths accordinly
		 *
		myPipeline.buildFilesNewDomain("src\\main\\resources", "src\\main\\resources\\learningtest_Baseline2\\subtask3\\constrained");
		myPipeline.buildFilesOldDomain("src\\main\\resources\\SEABSA16_data", "src\\main\\resources\\learningtest_AUEB\\subtask3\\old\\unconstrained");
		myPipeline.foldLearning();
		myPipeline.executeAnnotationStudy();
		*/
		
		//myPipeline.buildFilesNewDomain("src\\main\\resources", "src\\main\\resources\\learningtest_OwnClassifier\\subtask3\\constrained");
		myPipeline.foldLearning();
		//myPipeline.executeAnnotationStudy();
		//myPipeline.executeRegressionTask("src\\main\\resources", "src\\main\\resources\\RQ2_learningtest\\unconstrained", false);
	}
	
	public void buildFilesOldDomain(String inputFile, String outputFolder) throws UIMAException, IOException {
		//Training data
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                RawJsonReviewReader.class, RawJsonReviewReader.PARAM_SOURCE_LOCATION, inputFile,
                RawJsonReviewReader.PARAM_LANGUAGE, "en",
                RawJsonReviewReader.PARAM_PATTERNS, "*.xml",
                RawJsonReviewReader.PARAM_FOLDER_PATH, inputFile,
                RawJsonReviewReader.PARAM_FILE_EXTENSION, ".xml",
                RawJsonReviewReader.PARAM_USE_OLD_DATA, "true",
                RawJsonReviewReader.PARAM_SEARCH_SUBFOLDERS, "false");
		
        AnalysisEngineDescription tokenizer = AnalysisEngineFactory.createEngineDescription(ClearNlpSegmenter.class, ClearNlpSegmenter.PARAM_LANGUAGE, "en");
		
        AnalysisEngineDescription tagger = AnalysisEngineFactory.createEngineDescription(ClearNlpPosTagger.class, ClearNlpPosTagger.PARAM_LANGUAGE, "en");
        
        AnalysisEngineDescription lemmatizer = AnalysisEngineFactory.createEngineDescription(ClearNlpLemmatizer.class, ClearNlpLemmatizer.PARAM_LANGUAGE, "en");
        
        AnalysisEngineDescription dependency = AnalysisEngineFactory.createEngineDescription(MaltParser.class, MaltParser.PARAM_LANGUAGE, "en");
        
        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(Baseline2_ClassifierGenerator3.class, 
        		Baseline2_ClassifierGenerator3.PARAM_OUTPUT_PATH, outputFolder, 
        		Baseline2_ClassifierGenerator3.PARAM_RELATION_NAME, "Baseline2",
        		Baseline2_ClassifierGenerator3.PARAM_CONSTRAINED, "true",
        		Baseline2_ClassifierGenerator3.PARAM_USE_OLD_DATA, "true",
        		Baseline2_ClassifierGenerator3.PARAM_IS_TEST_DATA, "false");
        
        SimplePipeline.runPipeline(reader, tokenizer, tagger, lemmatizer, dependency, writer);
        
        
        //Test data
        CollectionReaderDescription reader2 = CollectionReaderFactory.createReaderDescription(
                RawJsonReviewReader.class, RawJsonReviewReader.PARAM_SOURCE_LOCATION, inputFile,
                RawJsonReviewReader.PARAM_LANGUAGE, "en",
                RawJsonReviewReader.PARAM_PATTERNS, "*.gold",
                RawJsonReviewReader.PARAM_FOLDER_PATH, inputFile,
                RawJsonReviewReader.PARAM_FILE_EXTENSION, ".gold",
                RawJsonReviewReader.PARAM_USE_OLD_DATA, "true",
                RawJsonReviewReader.PARAM_SEARCH_SUBFOLDERS, "false");
        
        AnalysisEngineDescription writer2 = AnalysisEngineFactory.createEngineDescription(Baseline2_ClassifierGenerator3.class, 
        		Baseline2_ClassifierGenerator3.PARAM_OUTPUT_PATH, outputFolder, 
        		Baseline2_ClassifierGenerator3.PARAM_RELATION_NAME, "Baseline2",
        		Baseline2_ClassifierGenerator3.PARAM_CONSTRAINED, "true",
        		Baseline2_ClassifierGenerator3.PARAM_USE_OLD_DATA, "true",
        		Baseline2_ClassifierGenerator3.PARAM_IS_TEST_DATA, "true");
        
        SimplePipeline.runPipeline(reader2, tokenizer, tagger, lemmatizer, dependency, writer2);
	}
	
	public void buildFilesNewDomain(String inputFile, String outputFolder) throws UIMAException, IOException {
		 System.setProperty("DKPRO_HOME", System.getProperty("user.home")+"/Desktop/");
		 
	        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
	                XmiReader.class, XmiReader.PARAM_LANGUAGE, "x-undefined",
	                XmiReader.PARAM_SOURCE_LOCATION,
	                inputFile,
	                XmiReader.PARAM_PATTERNS, "*.xmi",
	                XmiReader.PARAM_TYPE_SYSTEM_FILE, "src/main/resources/typesystem.xml");
	        
			 AnalysisEngineDescription lemmatizer = AnalysisEngineFactory.createEngineDescription(ClearNlpLemmatizer.class);
	        
	        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(OwnClassifier_ClassifierGenerator3.class, 
	        		OwnClassifier_ClassifierGenerator3.PARAM_OUTPUT_PATH, outputFolder, 
	        		OwnClassifier_ClassifierGenerator3.PARAM_RELATION_NAME, "OwnClassifier",
	        		OwnClassifier_ClassifierGenerator3.PARAM_CONSTRAINED, "true",
	        		OwnClassifier_ClassifierGenerator3.PARAM_USE_OLD_DATA, "false");
	        
	        SimplePipeline.runPipeline(reader, lemmatizer, writer);
	}
	
	public void executeAnnotationStudy() throws UIMAException, IOException {
		System.setProperty("DKPRO_HOME", System.getProperty("user.home")+"/Desktop/");
		 
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                XmiReader.class, XmiReader.PARAM_LANGUAGE, "x-undefined",
                XmiReader.PARAM_SOURCE_LOCATION,
                "src\\main\\resources\\dataset2\\split",
                XmiReader.PARAM_PATTERNS, "*.xmi",
                XmiReader.PARAM_TYPE_SYSTEM_FILE, "src/main/resources/typesystem.xml");
        
        AnalysisEngineDescription annotationStudy = AnalysisEngineFactory.createEngineDescription(AnnotationStudy.class);
        
        SimplePipeline.runPipeline(reader, annotationStudy);
	}
	
	public void executeRegressionTask(String inputFile, String outputFolder, boolean constrained) throws UIMAException, IOException {
		 System.setProperty("DKPRO_HOME", System.getProperty("user.home")+"/Desktop/");
		 
	        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
	                XmiReader.class, XmiReader.PARAM_LANGUAGE, "x-undefined",
	                XmiReader.PARAM_SOURCE_LOCATION,
	                inputFile,
	                XmiReader.PARAM_PATTERNS, "*.xmi",
	                XmiReader.PARAM_TYPE_SYSTEM_FILE, "src/main/resources/typesystem.xml");
	        
			 AnalysisEngineDescription lemmatizer = AnalysisEngineFactory.createEngineDescription(ClearNlpLemmatizer.class);
	        
	        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(RQ2_ReviewLevelRegression_ClassifierGenerator.class, 
	        		RQ2_ReviewLevelRegression_ClassifierGenerator.PARAM_OUTPUT_PATH, outputFolder, 
	        		RQ2_ReviewLevelRegression_ClassifierGenerator.PARAM_RELATION_NAME, "RegressionClassifier",
	        		RQ2_ReviewLevelRegression_ClassifierGenerator.PARAM_CONSTRAINED, String.valueOf(constrained),
	        		RQ2_ReviewLevelRegression_ClassifierGenerator.PARAM_USE_OLD_DATA, "false",
	        		RQ2_ReviewLevelRegression_ClassifierGenerator.PARAM_XML_PATH, "src\\main\\resources");
	        
	        SimplePipeline.runPipeline(reader, lemmatizer, writer);
	}
	
	public void foldLearning() throws Exception {	 
		Regression_Evaluator myEvaluator = new Regression_Evaluator();
		myEvaluator.useOldData(false);
		myEvaluator.execute(3, "esvr");
		
		
		//enable/disable old domain
		//myEvaluator.useOldData(false);
		
		
		//feature analysis/ablation
		//myEvaluator.setUpAblation("0,1,2,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22", 5, 3);
		
		//manual removal of features:
		//myEvaluator.setOutputPath(myEvaluator.sourcePath + "_0142122_neutral.txt");
		//myEvaluator.execute(3, new int[] {0,4,22});
		//myEvaluator.execute(3, new int[] {0,1,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22});
		
		//general evaluation:
		//myEvaluator.execute(1);
	}
}
