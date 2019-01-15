package de.unidue.langtech.bachelor.meise.type.classifiers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
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

	int cutoff = 200; //Maximale Saetze/Datensatz
	int dataCutoff = 0; //Maximale Datenentries pro Datensatz
	int valueId=0;
	
	ArrayList<ArrayList<String>> sortedLines = new ArrayList<ArrayList<String>>();
	
	@Override
	public ArrayList<ArrayList<String>> generateRelations() {
		classAttributeAt = 2;
		
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
		
		for(int i=0;i<types.length;i++) {
			ArrayList<String> relations = new ArrayList<String>();
			
			relations.add("id numeric");
			relations.add("text string");
			relations.add("type string");
			
			relations.add("aspecttype " + generateTupel(new String[] {"true", "false"}));	
			
			allClassAttributes.add(types[i]);
			
			ArrayList<String> newList = new ArrayList<String>();
			newList.add(types[i]);
			
			this.relations.add(relations);
		}
		
		return this.relations;
	}

	@Override
	public Collection<ArrayList<String>> generateFeaturesFromCas(Sentence sentence) {
		Collection<ArrayList<String>> returnList = new ArrayList<ArrayList<String>>();
		
		if(valueId < dataCutoff || dataCutoff==0) { //check if max amount of data is enabled
        	/*System.out.println(sentence.getCoveredText());
        	Collection<Dependency> dependencies =  selectCovered(Dependency.class, sentence);
        	Collection<Tree<Token>> treeCollection = new ArrayList<Tree<Token>>();
        	
        	//find root and sentence start
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
            
            int checkValue, checkValueAlt, dependencyDistance=0, tokenDistance;
        	//generate Tokens-Relationship
        	for(Token t1 : tokens) {
        		for(Token t2 : tokens) {
        			tokenDistance=-1;
        			singleLine = new ArrayList<String>();
        			checkValue = t1.getBegin() * 1000 + (t2.getBegin());
        			checkValueAlt = t2.getBegin() * 1000 + (t1.getBegin());
        			
        			for(Tree<Token> tree : treeCollection) {
        				dependencyDistance = tree.tokenDistanceInTree(t1, t2);
        			}
        			
        			//dependencyDistance > 0 ==> has to be in the same sentence
        			if(!h.contains(checkValue) && !h.contains(checkValueAlt) && dependencyDistance >= 0) { //is not already Dependency relation
        				for(Token sentenceToken : tokens) {
        					if((sentenceToken.getBegin() >= t1.getBegin() && sentenceToken.getBegin() <= t2.getBegin()) ||
        						(sentenceToken.getBegin() >= t2.getBegin() && sentenceToken.getBegin() <= t1.getBegin())) {
        						tokenDistance++;
        					}
        				}
        				
        				//add actual data
        				singleLine.add("" + valueId);
        				singleLine.add("'_" + t1.getCoveredText().replace("\"", "").replace("'","") + " _" + t2.getCoveredText().replace("\"", "").replace("'","") + "'");
        				singleLine.add("" + tokenDistance);
        				singleLine.add("" + dependencyDistance);
        				singleLine.add(new String("'" + t1.getPos().getPosValue() + " " + t2.getPos().getPosValue() + "'").replace("''", ""));
        				
        				//context-Generator
        				Collection<Token> originalTokens = new ArrayList<Token>();
        				originalTokens.add(t1);
        				originalTokens.add(t2);
        				
        				Collection<Token> context = getContext(sentence,treeCollection, contextWidth, 200, originalTokens);
        				String contextString = "";
        				for(Token contextToken : context) {
        					contextString = contextString + contextToken.getCoveredText() + " ";
        				}
        				
        				if(contextString.length()>1) {
        					singleLine.add("'" + contextString.substring(0,contextString.length()-1).replace("\"", "").replace("'","") + "'");
        				} else {
        					singleLine.add("''");
        				}
	        				
        				if(!learningModeActivated) {
	        				singleLine.add("NONE");
        				} else {
        					singleLine.add("?");
        				}
        				
        	        	returnList.add(singleLine);
        	        	valueId++;
        			}
        		}
        	}

        	//copy-pasta'd since Valence can't be cast to Token....
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
    				System.out.println(t1.getAspect() + "(T1) -> " + t2.getAspect() + "(T2)");
    				singleLine.add("'_" + t1.getCoveredText().replace("\"", "").replace("'","") + " _" + t2.getCoveredText().replace("\"", "").replace("'","") + "'");
    				singleLine.add("" + tokenDistance);
    				singleLine.add("" + dependencyDistance);			
    				singleLine.add(new String("'" + selectCovered(Token.class, t1).get(0).getPos().getPosValue() + " " + selectCovered(Token.class, t2).get(0).getPos().getPosValue() + "'").replace("''", ""));
    				
    				//context-Generator
    				Collection<Token> originalTokens = new ArrayList<Token>();
    				originalTokens.add(selectCovered(Token.class,t1).get(0));
    				originalTokens.add(selectCovered(Token.class,t2).get(0));
    				
    				Collection<Token> context = getContext(sentence,treeCollection, contextWidth, 200, originalTokens);
    				String contextString = "";
    				for(Token contextToken : context) {
    					contextString = contextString + contextToken.getCoveredText() + " ";
    				}
    				
    				if(contextString.length()>1) {
    					singleLine.add("'" + contextString.substring(0,contextString.length()-1).replace("\"", "").replace("'","") + "'");
    				} else {
    					singleLine.add("''");
    				}
    				
    				
    				if(t1.getAspect() != null && valence.getValenceRating()!=null && !learningModeActivated) {
    					String currentValence=valence.getValenceRating();
    					if(valence.getValenceRating()==null) {
    						currentValence="positive";
    					}
    					
	    				if(t1.getAspect().toLowerCase().compareTo("ratingofaspect")==0) {
	    					//non-negated
	    					singleLine.add(t2.getAspect() + "-" + currentValence);
	    				} else {
	    					//negated
	    					singleLine.add(t1.getAspect() + "-" + currentValence);
	    				}
    				} else if(learningModeActivated){
    					singleLine.add("?");
    				} else {
    					singleLine.add("NONE");
    				}

    				
    	        	returnList.add(singleLine);
    	        	valueId++;
        		}
        	}*/

        	Collection<Dependency> dependencies =  selectCovered(Dependency.class, sentence);
        	Collection<Tree<Token>> treeCollection = new ArrayList<Tree<Token>>();
        	Collection<ArrayList<Token>> subSentences = new ArrayList<ArrayList<Token>>();
			
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
	        	subSentences.add(tree.getAllObjects());	
        	}
	        	
        	
        	subSentences = divideIntoSubSentences(sentence, treeCollection);
        	
        	//generate all outputs, then overwrite specific ones
        	for(String singleClass : allClassAttributes) {
        		for(ArrayList<Token> singleSentence : subSentences) {
        			String currentSentence = "";
        			for(Token singleToken : singleSentence) {
        				currentSentence = currentSentence + singleToken.getCoveredText() + " ";
        			}
        			
					ArrayList<String> singleLine = new ArrayList<String>();
					singleLine.add("" + valueId);
					singleLine.add("'" + currentSentence.replace("\"", "").replace("'","") + "'");
					singleLine.add(singleClass);
					
					if(!learningModeActivated) {
						singleLine.add("false");
					} else {
						singleLine.add("?");
					}
					
					valueId++;
					
					returnList.add(singleLine);
					sortedLines.add(singleLine);
        		}
        	}
        	
			for(Valence valence : selectCovered(Valence.class, sentence)) {
        		AspectRating t1 = valence.getDependent();
        		AspectRating t2 = valence.getGovernor();
        		
				int dependencyDistance = -1;
				
				for(Tree<Token> tree : treeCollection) {
    				int newDistance = tree.tokenDistanceInTree(selectCovered(Token.class, t1).get(0),selectCovered(Token.class, t2).get(0));
    				
    				if(newDistance > dependencyDistance) {
    					dependencyDistance = newDistance;
    				}
    			}
				
				if(dependencyDistance>=0) {
					ArrayList<Token> currentSentence = new ArrayList<Token>();
	        		for(ArrayList<Token> subSentence : subSentences) {
	        			for(Token singleToken : subSentence) {
	        				if(singleToken.equals(selectCovered(Token.class, t1).get(0)) || singleToken.equals(selectCovered(Token.class, t2).get(0))) {
	        					currentSentence = subSentence;
	        				}
	        			}
	        		}
	        		
	        		String stringSentence = "";
	        		for(Token token : currentSentence) {
	        			stringSentence = stringSentence + token.getCoveredText() + " ";
	        		}
	        		
	        		stringSentence = "'" + stringSentence.replace("\"", "").replace("'", "") + "'";        		
	        		
					String currentValence=valence.getValenceRating();
					String identifier;
					
					if(valence.getValenceRating()==null) {
						currentValence="positive";
					}
					if(t1.getAspect()!=null && t2.getAspect()!=null) {
						if(t1.getAspect().toLowerCase().compareTo("ratingofaspect")==0) {
	    					//non-negated
							identifier = t2.getAspect().replace("?", "ae") + "-" + currentValence;
	    				} else {
	    					//negated
	    					identifier = t1.getAspect().replace("?", "ae") + "-" + currentValence;
	    				}
						
						//find the value in returnList and replace it with the new one
						
						for(ArrayList<String> singleLine : sortedLines) {
							//find sentences that match
							if(singleLine.get(1).compareTo(stringSentence)==0) {
								//find identifier that match
								if(singleLine.get(2)!=null) {
									if(singleLine.get(2).compareTo(identifier)==0) {
										//mark this ratio as checked
										if(!learningModeActivated) {
											singleLine.set(3, "true");
										} else {
											//it will be set on "?" anyway...
										}
									}
								}
							}
						}
					}
				}
			}
		}       
		return returnList;
	}

	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		myLog.log("Finished! Total of " + data.size() + " entries added.");
		//we'll write out own method...
		//writeOutput();
		
		//cycle through all the 
		myLog.log("Output into Folder activated: " + outputIntoFolderActivated);
		if(outputIntoFolderActivated) {
			myLog.log("Found that many class attributes: " + allClassAttributes.size());
			
			for(String singleClass : allClassAttributes) {
				setOutputFile(outputPath + "/" + singleClass + ".arff");

				String completeOutput;
				completeOutput = "@relation " + relationName + "\n\n";
	    		 
	    		//write all relational attributes
	    		for(String relation : relations.get(0)) {
	    			completeOutput = completeOutput + "@attribute " + relation + "\n";
	    		}
	    		
	    		completeOutput = completeOutput + "\n@data\n";
	    		
				
				for(ArrayList<String> singleLine : sortedLines) {
					if(singleLine.get(classAttributeAt)!=null) {
						if(singleLine.get(classAttributeAt).compareTo(singleClass)==0) {
							completeOutput = completeOutput + generateArffLine(singleLine) + "\n";
						}
					}
				}
				
				fu.write(completeOutput);
				myLog.log("Wrote to '" + outputPath + "/" + singleClass + ".arff" + "'");
				
				
				fu.close();
			}
		} else {
			myLog.log("Output path not a folder, can't generate output files....");
		}
	}	
	
	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
		generateData(arg0, cutoff);
	}
}
