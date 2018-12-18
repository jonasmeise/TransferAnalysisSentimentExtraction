package de.unidue.langtech.bachelor.meise.pipeline;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.dkpro.core.io.webanno.tsv.WebannoTsv3XReader;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpParser;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.maltparser.MaltParser;
import de.unidue.langtech.bachelor.meise.files.JCasReader;
import de.unidue.langtech.bachelor.meise.files.RawJsonReviewReader;

public class MainPipeline {
	
	String inputFilePath = "C:\\Users\\Jonas\\Documents\\Bachelorarbeit\\review_output.xml";
	String outputFilePath = "C:\\Users\\Jonas\\Documents\\Bachelorarbeit\\output.tsv";

	public MainPipeline() {
	}
	
	public static void main(String[] args) throws Exception {
		MainPipeline myPipeline = new MainPipeline();
		myPipeline.run_read();
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
		 TypeSystemDescription core = TypeSystemDescriptionFactory.createTypeSystemDescription();
		 TypeSystemDescription custom = TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath("typesystem.xml");
	     List<TypeSystemDescription> systemsList = Arrays.asList(core, custom);
	     TypeSystemDescription merged = CasCreationUtils.mergeTypeSystems(systemsList);
	     
	     CollectionReaderDescription tsvReader= CollectionReaderFactory.createReaderDescription(
	             WebannoTsv3XReader.class,
	             merged,
	             WebannoTsv3XReader.PARAM_SOURCE_LOCATION, "output.xmi",
	               WebannoTsv3XReader.PARAM_LANGUAGE, "en"
	      );

	      for (JCas jcas : new JCasIterable(tsvReader)) {
	          int i=0;
	          for(Sentence sent : JCasUtil.select(jcas, Sentence.class)){
	        	  System.out.println(sent.getCoveredText());
	          }
          }
      }
}
