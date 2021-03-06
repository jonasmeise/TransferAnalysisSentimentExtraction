package de.unidue.langtech.bachelor.meise.type;

import java.io.File;
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
import de.unidue.langtech.bachelor.meise.files.RawJsonReviewReader;

public abstract class ArffGenerator extends JCasAnnotator_ImplBase{
	
	public static final String PARAM_OUTPUT_PATH = "outputPath";
    @ConfigurationParameter(name = PARAM_OUTPUT_PATH, mandatory = true)
    public String outputPath;
	public String stopwordsFile = "src/main/resources/stopwords.txt";
    public FileUtils fu;
    public int cutoff, id=0, numberOfFeatures, identifierAttributeAt;
    public int[] ignoreFeatures;
    public ConsoleLog myLog;
    public boolean learningModeActivated=false;
    public boolean outputIntoFolderActivated=false;
    public RawJsonReviewReader myReader;
	
	//Header for .arff file, 
	public static final String PARAM_CONSTRAINED = "paramConstrained";
    @ConfigurationParameter(name = PARAM_CONSTRAINED, mandatory = false)
	public String paramConstrained;
	public boolean constrained = true;
	
	public static final String PARAM_RELATION_NAME = "relationName";
    @ConfigurationParameter(name = PARAM_RELATION_NAME, mandatory = true)
	public String relationName;
    
	public static final String PARAM_USE_OLD_DATA = "paramUseOldData";
    @ConfigurationParameter(name = PARAM_USE_OLD_DATA, mandatory = false)
	public String paramUseOldData;
	public boolean useOldData = false;
	
	public static final String PARAM_IS_TEST_DATA = "paramIsTestData";
    @ConfigurationParameter(name = PARAM_IS_TEST_DATA, mandatory = false)
	public String paramIsTestData;
	public boolean isTestData = false;
	
	//every data-entry refers to a single line
	public ArrayList<ArrayList<String>> relations;
	public ArrayList<String> data; //matrix of Sentence/relation
	public ArrayList<String> allClassAttributes;
	
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
    	super.initialize(aContext);
    	
    	ignoreFeatures = new int[0];
    	fu = new FileUtils();
    	myLog = new ConsoleLog();
    	data = new ArrayList<String>();
    	allClassAttributes = new ArrayList<String>();
    	relations = new ArrayList<ArrayList<String>>();
    	    	
    	if(paramUseOldData!=null) {
    		useOldData = Boolean.valueOf(paramUseOldData);
    	} else {
    		useOldData = false;
    	}
    	
    	if(paramIsTestData!=null) {
    		isTestData = Boolean.valueOf(paramIsTestData);
    	} else {
    		isTestData = false;
    	}
    	
		relations = generateRelations();
		if(relations.size()>0) {
			numberOfFeatures = relations.get(0).size();
		}
		
    	if(paramConstrained!=null) {
    		constrained = Boolean.valueOf(paramConstrained);
    	} else {
    		constrained = true;
    	}
    	
