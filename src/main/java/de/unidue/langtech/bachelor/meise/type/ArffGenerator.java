package de.unidue.langtech.bachelor.meise.type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unidue.langtech.bachelor.meise.extra.ConsoleLog;
import de.unidue.langtech.bachelor.meise.files.FileUtils;

public abstract class ArffGenerator extends JCasAnnotator_ImplBase{
	
	public static final String PARAM_OUTPUT_PATH = "outputPath";
    @ConfigurationParameter(name = PARAM_OUTPUT_PATH, mandatory = true)
    private String outputPath;
    private FileUtils fu;
    public int cutoff, id=0, numberOfFeatures;
    private ConsoleLog myLog;
    public boolean learningModeActivated=false;
	
	//Header for .arff file, 
	public static final String PARAM_RELATION_NAME = "relationName";
    @ConfigurationParameter(name = PARAM_RELATION_NAME, mandatory = true)
	public String relationName;
	
	//every data-entry refers to a single line
	public ArrayList<String> relations;
	public ArrayList<String> data; //matrix of Sentence/relation
	
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
    	super.initialize(aContext);
    	fu = new FileUtils();
    	myLog = new ConsoleLog();
    	data = new ArrayList<String>();
    	
		relations = generateRelations();
    	numberOfFeatures = relations.size();
		
    	try {
			fu.createWriter(outputPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	public String generateTupel(String[] tupel) {
    	String returnString;
    	if(tupel.length > 0) {
    		returnString = "{" + tupel[0];
    		
    		for(int i=1;i<tupel.length;i++) {
    			returnString = returnString + ", " + tupel[i];
    		}
    		
    		returnString = returnString + "}";
    	} else {
    		returnString = "{}";
    	}
    	
    	return returnString;
    }
    
    public void writeOutput() {
    	String completeOutput;
    	int counter=0;
    	
    	if(fu.fileWriter!=null) {	
    		completeOutput = "@relation " + relationName + "\n\n";
    		 
    		//write all relational attributes
    		for(String relation : relations) {
    			completeOutput = completeOutput + "@attribute " + relation + "\n";
    		}
    		
    		completeOutput = completeOutput + "\n@data\n";
    		
    		//write all dataa
    		for(String dataLine : data) {
    			myLog.log(counter + "/" + data.size());
    			completeOutput = completeOutput + dataLine + "\n";
    			counter++;
			}
    			
    		fu.write(completeOutput);
  
		}
    	else {
    		myLog.log("Writer isn't available!");
    	}
    	
    	fu.close();
    }
    
    public String clearString(String string) {
    	return string.replace("\"", "?").replace("'", "?");
    }
    
    public String generateArffLine(Collection<String> features) {
    	String returnString = "";
    	
    	if(features.size()!=numberOfFeatures) {
    		myLog.log("Wrong number of features! Expected " + numberOfFeatures + ", found " + features.size() + " instead!");
    		return null;
    	} else {
    		for(String feature : features) {
    			returnString = returnString + feature + ",";
    		}
    	}
    	
    	return returnString.substring(0,returnString.length()-1);
    }
    
    public abstract Collection<ArrayList<String>> generateFeaturesFromCas(Sentence sentence);
    
    public abstract ArrayList<String> generateRelations();
    
    public void generateData(JCas arg0, int cutoff) {
    	int counter=0;
    	for(Sentence sentence : JCasUtil.select(arg0, Sentence.class)) {
    		if(counter<cutoff) {
	    		Collection<ArrayList<String>> newData = generateFeaturesFromCas(sentence);
	    		
	    		if(!newData.isEmpty()) {
		    		for(Collection<String> dataLine : newData) {
			    		if(!dataLine.isEmpty()) {
			    			data.add(generateArffLine(dataLine));
			    		}
		    		}
	    		}
    		}
    		counter++;
    	}
    }
    
    public Collection<Token> getContext(Sentence sentence, Collection<Tree<Token>> sentences, int contextWidth, int maxReturnSize, Collection<Token> searchTokens) {
    	Collection<Token> returnList = new ArrayList<Token>();
    	Collection<Token> tokens = selectCovered(Token.class, sentence);
    	ArrayList<Token> sentenceOrder = new ArrayList<Token>();
    	
    	//Sorted in-place insertion; Ascending Tokens from 0 (first Token in sentence) to n (last Token)
    	for(Token token : tokens) {
    		if(sentenceOrder.size()==0) { //start
    			sentenceOrder.add(token);
    		} else {
    			if(sentenceOrder.get(sentenceOrder.size()-1).getBegin()<=token.getBegin()) { //no search needed
    				sentenceOrder.add(token);
    			} else {
	    			for(int i=0;i<sentenceOrder.size();i++) {
	    				Token comparisonToken = sentenceOrder.get(i);
	    				
	    				if(token.getBegin() < comparisonToken.getBegin()) {
	    					sentenceOrder.add(i, token);
	    				}
	    			}
    			}
    		}
    	}

    	for(int pos=0;pos<sentenceOrder.size();pos++) {
        	for(Token toFind : searchTokens) {
	    		if(sentenceOrder.get(pos).equals(toFind)) {
	    			for(int searchArray=-contextWidth;searchArray<=contextWidth;searchArray++) {	
		    			if(isInRange(0, sentenceOrder.size()-1, pos+searchArray) //check if it's out of border
	    					&& !returnList.contains(sentenceOrder.get(pos+searchArray))  //check if it's already contained
		    				&& !searchTokens.contains(sentenceOrder.get(pos+searchArray))) {//check if it's not a source Token
		    					for(Tree<Token> singleSentence : sentences) { //check if both tokens are actually part of the same sentence
		    						if(singleSentence.tokenDistanceInTree(toFind, sentenceOrder.get(pos+searchArray))>=0) {
		    							returnList.add(sentenceOrder.get(pos+searchArray));
		    						}
		    					}
		    			}
	    			}
	    		}
        	}
    	}
    	
    	return returnList;
    }
    
    public boolean isInRange(int min, int max, int value) {
    	return(value>=min && value<=max);
    }
    
	@Override
	public abstract void process(JCas arg0) throws AnalysisEngineProcessException;	
	
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		myLog.log("Finished! Total of " + data.size() + " entries added.");
		writeOutput();
	}	
	
    public boolean isLearningModeActivated() {
		return learningModeActivated;
	}

	public void setLearningModeActivated(boolean learningModeActivated) {
		this.learningModeActivated = learningModeActivated;
	}

}
