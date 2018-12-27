package de.unidue.langtech.bachelor.meise.pipeline;

import java.io.IOException;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;

import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.maltparser.MaltParser;
import de.unidue.langtech.bachelor.meise.files.RawJsonReviewReader;

public class MainPipeline {
	
	String inputFilePath = "C:\\Users\\Jonas\\Documents\\Bachelorarbeit\\review_output.xml";
	String outputFilePath = "C:\\Users\\Jonas\\Documents\\Bachelorarbeit\\output.tsv";

	public MainPipeline() {
	}
	
	public static void main(String[] args) throws Exception {
		//MainPipeline myPipeline = new MainPipeline();
		//myPipeline.run_read();
		
		 System.setProperty("DKPRO_HOME", System.getProperty("user.home")+"/Desktop/");
	        
	        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
	                XmiReader.class, XmiReader.PARAM_LANGUAGE, "x-undefined",
	                XmiReader.PARAM_SOURCE_LOCATION,
	                "src/main/resources/",
	                XmiReader.PARAM_PATTERNS, "*.xmi",
	                XmiReader.PARAM_TYPE_SYSTEM_FILE, "src/main/resources/typesystem.xml");
	        
	        AnalysisEngineDescription report = AnalysisEngineFactory.createEngineDescription(TestReader.class);
	        
	        SimplePipeline.runPipeline(reader, report);
	}
	
	public void run() throws UIMAException, IOException {
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                RawJsonReviewReader.class, RawJsonReviewReader.PARAM_SOURCE_LOCATION, inputFilePath,
                RawJsonReviewReader.PARAM_LANGUAGE, "en");
		
        AnalysisEngineDescription tokenizer = AnalysisEngineFactory.createEngineDescription(ClearNlpSegmenter.class, ClearNlpSegmenter.PARAM_LANGUAGE, "en");
		
        AnalysisEngineDescription tagger = AnalysisEngineFactory.createEngineDescription(ClearNlpPosTagger.class, ClearNlpPosTagger.PARAM_LANGUAGE, "en");
        
        AnalysisEngineDescription lemmatizer = AnalysisEngineFactory.createEngineDescription(ClearNlpLemmatizer.class, ClearNlpLemmatizer.PARAM_LANGUAGE, "en");
        
        AnalysisEngineDescription dependency = AnalysisEngineFactory.createEngineDescription(MaltParser.class, MaltParser.PARAM_LANGUAGE, "en");
        
        AnalysisEngineDescription exporter = AnalysisEngineFactory.createEngineDescription(TSVExporter.class, TSVExporter.PARAM_OUTPUT_PATH, outputFilePath);
        
        SimplePipeline.runPipeline(reader, tokenizer, tagger, lemmatizer, dependency, exporter);
	}
	
	public void run_read() throws UIMAException, IOException {
		 System.setProperty("DKPRO_HOME", System.getProperty("user.home")+"/Desktop/");
	        
	        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
	                XmiReader.class, XmiReader.PARAM_LANGUAGE, "x-undefined",
	                XmiReader.PARAM_SOURCE_LOCATION,
	                "src/main/resources/",
	                XmiReader.PARAM_PATTERNS, "*.xmi",
	                XmiReader.PARAM_TYPE_SYSTEM_FILE, "src/main/resources/typesystem.xml");
	        
	        AnalysisEngineDescription report = AnalysisEngineFactory.createEngineDescription(TestReader.class);
	        
	        SimplePipeline.runPipeline(reader, report);
      }
}
