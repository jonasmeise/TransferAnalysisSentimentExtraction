package de.unidue.langtech.bachelor.meise.pipeline;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.unidue.langtech.bachelor.meise.extra.ConsoleLog;
import de.unidue.langtech.bachelor.meise.files.FileUtils;
import de.unidue.langtech.bachelor.meise.type.Tree;
import webanno.custom.AspectRating;
import webanno.custom.Valence;

public class DataStatistics extends JCasAnnotator_ImplBase{
	//Path of the output file
	public static final String PARAM_OUTPUT_PATH = "outputPath";
    @ConfigurationParameter(name = PARAM_OUTPUT_PATH, mandatory = true)
	private String outputPath;
	
	FileUtils fu;
	int counterSentence, counterReview, counterToken, counterValence;
	String[] types;
	int[][] aspectMatrix;
	ArrayList<HashMap<String, Integer>> allMaps;
	int seen ;
	
	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
		if(seen <= 200000) {
			for(Sentence sentence : JCasUtil.select(arg0, Sentence.class)) {
				System.out.println(seen + "\t" + sentence.getCoveredText());
				seen++;
				counterReview++;
				Collection<Dependency> dependencies =  selectCovered(Dependency.class, sentence);
	        	Collection<Tree<Token>> treeCollection = new ArrayList<Tree<Token>>();
				
	        	ArrayList<Token> roots = new ArrayList<Token>();
	        	for(Dependency dpElement : dependencies) {
					if(dpElement.getDependencyType().toLowerCase().compareTo("root") == 0) {
						roots.add(dpElement.getGovernor());
					}
				}
	        	
	        	for(Token root : roots) {
		        	Tree<Token> tree = new Tree<Token>(root);
		        	tree.generateTreeOfDependency(dependencies, root);
		        	tree.setParentDependencyType("ROOT");
		        	
		        	treeCollection.add(tree);
	        	}	
	        	
	        	Collection<ArrayList<Token>> subSentences = divideIntoSubSentences(sentence, treeCollection);
	        	
	        	//for each-subsentence
	        	for(ArrayList<Token> subSentence : subSentences) {
	        		counterSentence++;
	        		counterToken += subSentence.size();
	        		
	        		for(Valence valence : selectCovered(Valence.class, sentence)) {
	            		AspectRating t1 = valence.getDependent();
	            		AspectRating t2 = valence.getGovernor();

	    				if(subSentence.contains(selectCovered(Token.class, t1).get(0)) && subSentence.contains(selectCovered(Token.class, t2).get(0))) {
	    					//actually in the current sentence
	    					String currentValence=valence.getValenceRating();
	    					String identifier;
	    					String aspect;
	    					String rating;
	    					
	    					if(valence.getValenceRating()==null) {
	    						currentValence="positive";
	    					}
	    					
	    					if(t1.getAspect()!=null && t2.getAspect()!=null) {
	        					counterValence++;        					
	    						
	    						if(t1.getAspect().toLowerCase().compareTo("ratingofaspect")==0) {
	    	    					//non-negated
	    							identifier = t2.getAspect().replaceAll("[^\\x00-\\x7F]", "") + "-" + currentValence;
	    							aspect=t2.getCoveredText();
	    							rating=t1.getCoveredText();
	    	    				} else {
	    	    					//negated
	    	    					identifier = t1.getAspect().replaceAll("[^\\x00-\\x7F]", "") + "-" + currentValence;
	    	    					aspect=t1.getCoveredText();
	    							rating=t2.getCoveredText();
	    	    				}
	    						
	    						for(int i=0;i<types.length;i++) {
	    							if(identifier.equals(types[i])) {
	    								aspectMatrix[i][1]++; 
	    								
	    								if(allMaps.get(i).containsKey("aspect-" + aspect.toLowerCase())) {
	    									allMaps.get(i).put("aspect-" + aspect.toLowerCase(), allMaps.get(i).get("aspect-" + aspect.toLowerCase())+1);
	    								} else {
	    									allMaps.get(i).put("aspect-" + aspect.toLowerCase(), 1);
	    								}
	    								
	    								if(allMaps.get(i).containsKey("rating-" + rating.toLowerCase())) {
	    									allMaps.get(i).put("rating-" + rating.toLowerCase(), allMaps.get(i).get("rating-" + rating.toLowerCase())+1);
	    								} else {
	    									allMaps.get(i).put("rating-" + rating.toLowerCase(), 1);
	    								}
	    							}
	    						}
	    					}	
	    				}
	        		}
	        	}
			}
		}
	}

	public ArrayList<String> findTop(HashMap<String, Integer> hashMap, int n) {
		ArrayList<String> returnList = new ArrayList<String>();
		int threshold=0, amount=n+1;
		
		while(amount>=n) {
			amount=0;
			Collection<Integer> pureNumbers = hashMap.values();
			for(Integer i: pureNumbers) {
				if(i >= threshold) {
					amount++;
				}
			}
			threshold++;
		}
		
		for(String string : hashMap.keySet()) {
			if(hashMap.get(string)>threshold) {
				returnList.add(string);
			}
		}
		
		return returnList;
	}
	
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
    	fu = new FileUtils();
    	counterSentence=0;
    	counterReview=0;
    	counterToken=0;
    	counterValence=0;
    	seen=0;
    	
    	types = new String[16];
    	
		types[0] = "Ausstattung-positive";
		types[1] = "Hotelpersonal-positive";
		types[2] = "Lage-positive";
		types[3] = "OTHER-positive";
		types[4] = "Komfort-positive";
		types[5] = "Preis-Leistungs-Verhltnis-positive";
		types[6] = "WLAN-positive";
		types[7] = "Sauberkeit-positive";
		types[8] = "Ausstattung-negative";
		types[9] = "Hotelpersonal-negative";
		types[10] = "Lage-negative";
		types[11] = "OTHER-negative";
		types[12] = "Komfort-negative";
		types[13] = "Preis-Leistungs-Verhltnis-negative";
		types[14] = "WLAN-negative";
		types[15] = "Sauberkeit-negative";
		
		aspectMatrix = new int[types.length][2];
		
		allMaps = new ArrayList<HashMap<String, Integer>>();
		for(int i=0;i<types.length;i++) {
			allMaps.add(new HashMap<String, Integer>());
		}
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
	
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		int i=0;
		try {
			fu.createWriter("src/main/resources/learningtest_AKTSKI/sourceData.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		fu.write("#reviews\t" + counterReview);
		fu.write("#sentence\t" + counterSentence);
		fu.write("#token\t" + counterToken);
		fu.write("#relations\t" + counterValence + "\n");
		
		for(HashMap<String, Integer> currentMap : allMaps) {
			Collection<String> toPrint = findTop(currentMap, 30);
			
			fu.write("---" + types[i] + "---");
			for(String string : toPrint) {
				fu.write(string);
			}
			fu.write("\n\n");
			i++;
		}
		
		for(int n=0;n<types.length;n++) {
			fu.write(types[n] + "\t" + aspectMatrix[n][1]);
		}
		
		fu.close();
	}	
}
