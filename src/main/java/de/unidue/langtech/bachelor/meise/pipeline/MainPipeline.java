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

		//myPipeline.run_read("src/main/resources/dataset5","src/main/resources/learningtest", null, "src/main/resources/dataset5/test.txt");
		//myPipeline.run_read("src/main/resources/", "*.xmi");
		//myPipeline.createArff("src\\main\\resources", "src\\main\\resources\\learningtest_Baseline2\\subtask3\\constrained", "*.xmi");
		//myPipeline.run("src\\main\\resources\\SEABSA16_data", "src\\main\\resources\\learningtest_AUEB\\subtask3\\old\\unconstrained");
		//myPipeline.run(myPipeline.inputFilePath, myPipeline.outputFilePath);
		//myPipeline.foldLearning("src\\main\\resources\\learningtest_OwnClassifier\\subtask3\\unconstrained", "src\\main\\resources\\learningtest_OwnClassifier\\subtask3\\unconstrained\\analysis_test1.txt");
		
		//myPipeline.executeReviewRegressionTask("src\\main\\resources", "src\\main\\resources","src\\main\\resources\\RQ2_learningtest\\unconstrained", "output.xml", "true");
		//myPipeline.valenceStatsRegressionTask("src\\main\\resources\\dataset5", "src\\main\\resources\\RQ2_learningtest_hotel-level");		
		
		//myPipeline.foldLearning();
		myPipeline.executeAnnotationStudy();
	}
	
	public void run(String inputFile, String outputFile) throws UIMAException, IOException {
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
        
        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(AUEB_ClassifierGenerator3.class, 
        		AUEB_ClassifierGenerator3.PARAM_OUTPUT_PATH, outputFile, 
        		AUEB_ClassifierGenerator3.PARAM_RELATION_NAME, "AKTSKI",
        		AUEB_ClassifierGenerator3.PARAM_CONSTRAINED, "false",
        		AUEB_ClassifierGenerator3.PARAM_USE_OLD_DATA, "true",
        		AUEB_ClassifierGenerator3.PARAM_IS_TEST_DATA, "false");
        
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
        
        AnalysisEngineDescription writer2 = AnalysisEngineFactory.createEngineDescription(AUEB_ClassifierGenerator3.class, 
        		AUEB_ClassifierGenerator3.PARAM_OUTPUT_PATH, outputFile, 
        		AUEB_ClassifierGenerator3.PARAM_RELATION_NAME, "AKTSKI",
        		AUEB_ClassifierGenerator3.PARAM_CONSTRAINED, "false",
        		AUEB_ClassifierGenerator3.PARAM_USE_OLD_DATA, "true",
        		AUEB_ClassifierGenerator3.PARAM_IS_TEST_DATA, "true");
        
        SimplePipeline.runPipeline(reader2, tokenizer, tagger, lemmatizer, dependency, writer2);
	}
	
	public void createArff(String inputFile, String outputFile, String typeFile) throws UIMAException, IOException {
		 System.setProperty("DKPRO_HOME", System.getProperty("user.home")+"/Desktop/");
		 
	        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
	                XmiReader.class, XmiReader.PARAM_LANGUAGE, "x-undefined",
	                XmiReader.PARAM_SOURCE_LOCATION,
	                inputFile,
	                XmiReader.PARAM_PATTERNS, typeFile,
	                XmiReader.PARAM_TYPE_SYSTEM_FILE, "src/main/resources/typesystem.xml");
	        
			 AnalysisEngineDescription lemmatizer = AnalysisEngineFactory.createEngineDescription(ClearNlpLemmatizer.class);
	        
	        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(Baseline2_ClassifierGenerator3.class, 
	        		Baseline2_ClassifierGenerator3.PARAM_OUTPUT_PATH, outputFile, 
	        		Baseline2_ClassifierGenerator3.PARAM_RELATION_NAME, "Baseline2",
	        		Baseline2_ClassifierGenerator3.PARAM_CONSTRAINED, "true",
	        		Baseline2_ClassifierGenerator3.PARAM_USE_OLD_DATA, "false");
	        
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
	
	public void foldLearning() throws Exception {	 
		/* LibSVM svm = new LibSVM();
		svm.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_LINEAR, LibSVM.TAGS_KERNELTYPE));
		svm.setSVMType(new SelectedTag(LibSVM.SVMTYPE_C_SVC, LibSVM.TAGS_SVMTYPE));
		svm.setProbabilityEstimates(true);
		//svm.setDegree(2);
		svm.setNormalize(true);
		svm.setShrinking(true);
		//svm.setCost(210);
		//svm.setGamma(0.0015);
		//svm.setEps(0.0001);
			
		SimpleLinearRegression slr = new SimpleLinearRegression();
		slr.setOutputAdditionalStats(true);
		
		 
		 Classifier myClassifier = null;
		 LinearRegression lr = new LinearRegression(); 
		 //lr.setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_M5, LinearRegression.TAGS_SELECTION));
		 //lr.setRidge(0.0000001);
		 //lr.setDebug(true);
	 
		 IBk ibk = new IBk();	
		 ibk.setKNN(3);
		 ibk.setDebug(true);
		 ibk.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_SIMILARITY, IBk.TAGS_WEIGHTING));
		 ibk.setMeanSquared(true);*/

		AKTSKI_Evaluator myEvaluator = new AKTSKI_Evaluator();
		myEvaluator.useOldData(false);
		myEvaluator.setUpAblation("0", 23, 3);
	}
}
