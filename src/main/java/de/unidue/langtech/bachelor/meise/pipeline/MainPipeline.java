package de.unidue.langtech.bachelor.meise.pipeline;

import java.io.File;
import java.io.IOException;
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
import de.unidue.langtech.bachelor.meise.classifier.ClassifierHandler;
import de.unidue.langtech.bachelor.meise.files.DataParser;

public class MainPipeline {
	
	String inputFilePath = "src/main/resources/dataset5";
	String outputFilePath = "src/main/resources/dataset5/output_5.tsv";
	String modelFilePath = "src/main/resources/data800.model";
	String arffFilePath = "src/main/resources/data800.arff";
	String tsvOutput = "src/main/resources/output3.tsv";
	
	public MainPipeline() {
	}
	
	public static void main(String[] args) throws Exception {
		MainPipeline myPipeline = new MainPipeline();

		//myPipeline.run_read();
		//myPipeline.run_read("src/main/resources/", "*.xmi");
		//myPipeline.createArff("src/main/resources/", "src/main/resources/learningtest", "*.xmi");
		myPipeline.run(myPipeline.inputFilePath, myPipeline.outputFilePath);
	}
	
	public void run(String inputFile, String outputFile) throws UIMAException, IOException {
		File file = new File(inputFile);
		
		if(file.isDirectory()) {
			//convert files into .xml format first
			DataParser dp = new DataParser(inputFile, inputFile + "/output.xml");
			
			//feed the output and treat it as new input
			inputFile = inputFile + "/output.xml";
			dp.run(".txt");
		}	
		
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                RawJsonReviewReader.class, RawJsonReviewReader.PARAM_SOURCE_LOCATION, inputFile,
                RawJsonReviewReader.PARAM_LANGUAGE, "en");
		
        AnalysisEngineDescription tokenizer = AnalysisEngineFactory.createEngineDescription(ClearNlpSegmenter.class, ClearNlpSegmenter.PARAM_LANGUAGE, "en");
		
        AnalysisEngineDescription tagger = AnalysisEngineFactory.createEngineDescription(ClearNlpPosTagger.class, ClearNlpPosTagger.PARAM_LANGUAGE, "en");
        
        AnalysisEngineDescription lemmatizer = AnalysisEngineFactory.createEngineDescription(ClearNlpLemmatizer.class, ClearNlpLemmatizer.PARAM_LANGUAGE, "en");
        
        AnalysisEngineDescription dependency = AnalysisEngineFactory.createEngineDescription(MaltParser.class, MaltParser.PARAM_LANGUAGE, "en");
        
        AnalysisEngineDescription arffGenerator = AnalysisEngineFactory.createEngineDescription(TestClassifierGenerator.class, TestClassifierGenerator.PARAM_OUTPUT_PATH, outputFile, TestClassifierGenerator.PARAM_RELATION_NAME, "test");
        
        AnalysisEngineDescription exporter = AnalysisEngineFactory.createEngineDescription(TSVExporter.class, TSVExporter.PARAM_OUTPUT_PATH, outputFile);
        
        //SimplePipeline.runPipeline(reader, tokenizer, tagger, lemmatizer, dependency, arffGenerator, exporter);
        SimplePipeline.runPipeline(reader, tokenizer, tagger, lemmatizer, dependency, exporter);
	}
	
	public void createArff(String inputFile, String outputFile, String typeFile) throws UIMAException, IOException {
		 System.setProperty("DKPRO_HOME", System.getProperty("user.home")+"/Desktop/");
	        
		 	if(new File(modelFilePath).exists() && new File(arffFilePath).exists()) {
		 		//jump directly to learning
		 		
		 	}
		 
	        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
	                XmiReader.class, XmiReader.PARAM_LANGUAGE, "x-undefined",
	                XmiReader.PARAM_SOURCE_LOCATION,
	                inputFile,
	                XmiReader.PARAM_PATTERNS, typeFile,
	                XmiReader.PARAM_TYPE_SYSTEM_FILE, "src/main/resources/typesystem.xml");
	        
	        AnalysisEngineDescription report = AnalysisEngineFactory.createEngineDescription(TestClassifierGenerator.class, 
	        		TestClassifierGenerator.PARAM_OUTPUT_PATH, outputFile, 
	        		TestClassifierGenerator.PARAM_RELATION_NAME, "test");
	        
	        SimplePipeline.runPipeline(reader, report);
	}
	
	public void run_read() throws UIMAException, IOException {
		/*System.setProperty("DKPRO_HOME", System.getProperty("user.home")+"/Desktop/");
		
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                XmiReader.class, XmiReader.PARAM_LANGUAGE, "x-undefined",
                XmiReader.PARAM_SOURCE_LOCATION,
                inputFilePath,
                XmiReader.PARAM_PATTERNS, "*.xmi",
                XmiReader.PARAM_TYPE_SYSTEM_FILE, "src/main/resources/typesystem.xml");
        
        //AnalysisEngineDescription tokenizer = AnalysisEngineFactory.createEngineDescription(ClearNlpSegmenter.class, ClearNlpSegmenter.PARAM_LANGUAGE, "en");
		
        //AnalysisEngineDescription tagger = AnalysisEngineFactory.createEngineDescription(ClearNlpPosTagger.class, ClearNlpPosTagger.PARAM_LANGUAGE, "en");
        
        //AnalysisEngineDescription lemmatizer = AnalysisEngineFactory.createEngineDescription(ClearNlpLemmatizer.class, ClearNlpLemmatizer.PARAM_LANGUAGE, "en");
        
        //AnalysisEngineDescription dependency = AnalysisEngineFactory.createEngineDescription(MaltParser.class, MaltParser.PARAM_LANGUAGE, "en");
        
        AnalysisEngineDescription classifierHandler = AnalysisEngineFactory.createEngineDescription(ClassifierHandler.class,
    			ClassifierHandler.PARAM_ARFF_FILE, arffFilePath,
				ClassifierHandler.PARAM_MODEL_FILE, modelFilePath,
        		ClassifierHandler.PARAM_TSV_OUTPUT, tsvOutput);
        
        SimplePipeline.runPipeline(reader, classifierHandler);*/
	}
}
