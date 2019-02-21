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
	public int[] removeArray;
	
	public MainPipeline() {
		fu = new FileUtils();
	}
	
	public static void main(String[] args) throws Exception {
		MainPipeline myPipeline = new MainPipeline();

		//myPipeline.run_read("src/main/resources/dataset5","src/main/resources/learningtest", null, "src/main/resources/dataset5/test.txt");
		//myPipeline.run_read("src/main/resources/", "*.xmi");
		//myPipeline.createArff("src\\main\\resources", "src\\main\\resources\\learningtest_OwnClassifier\\subtask3\\unconstrained", "*.xmi");
		//myPipeline.run("src\\main\\resources\\SEABSA16_data", "src\\main\\resources\\learningtest_BUTknot\\old\\constrained");
		//myPipeline.run(myPipeline.inputFilePath, myPipeline.outputFilePath);
		//myPipeline.foldLearning("src\\main\\resources\\learningtest_OwnClassifier\\subtask3\\unconstrained", "src\\main\\resources\\learningtest_OwnClassifier\\subtask3\\unconstrained\\analysis_test1.txt");
		
		//myPipeline.executeReviewRegressionTask("src\\main\\resources", "src\\main\\resources","src\\main\\resources\\RQ2_learningtest\\unconstrained", "output.xml", "true");
		//myPipeline.valenceStatsRegressionTask("src\\main\\resources\\dataset5", "src\\main\\resources\\RQ2_learningtest_hotel-level");
		
		myPipeline.foldLearning("src\\main\\resources\\learningtest_BUTknot\\old\\constrained", "src\\main\\resources\\learningtest_BUTknot\\old\\constrained\\analysis");
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
        
        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(BUTknot_ClassifierGenerator.class, 
        		BUTknot_ClassifierGenerator.PARAM_OUTPUT_PATH, outputFile, 
        		BUTknot_ClassifierGenerator.PARAM_RELATION_NAME, "BUTknot",
        		BUTknot_ClassifierGenerator.PARAM_CONSTRAINED, "true",
        		BUTknot_ClassifierGenerator.PARAM_USE_OLD_DATA, "true",
        		BUTknot_ClassifierGenerator.PARAM_IS_TEST_DATA, "false");
        
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
        
        AnalysisEngineDescription writer2 = AnalysisEngineFactory.createEngineDescription(BUTknot_ClassifierGenerator.class, 
        		BUTknot_ClassifierGenerator.PARAM_OUTPUT_PATH, outputFile, 
        		BUTknot_ClassifierGenerator.PARAM_RELATION_NAME, "BUTknot",
        		BUTknot_ClassifierGenerator.PARAM_CONSTRAINED, "true",
        		BUTknot_ClassifierGenerator.PARAM_USE_OLD_DATA, "true",
        		BUTknot_ClassifierGenerator.PARAM_IS_TEST_DATA, "true");
        
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
	        
	        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(OwnClassifier_ClassifierGenerator3.class, 
	        		OwnClassifier_ClassifierGenerator3.PARAM_OUTPUT_PATH, outputFile, 
	        		OwnClassifier_ClassifierGenerator3.PARAM_RELATION_NAME, "OwnClassifier",
	        		OwnClassifier_ClassifierGenerator3.PARAM_CONSTRAINED, "false",
	        		OwnClassifier_ClassifierGenerator3.PARAM_USE_OLD_DATA, "false");
	        
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
        		RQ2_ReviewLevelRegression_ClassifierGenerator.PARAM_USE_OLD_DATA, "false",
        		RQ2_ReviewLevelRegression_ClassifierGenerator.PARAM_XML_PATH, folderPath
        		);
        
        //SimplePipeline.runPipeline(reader, tokenizer, tagger, lemmatizer, dependency, arffGenerator, exporter);
        SimplePipeline.runPipeline(reader, lemmatizer, writer);
	}
	
	public void foldLearning(String arffFileFolder, String outputPath) throws Exception {
		 ClassifierHandler myClassifierHandler = new ClassifierHandler();
		 
		 LibSVM svm = new LibSVM();
		//svm.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_RBF, LibSVM.TAGS_KERNELTYPE));
		svm.setSVMType(new SelectedTag(LibSVM.SVMTYPE_EPSILON_SVR, LibSVM.TAGS_SVMTYPE));
		svm.setProbabilityEstimates(true);
		//svm.setDegree(2);
		svm.setNormalize(true);
		svm.setShrinking(true);
		//svm.setCost(500);
		//svm.setGamma(0.001);
		//svm.setEps(0.00005);
			
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
		 ibk.setMeanSquared(true);
		 
		 
		CVParameterSelection cps = new CVParameterSelection();
		//svm.setWeights("1000 1");
		cps.setClassifier(svm);
		cps.setNumFolds(4);
		cps.setDebug(true);
		String[] params = new String[3];
		params[0] = "D 2 4 3";
		params[1] = "C 0.01 100 3";
		params[2] = "G 0.0001 0.1 3";
		cps.setCVParameters(params);
		
		SGD sgd = new SGD();
		sgd.setLossFunction(new SelectedTag(SGD.LOGLOSS, SGD.TAGS_SELECTION));
		sgd.setLearningRate(0.41);
		//sgd.setEpsilon(0.40);
		//sgd.setEpochs(10);
		
		AdditiveRegression ar = new AdditiveRegression();
		ar.setDebug(true);
		ar.setClassifier(svm);
		
		
		myClassifier = sgd;
		 //you may change myClassifier in order to set up a custom classifier algorithm
		 myClassifierHandler.useCFV = true;
		
		//remove this part if you do not want to enable ablation test

		 
		 
		//this last part will be cycled over: in this case, all attributes 1-9 are removed (if they are nott already removed)
		if(removeArray!=null) {
			int maxIteration = removeArray[removeArray.length-1];
			
			for(int i=1;i<maxIteration;i++) {
				//check if it isn't already included
				String alreadyIncluded="";
				boolean allGood=true;
				
				for(int check=0;check<removeArray.length-1;check++) {
					alreadyIncluded += removeArray[check];
					if(removeArray[check]==i) {
						allGood=false;
					}
				}
				
				if(allGood) {
					removeArray[removeArray.length-1] = i;
					System.out.println("Ablation test: Remove features '" + alreadyIncluded + "' + '" + i + "'.");
					myClassifierHandler.removeArray = removeArray;
						 
					myClassifierHandler.generateFoldsAndLearn(fu.getFilesInFolder(arffFileFolder, ".arff", false),5,1,LibSVM.KERNELTYPE_LINEAR, 0, outputPath + "_" + alreadyIncluded + "_" + i + ".txt", false, myClassifier);
				}
			}
		} else {
			 myClassifierHandler.generateFoldsAndLearn(fu.getFilesInFolder(arffFileFolder, ".arff", false),5,1,LibSVM.KERNELTYPE_LINEAR, 0, outputPath + ".txt", false, myClassifier);
		}
	}
}
