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
		 * Make sure to change the ClassifierGenerator for the corresponding models and set the model paths accordingly
		 *
		 * Refer to Readme.md for further instructions.
		 * 
		 */
		//myPipeline.buildFilesNewDomain("src\\main\\resources", "src\\main\\resources\\learningtest_OwnClassifier\\subtask3\\constrained");
		//myPipeline.buildFilesOldDomain("src\\main\\resources\\SEABSA16_data", "src\\main\\resources\\learningtest_AUEB\\subtask3\\old\\unconstrained");
		//myPipeline.foldLearning();
		//myPipeline.executeAnnotationStudy();
		//myPipeline.executeRegressionTask("src\\main\\resources", "src\\main\\resources\\RQ2_learningtest\\unconstrained", false);
		
		
		//--IMPORTANT--
		//This method appends content to all existing .arff files in respective model folders.
		//Proceed with caution, remove the content of the target folder first
		//
		//For example, for constructing the Slot 3 unconstrained model for OwnClass
		//myPipeline.buildFilesNewDomain("src\\main\\resources", "src\\main\\resources\\learningtest_OwnClassifier\\subtask3\\constrained");
			//change the ClassifierType in line 154 to "OwnClassifier_ClassifierGenerator3" and the constrain type to "false" in 157
		//myPipeline.foldLearning();
			//change the EvaluatorType in line 222 to "OwnClassifier_Evaluator", the useOldData in line 226 to "false", the constrained in line 224 to "false", and the parameter of execute() in line 237 to "3". Enable the program code by removing the commentary lines.
	}
	
	//usual setup:
	//inputFile = "src\\main\\resources"
	//
	//outputFolder = "learningtest_GTI\\subtask1\\unconstrained\\" or any other folder
	//
	//generates .arff files (training data) and .gold files (test data) in the outputFolder
	
	//--IMPORTANT--
	//This method appends content to all existing .arff files in respective model folders.
	//Proceed with caution, remove the content of the folders first
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
        
        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(GTI_ClassifierGenerator.class, //change GTI_ClassifierGenerator with any _ClassifierGenerator class (except the RQ2-generator)
        		GTI_ClassifierGenerator.PARAM_OUTPUT_PATH, outputFolder, 
        		GTI_ClassifierGenerator.PARAM_RELATION_NAME, "classifierName",
        		GTI_ClassifierGenerator.PARAM_CONSTRAINED, "true",  //change to "true" for constrained, "false" for unconstrained type
        		GTI_ClassifierGenerator.PARAM_USE_OLD_DATA, "true",
        		GTI_ClassifierGenerator.PARAM_IS_TEST_DATA, "false");
        
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
        
        AnalysisEngineDescription writer2 = AnalysisEngineFactory.createEngineDescription(Baseline2_ClassifierGenerator3.class, //change GTI_ClassifierGenerator with any _ClassifierGenerator class (except the RQ2-generator)
        		GTI_ClassifierGenerator.PARAM_OUTPUT_PATH, outputFolder, 
        		GTI_ClassifierGenerator.PARAM_RELATION_NAME, "Baseline2",
        		GTI_ClassifierGenerator.PARAM_CONSTRAINED, "true",  //change to "true" for constrained, "false" for unconstrained type
        		GTI_ClassifierGenerator.PARAM_USE_OLD_DATA, "true",
        		GTI_ClassifierGenerator.PARAM_IS_TEST_DATA, "true");
        
        SimplePipeline.runPipeline(reader2, tokenizer, tagger, lemmatizer, dependency, writer2);
	}
	
	//usual setup:
	//inputFile = "src\\main\\resources"
	//
	//outputFolder = "learningtest_OwnClassifier\\subtask3\\unconstrained\\" or any other folder
	//
	//generates .arff files in the outputFolder
	
	//--IMPORTANT--
	//This method appends content to all existing .arff files in respective model folders.
	//Proceed with caution, remove the content of the folders first
	public void buildFilesNewDomain(String inputFile, String outputFolder) throws UIMAException, IOException {
		 System.setProperty("DKPRO_HOME", System.getProperty("user.home")+"/Desktop/");
		 
	        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
	                XmiReader.class, XmiReader.PARAM_LANGUAGE, "x-undefined",
	                XmiReader.PARAM_SOURCE_LOCATION,
	                inputFile,
	                XmiReader.PARAM_PATTERNS, "*.xmi",
	                XmiReader.PARAM_TYPE_SYSTEM_FILE, "src/main/resources/typesystem.xml");
	        
			 AnalysisEngineDescription lemmatizer = AnalysisEngineFactory.createEngineDescription(ClearNlpLemmatizer.class);
	        
	        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(OwnClassifier_ClassifierGenerator3.class, //change OwnClassifier_ClassifierGenerator3 with any _ClassifierGenerator class
	        		OwnClassifier_ClassifierGenerator3.PARAM_OUTPUT_PATH, outputFolder, 
	        		OwnClassifier_ClassifierGenerator3.PARAM_RELATION_NAME, "OwnClassifier",
	        		OwnClassifier_ClassifierGenerator3.PARAM_CONSTRAINED, "false", //change to "true" for constrained, "false" for unconstrained type
	        		OwnClassifier_ClassifierGenerator3.PARAM_USE_OLD_DATA, "false");
	        
	        SimplePipeline.runPipeline(reader, lemmatizer, writer);
	}
	
	public void executeAnnotationStudy() throws UIMAException, IOException {
		System.setProperty("DKPRO_HOME", System.getProperty("user.home")+"/Desktop/");
		 
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                XmiReader.class, XmiReader.PARAM_LANGUAGE, "x-undefined",
                XmiReader.PARAM_SOURCE_LOCATION,
                "src\\main\\resources\\dataset2\\split", //change this folder path to a folder with 2 .xmi files in it, where both .xmi files contain annotations by two different annotators
                XmiReader.PARAM_PATTERNS, "*.xmi",
                XmiReader.PARAM_TYPE_SYSTEM_FILE, "src/main/resources/typesystem.xml");
        
        AnalysisEngineDescription annotationStudy = AnalysisEngineFactory.createEngineDescription(AnnotationStudy.class);
        
        SimplePipeline.runPipeline(reader, annotationStudy);
	}
	
	//usual parameter
	//inputFile = "src\\main\\resources"
	//outputFolder = "src\\main\\resources\\RQ2_learningtest\\unconstrained"
	//constrained = true or false
	//generates .arff file of the regression task in the outputFolder
	
	//--IMPORTANT--
	//This method appends content to all existing .arff files in respective model folders.
	//Proceed with caution, remove the content of the folders first
	public void executeRegressionTask(String inputFile, String outputFolder, boolean constrained) throws UIMAException, IOException {
		 System.setProperty("DKPRO_HOME", System.getProperty("user.home")+"/Desktop/");
		 
	        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
	                XmiReader.class, XmiReader.PARAM_LANGUAGE, "x-undefined",
	                XmiReader.PARAM_SOURCE_LOCATION,
	                inputFile,
	                XmiReader.PARAM_PATTERNS, "*.xmi",
	                XmiReader.PARAM_TYPE_SYSTEM_FILE, "src/main/resources/typesystem.xml");
	        
			 AnalysisEngineDescription lemmatizer = AnalysisEngineFactory.createEngineDescription(ClearNlpLemmatizer.class);
	        
			//do not change any settings in this method, they are already complete
	        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(RQ2_ReviewLevelRegression_ClassifierGenerator.class, 
	        		RQ2_ReviewLevelRegression_ClassifierGenerator.PARAM_OUTPUT_PATH, outputFolder, 
	        		RQ2_ReviewLevelRegression_ClassifierGenerator.PARAM_RELATION_NAME, "RegressionClassifier",
	        		RQ2_ReviewLevelRegression_ClassifierGenerator.PARAM_CONSTRAINED, String.valueOf(constrained),
	        		RQ2_ReviewLevelRegression_ClassifierGenerator.PARAM_USE_OLD_DATA, "false",
	        		RQ2_ReviewLevelRegression_ClassifierGenerator.PARAM_XML_PATH, "src\\main\\resources");
	        
	        SimplePipeline.runPipeline(reader, lemmatizer, writer);
	}
	
	public void foldLearning() throws Exception {	 
		//--IMPORTANT--
		//This method appends content to all existing analysis files in respective model folders.
		//Proceed with caution, remove the content of the folders first
		//Enable the following lines for executing the Regression evaluation:
			//Regression_Evaluator myEvaluator = new Regression_Evaluator();
			//myEvaluator.useOldData(false); //enable/disable old domain
			//myEvaluator.execute(3, "lr"); //executes the "unconstrained, no text" evaluation with the classifier algorithm LinearRegression
			
		
		//Enable the following lines for all other Classifier evaluations
			//change _Evaluator type for the wished model evaluation:
				//OwnClass_Evaluator myEvaluator = new OwnClass_Evaluator();
			//change constrain type for the model evaluation:
				//myEvaluator.constrained = true;
			//enable/disable old domain data usage:
				//myEvaluator.useOldData(false);
		
			//[------------
			//feature analysis/ablation
				//myEvaluator.setUpAblation("0,1,2", 5, 3); //-->removes features Nr.0,1,2 from the model,
															//then creates an own analysis file for each model
															//with the following features removed:
															//-0,1,2,3
															//-0,1,2,4
		
			//OR general evaluation:
				//myEvaluator.execute(1);		//--> create analysis.txt in designated model folder
			//------------]
		
			//alternatively:
			//manual removal of features:
				//myEvaluator.setOutputPath(myEvaluator.sourcePath + "_0142122_neutral.txt"); //manual path for analysis-file
				//myEvaluator.execute(3, new int[] {0,4,22}); //manually execute evaluation with an own removalFilter
		
	}
}
