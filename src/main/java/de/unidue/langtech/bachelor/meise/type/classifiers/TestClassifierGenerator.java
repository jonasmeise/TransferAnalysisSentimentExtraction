package de.unidue.langtech.bachelor.meise.type.classifiers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.unidue.langtech.bachelor.meise.type.ArffGenerator;
import de.unidue.langtech.bachelor.meise.type.Tree;
import webanno.custom.AspectRating;
import webanno.custom.Valence;

public class TestClassifierGenerator extends ArffGenerator{

	int cutoff = 200;
	int valueId = 0;
	
	@Override
	public ArrayList<String> generateRelations() {
		ArrayList<String> relations = new ArrayList<String>();
		
		relations.add("id numeric");
		relations.add("text string");
		relations.add("tokendistance numeric");
		relations.add("dependencydistance numeric");
		relations.add("pos string");
		//relations.add("emotionalrating numeric");
		//relations.add("negated boolean");
		
		String[] types = new String[18];
		types[0] = "Ausstattung-positive";
		types[1] = "Hotelpersonal-positive";
		types[2] = "Lage-positive";
		types[3] = "OTHER-positive";
		types[4] = "Komfort-positive";
		types[5] = "Preis-Leistungs-Verhaeltnis-positive";
		types[6] = "WLAN-positive";
		types[7] = "Sauberkeit-positive";
		types[8] = "Ausstattung-negative";
		types[9] = "Hotelpersonal-negative";
		types[10] = "Lage-negative";
		types[11] = "OTHER-negative";
		types[12] = "Komfort-negative";
		types[13] = "Preis-Leistungs-Verhaeltnis-negative";
		types[14] = "WLAN-negative";
		types[15] = "Sauberkeit-negative";
		types[16] = "RatingOfAspect";
		types[17] = "NONE";
		
		relations.add("aspecttype " + generateTupel(types));
		
		return relations;
	}

