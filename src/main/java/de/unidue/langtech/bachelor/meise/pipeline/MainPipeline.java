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
import weka.classifiers.functions.SimpleLinearRegression;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdditiveRegression;
import weka.classifiers.meta.CVParameterSelection;
import weka.core.SelectedTag;
import weka.filters.unsupervised.attribute.StringToWordVector;
import de.unidue.langtech.bachelor.meise.classifier.ClassifierHandler;
import de.unidue.langtech.bachelor.meise.files.DataParser;
import de.unidue.langtech.bachelor.meise.files.FileUtils;

public class MainPipeline {
	
	String inputFilePath = "src/main/resources/dataset5";
	String outputFilePath = "src/main/resources/dataset5/output_5.tsv";
	String modelFilePath = "src/main/resources/data800.model";
	String arffFilePath = "src/main/resources/data800.arff";
	String tsvOutput = "src/main/resources/output3.tsv";
	FileUtils fu;
	public boolean constrained = true;
	
	public MainPipeline() {
		fu = new FileUtils();
	}
	
	public static void main(String[] args) throws Exception {
		MainPipeline myPipeline = new MainPipeline();

		//myPipeline.run_read("src/main/resources/dataset5","src/main/resources/learningtest", null, "src/main/resources/dataset5/test.txt");
		//myPipeline.run_read("src/main/resources/", "*.xmi");
		myPipeline.createArff("src\\main\\resources", "src\\main\\resources\\learningtest_AKTSKI\\subtask3\\unconstrained", "*.xmi");
		//myPipeline.run("src\\main\\resources\\SEABSA16_data", "src\\main\\resources\\learningtest_AKTSKI\\subtask3\\old\\unconstrained");
		//myPipeline.run(myPipeline.inputFilePath, myPipeline.outputFilePath);
		//myPipeline.foldLearning("src\\main\\resources\\learningtest_OwnClassifier\\subtask3\\unconstrained", "src\\main\\resources\\learningtest_OwnClassifier\\subtask3\\unconstrained\\analysis_test1.txt");
		
		//myPipeline.executeReviewRegressionTask("src\\main\\resources", "src\\main\\resources","src\\main\\resources\\RQ2_learningtest\\", "output.xml", "true");
		//myPipeline.valenceStatsRegressionTask("src\\main\\resources\\dataset5", "src\\main\\resources\\RQ2_learningtest_hotel-level");
		myPipeline.foldLearning("src\\main\\resources\\learningtest_AKTSKI\\subtask3\\unconstrained", "src\\main\\resources\\learningtest_AKTSKI\\subtask3\\unconstrained\\analysis.txt");
	}
	
	public void run(String inputFile, String outputFile) throws UIMAException, IOException {
		File file = new File(inputFile);
		
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
        
        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(AKTSKI_ClassifierGenerator.class, 
        		AKTSKI_ClassifierGenerator.PARAM_OUTPUT_PATH, outputFile, 
        		AKTSKI_ClassifierGenerator.PARAM_RELATION_NAME, "AKTSKI",
        		AKTSKI_ClassifierGenerator.PARAM_CONSTRAINED, "false",
        		AKTSKI_ClassifierGenerator.PARAM_USE_OLD_DATA, "true");
        
        SimplePipeline.runPipeline(reader, tokenizer, tagger, lemmatizer, dependency, writer);
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
	        
			 /*AnalysisEngineDescription rawDataAnalyser = AnalysisEngineFactory.createEngineDescription(DataStatistics.class, 
					 DataStatistics.PARAM_OUTPUT_PATH, outputFile + "/sourceDataAnalysis.txt");*/
			 
	        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(AKTSKI_ClassifierGenerator.class, 
	        		AKTSKI_ClassifierGenerator.PARAM_OUTPUT_PATH, outputFile, 
	        		AKTSKI_ClassifierGenerator.PARAM_RELATION_NAME, "AKTSKI",
	        		AKTSKI_ClassifierGenerator.PARAM_CONSTRAINED, "false",
	        		AKTSKI_ClassifierGenerator.PARAM_USE_OLD_DATA, "false");
	        
	        SimplePipeline.runPipeline(reader, lemmatizer, writer);
	}
	
