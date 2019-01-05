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
		relations.add("dependencytreedistance numeric");
		relations.add("emotionalrating numeric");
		//relations.add("negated boolean");
		
		String[] types = new String[16];
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
		        	
		        	System.out.println(tree.printTree(0));
	        	}
	        	
	        	ArrayList<String> singleLine = new ArrayList<String>();
	    		ArrayList<Token> tokens = new ArrayList<Token>();
	        	
	    		for(Token token : selectCovered(Token.class, sentence)) {
	    			tokens.add(token);
	    		}
	    		
	        	Collection<Valence> valences = selectCovered(Valence.class, sentence);
	            HashSet<Integer> h = new HashSet<Integer>(); 
	        	
	            for(Valence valence : valences) {
	            	//unique Token-Token identifier
	            	h.add(valence.getDependent().getBegin() * 1000 + (valence.getGovernor().getBegin()));  	
	            }
	            
	            int checkValue, checkValueAlt, distance;
	        	//generate Tokens-Relationship
	        	for(Token t1 : tokens) {
	        		for(Token t2 : tokens) {
	        			checkValue = t1.getBegin() * 1000 + (t2.getBegin());
	        			checkValueAlt = t2.getBegin() * 1000 + (t1.getBegin());
	        			
	        			for(Tree<Token> tree : treeCollection) {
	        				distance = tree.tokenDistanceInTree(t1, t2);
	        				if(distance >= 0) {
	        					System.out.println(t1.getCoveredText() + " - " + t2.getCoveredText() + " = " + distance);
	        				}
	        			}
	        			
	        			if(!h.contains(checkValue) && !h.contains(checkValueAlt)) { //is not an already Dependency relation
	        				//add actual data
	        				singleLine.add("" + valueId);
	        				valueId++;
	        				singleLine.add(t1.getCoveredText() + " " + t2.getCoveredText());
	        				singleLine.add("" + Math.abs((t1.getBegin() - t2.getBegin()))); //TODO: actual "word" spacing distance, not raw distance
	        				
	        			}
	        		}
	        	}
	        	
	        	returnList.add(singleLine);
	        	id++;
    		}
        }
		return returnList;
	}

	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
			data.addAll(this.generateData(arg0));
	}
}