	@Override
	public ArrayList<ArrayList<String>> generateData(JCas arg0) {
		
		System.out.println("OKAY");
		
		ArrayList<ArrayList<String>> returnList = new ArrayList<ArrayList<String>>();
		Collection<Sentence> sentences = JCasUtil.select(arg0, Sentence.class);
		
        for(Sentence sentence : sentences) {
    		if(id < cutoff) {
	        	System.out.println(sentence.getCoveredText());
	        	//TODO: Inner-sentence functions, Word Distance, Parsing Tree....
	        	Collection<Dependency> dependencies =  selectCovered(Dependency.class, sentence);
	        	Collection<Tree<Token>> treeCollection = new ArrayList<Tree<Token>>();
	        	
	        	//find root
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
	        	
	        	ArrayList<String> singleLine = new ArrayList<String>();
	    		Collection<Token> tokens = selectCovered(Token.class, sentence);
	    		
	        	Collection<Valence> valences = selectCovered(Valence.class, sentence);
	            HashSet<Integer> h = new HashSet<Integer>(); 
	        	
	            for(Valence valence : valences) {
	            	//unique Token-Token identifier
	            	h.add(valence.getDependent().getBegin() * 1000 + (valence.getGovernor().getBegin()));  	
	            }
	            
	            int checkValue, checkValueAlt, dependencyDistance=0, tokenDistance=0;
	        	//generate Tokens-Relationship
	        	for(Token t1 : tokens) {
	        		for(Token t2 : tokens) {
	        			singleLine = new ArrayList<String>();
	        			checkValue = t1.getBegin() * 1000 + (t2.getBegin());
	        			checkValueAlt = t2.getBegin() * 1000 + (t1.getBegin());
	        			
	        			for(Tree<Token> tree : treeCollection) {
	        				dependencyDistance = tree.tokenDistanceInTree(t1, t2);
	        				if(dependencyDistance >= 0) {
	        					//System.out.println(t1.getCoveredText() + " - " + t2.getCoveredText() + " = " + dependencyDistance);
	        				}
	        			}
	        			
	        			//dependencyDistance > 0 ==> has to be in the same sentence
	        			if(!h.contains(checkValue) && !h.contains(checkValueAlt) && dependencyDistance >= 0) { //is not already Dependency relation
	        				for(Token sentenceToken : tokens) {
	        					if((sentenceToken.getBegin() > t1.getBegin() && sentenceToken.getBegin() < t2.getBegin()) ||
	        						(sentenceToken.getBegin() > t2.getBegin() && sentenceToken.getBegin() < t1.getBegin())) {
	        						tokenDistance++;
	        					}
	        				}
	        				
	        				//add actual data
	        				singleLine.add("" + valueId);
	        				valueId++;
	        				singleLine.add("'" + t1.getCoveredText() + " " + t2.getCoveredText() + "'");
	        				singleLine.add("" + tokenDistance);
	        				singleLine.add("" + dependencyDistance);
	        				singleLine.add("'" + t1.getPos().getPosValue() + " " + t2.getPos().getPosValue() + "'");
	        				singleLine.add("NONE");
	        				
	        	        	returnList.add(singleLine);
	        			}
	        		}
	        	}

	        	for(Valence valence : selectCovered(Valence.class, sentence)) {
	        		AspectRating t1 = valence.getDependent();
	        		AspectRating t2 = valence.getGovernor();
	        		
		        	dependencyDistance=-1;
		        	tokenDistance=0;
	        		
	        		singleLine = new ArrayList<String>();
	        		
	        		for(Tree<Token> tree : treeCollection) {
        				int newDistance = tree.tokenDistanceInTree(selectCovered(Token.class, t1).get(0),selectCovered(Token.class, t2).get(0));
        				if(newDistance > dependencyDistance) {
        					dependencyDistance = newDistance;
        					//System.out.println(t1.getCoveredText() + " - " + t2.getCoveredText() + " = " + dependencyDistance);
        				}
        			}
        			
	        		System.out.println(t1.getCoveredText() + " -> " + t2.getCoveredText() + " >" + dependencyDistance);
	        		
	        		if(dependencyDistance >= 0) {
	        			//dependencyDistance > 0 ==> has to be in the same sentence
	    				for(Token sentenceToken : tokens) {
	    					if((sentenceToken.getBegin() > t1.getBegin() && sentenceToken.getBegin() < t2.getBegin()) ||
	    						(sentenceToken.getBegin() > t2.getBegin() && sentenceToken.getBegin() < t1.getBegin())) {
	    						tokenDistance++;
	    					}
	    				}
	    				
	    				//add actual data
	    				singleLine.add("" + valueId);
	    				valueId++;
	    				singleLine.add("'" + t1.getCoveredText() + " " + t2.getCoveredText() + "'");
	    				singleLine.add("" + tokenDistance);
	    				singleLine.add("" + dependencyDistance);			
	    				singleLine.add("'" + selectCovered(Token.class, t1).get(0).getPos().getPosValue() + " " + selectCovered(Token.class, t2).get(0).getPos().getPosValue() + "'");
	    				
	    				if(t1.getAspect() != null) {
	    					String currentValence=valence.getValenceRating();
	    					if(valence.getValenceRating()==null) {
	    						currentValence="positive";
	    					}
	    					
		    				if(t1.getAspect().toLowerCase().compareTo("ratingofaspect")==0) {
		    					singleLine.add(t2.getAspect().replace("?", "ae") + "-" + currentValence);
		    				} else {
		    					singleLine.add(t1.getAspect().replace("?", "ae") + "-" + currentValence);
		    				}
	    				} else {
	    					singleLine.add("NONE");
	    				}
	    				
	    	        	returnList.add(singleLine);
	        		}
	        	}
	        	id++;
    		}
        }
        
		return returnList;
	}

	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
			data.addAll(this.generateData(arg0));
			id=0;
	}
}