	public void executeReviewRegressionTask(String xmiPath, String folderPath, String outputPath, String namingConvention, String checkSubfolders) throws UIMAException, IOException {
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                XmiReader.class, XmiReader.PARAM_LANGUAGE, "x-undefined",
                XmiReader.PARAM_SOURCE_LOCATION,
                xmiPath,
                XmiReader.PARAM_PATTERNS, "*.xmi",
                XmiReader.PARAM_TYPE_SYSTEM_FILE, "src/main/resources/typesystem.xml");
        
		AnalysisEngineDescription lemmatizer = AnalysisEngineFactory.createEngineDescription(ClearNlpLemmatizer.class);
		
        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(
        		RQ2_ReviewLevelRegression_ClassifierGenerator.class, RQ2_ReviewLevelRegression_ClassifierGenerator.PARAM_OUTPUT_PATH, outputPath,
        		RQ2_ReviewLevelRegression_ClassifierGenerator.PARAM_CONSTRAINED, "false",
        		RQ2_ReviewLevelRegression_ClassifierGenerator.PARAM_RELATION_NAME, "RQ2",
        		RQ2_ReviewLevelRegression_ClassifierGenerator.PARAM_XML_PATH, folderPath
        		);
        
        //SimplePipeline.runPipeline(reader, tokenizer, tagger, lemmatizer, dependency, arffGenerator, exporter);
        SimplePipeline.runPipeline(reader, lemmatizer, writer);
	}
	
	public void foldLearning(String arffFileFolder, String outputPath) throws Exception {
		//TODO: Cycle through all models
		 ClassifierHandler myClassifierHandler = new ClassifierHandler();
		 
		 LibSVM svm = new LibSVM();
		 svm.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_RBF, LibSVM.TAGS_KERNELTYPE));
		//svm.setSVMType(new SelectedTag(LibSVM.SVMT, LibSVM.TAGS_SVMTYPE));
		//svm.setProbabilityEstimates(true);
		//svm.setDegree(3);
		svm.setNormalize(true);
		svm.setShrinking(true);
		//svm.setCost(200);
		//svm.setGamma(0.002);
		//svm.setEps(0.0005);
			
		SimpleLinearRegression slr = new SimpleLinearRegression();
		slr.setOutputAdditionalStats(true);
		
		 
		 Classifier myClassifier = null;
		 LinearRegression lr = new LinearRegression(); 
		 lr.setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_M5, LinearRegression.TAGS_SELECTION));
		 //lr.setRidge(0.0000001);
		 //lr.setDebug(true);
	 
		 IBk ibk = new IBk();
		 ibk.setKNN(1);
		 ibk.setDebug(true);
		 ibk.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_SIMILARITY, IBk.TAGS_WEIGHTING));
		 ibk.setMeanSquared(true);
		 
		 
		CVParameterSelection cps = new CVParameterSelection();
		cps.setClassifier(svm);
		cps.setNumFolds(5);
		cps.setDebug(true);
		String[] params = new String[3];
		params[0] = "C 0.001 0.005 4";
		params[1] = "G 0.0005 0.0015 4";
		params[2] = "P 0.005 0.51 4";
		cps.setCVParameters(params);
		
		AdditiveRegression ar = new AdditiveRegression();
		ar.setDebug(true);
		ar.setClassifier(svm);
		
		myClassifier = null;
		 //you may change myClassifier in order to set up a custom classifier algorithm
		 myClassifierHandler.useCFV = true;
		 //myClassifierHandler.testDataPath = 
		 myClassifierHandler.generateFoldsAndLearn(fu.getFilesInFolder(arffFileFolder, ".arff", false),10,1,LibSVM.KERNELTYPE_RBF, 0, outputPath, true, myClassifier);
	}
}