    	try {
			if(!new File(outputPath).isDirectory()) {
				fu.createWriter(outputPath);
			} else {
				outputIntoFolderActivated=true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    //for non-pipelined access
    public ArffGenerator() {
    	ignoreFeatures = new int[0];
    	fu = new FileUtils();
    	myLog = new ConsoleLog();
    	data = new ArrayList<String>();
    	allClassAttributes = new ArrayList<String>();
    	relations = new ArrayList<ArrayList<String>>();
    	
		relations = generateRelations();
		if(relations.size()>0) {
			numberOfFeatures = relations.get(0).size();
		}
    }
    
    public void setOutputFile(String fileName) {
    	try {
			fu.createWriter(fileName);
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
    
	public String stringToFeatureName(String originalName) {
		//transforms "not too good" into "_not_too_good"
		return ("_" + originalName.replace(" ", "_"));
	}
	
	public String[] getAspectLabelsOldData() {
		String[] returnArray = new String[12];
		
		returnArray[0] = "restaurant#general";
		returnArray[1] = "restaurant#prices";
		returnArray[2] = "restaurant#miscellaneous";
		returnArray[3] = "food#prices";
		returnArray[4] = "food#quality";
		returnArray[5] = "food#style_options";
		returnArray[6] = "drinks#prices";
		returnArray[7] = "drinks#quality";
		returnArray[8] = "drinks#style_options";
		returnArray[9] = "ambience#general";
		returnArray[10] = "service#general";
		returnArray[11] = "location#general";
		
		return returnArray;
	}
	
    public void writeOutput(ArrayList<ArrayList<String>> sortedLines) {
    	myLog.log("Finished! Total of " + sortedLines.size() + " entries added.");
		//we'll write out own method...
		//writeOutput();
		
		//cycle through all the 
		myLog.log("Output into Folder activated: " + outputIntoFolderActivated);
		if(outputIntoFolderActivated) {
			myLog.log("Found that many different identifier models: " + allClassAttributes.size());
			
			for(String singleClass : allClassAttributes) {
				if(!isTestData) {
					setOutputFile(outputPath + "/" + singleClass + ".arff");
				} else {
					setOutputFile(outputPath + "/" + singleClass + ".arff.gold");
				}

				String completeOutput;
				completeOutput = "@relation " + singleClass + "\n\n";
	    		 
	    		//write all relational attributes
	    		for(int n=0;n<relations.get(0).size();n++) {
	    			
	    			String relation = relations.get(0).get(n);
	    			
	    			if(ignoreFeatures.length>0) {
	    				boolean allFine=true;
	    				for(int i=0;i<ignoreFeatures.length;i++) {
	    					if(ignoreFeatures[i]==n) {
	    						allFine=false;
	    					}
	    				}
	    				
	    				if(allFine) {				
	    					completeOutput = completeOutput + "@attribute " + relation + "\n";
	    				}
	    			} else {
	    				completeOutput = completeOutput + "@attribute " + relation + "\n";
	    			}
	    		}
	    		
	    		completeOutput = completeOutput + "\n@data\n";
	    		
				
				for(ArrayList<String> singleLine : sortedLines) {
					if(singleLine.get(identifierAttributeAt)!=null) {						
						if(singleLine.get(identifierAttributeAt).compareTo(singleClass)==0) {
							completeOutput = completeOutput + generateArffLine(singleLine) + "\n";
						}
					}
				}
				
				fu.write(completeOutput.substring(0, completeOutput.length()-1));
				myLog.log("Wrote to '" + outputPath + "/" + singleClass + ".arff" + "'");
				
				
				fu.close();
			}
		} else {
			myLog.log("Output path not a folder, can't generate output files....");
		}
    }
    
    public String clearString(String string) {
    	return string.replace("\"", "").replace("'", "");
    }
    
    public String generateArffLine(ArrayList<String> features) {
    	String returnString = "";
    	
    	if(features.size()!=numberOfFeatures) {
    		myLog.log("WARNING: Wrong number of features! Expected " + numberOfFeatures + ", found " + features.size() + " instead!");
    	}
    	
		for(int n=0;n<features.size();n++) {
			String feature = features.get(n);
			
			if(ignoreFeatures.length>0) {
				boolean allFine=true;
				for(int i=0;i<ignoreFeatures.length;i++) {
					if(ignoreFeatures[i]==n) {
						allFine=false;
					}
				}
				
				if(allFine) {				
					returnString = returnString + feature + ",";
				}
			} else {
				returnString = returnString + feature + ",";
			}
		}
		
    	return returnString.substring(0,returnString.length()-1);
    }
    
    public abstract Collection<ArrayList<String>> generateFeaturesFromCas(Sentence sentence);
    
    public abstract ArrayList<ArrayList<String>> generateRelations();
    
    public void generateData(JCas arg0, int cutoff) {
    	int counter=0;
    	for(Sentence sentence : JCasUtil.select(arg0, Sentence.class)) {
    		if(counter<cutoff) {
	    		Collection<ArrayList<String>> newData = generateFeaturesFromCas(sentence);
	    		
	    		if(!newData.isEmpty()) {
		    		for(ArrayList<String> dataLine : newData) {
			    		if(!dataLine.isEmpty()) {
			    			data.add(generateArffLine(dataLine));
			    		}
		    		}
	    		}
    		}
    		counter++;
    	}
    }
    
    public ArrayList<Token> getContext(Sentence sentence, Collection<Tree<Token>> sentences, int contextWidth, int maxReturnSize, Collection<Token> searchTokens) {
    	ArrayList<Token> returnList = new ArrayList<Token>();
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
	    					&& !returnList.contains(sentenceOrder.get(pos+searchArray))) {//check if it's already contained)) {//check if it's not a source Token
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
    
    public Collection<ArrayList<Token>> divideIntoSubSentences(Sentence sentence, Collection<Tree<Token>> allTrees) {
    	Collection<ArrayList<Token>> returnList = new ArrayList<ArrayList<Token>>();
    	
    	for(Tree<Token> tree : allTrees) {
    		Collection<Token> currentTokens = tree.getAllObjects();
    		ArrayList<Token> sentenceOrder = new ArrayList<Token>();
        	
        	//Sorted in-place insertion; Ascending Tokens from 0 (first Token in sentence) to n (last Token)
        	for(Token token : currentTokens) {
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
    	    					break;
    	    				}
    	    			}
        			}
        		}
        	}
        	
    		returnList.add(sentenceOrder);
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
	}	
	
    public boolean isLearningModeActivated() {
		return learningModeActivated;
	}

	public void setLearningModeActivated(boolean learningModeActivated) {
		this.learningModeActivated = learningModeActivated;
	}

}
