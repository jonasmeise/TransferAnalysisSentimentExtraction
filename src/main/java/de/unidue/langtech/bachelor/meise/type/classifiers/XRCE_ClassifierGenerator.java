package de.unidue.langtech.bachelor.meise.type.classifiers;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.unidue.langtech.bachelor.meise.classifier.ClassifierHandler;
import de.unidue.langtech.bachelor.meise.type.ArffGenerator;
import de.unidue.langtech.bachelor.meise.type.Tree;
import webanno.custom.AspectRating;
import webanno.custom.Valence;

//BASED ON THIS MODEL (BASIC FEATURE SET, SECTION 2.2) DESCRIBED IN THIS PAPER:
//http://aclweb.org/anthology/S/S16/S16-1044.pdf
//XRCE at SemEval-2016 Task 5: Feedbacked Ensemble Modelling on
//Syntactico-Semantic Knowledge for Aspect Based Sentiment Analysis
//Caroline Brun and Julien Perez and Claude Roux
//Classification type: Connected Conditional Random Fields 
//Training file for word lists is generated separately
//input-output format is normalized to match the current Task; the model itself is not changed

public class XRCE_ClassifierGenerator extends ArffGenerator {

	ClassifierHandler myHandler; //for pre-crossfold learning, pipeline simulation
	int cutoff = 200; //Maximale Saetze/Datensatz
	int dataCutoff = 0; //Maximale Datenentries pro Datensatz
	int valueId=0;
	
	ArrayList<ArrayList<String>> sortedLinesEntity = new ArrayList<ArrayList<String>>();
	ArrayList<ArrayList<String>> sortedLinesAttribute = new ArrayList<ArrayList<String>>();
	
	String regexIgnore = "[\'\"]";
	
	@Override
	public Collection<ArrayList<String>> generateFeaturesFromCas(Sentence sentence) {
		ArrayList<ArrayList<String>> returnList = new ArrayList<ArrayList<String>>();
		Collection<ArrayList<Token>> subSentences = new ArrayList<ArrayList<Token>>();
		
		if(valueId < dataCutoff || dataCutoff==0) { //check if max amount of data is enabled

        	Collection<Dependency> dependencies =  selectCovered(Dependency.class, sentence);
        	ArrayList<Tree<Token>> treeCollection = new ArrayList<Tree<Token>>();
			
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
        			String sentenceString = "";
        			for(Token token : singleSentence) {
        				sentenceString = sentenceString + token.getCoveredText() + " ";
        			}
        			
        			for(int i=0;i<singleSentence.size();i++) {
            			String unigrams = "";
            			String bigrams = "";		
            			String lemma = "";
            			String pos = "";
        				Token singleToken = singleSentence.get(i);
        				
        				lemma = lemma + singleToken.getLemma().getValue();
        				pos = pos + singleToken.getPos().getPosValue();
        				
            			unigrams = unigrams.toLowerCase();
        				bigrams = bigrams.toLowerCase();
        				lemma = lemma.toLowerCase();
            			
    					ArrayList<String> singleLine = new ArrayList<String>();
    					singleLine.add("" + valueId);
    					singleLine.add("'" + sentenceString.replaceAll(regexIgnore, "") + "'");
    					
    					singleLine.add("'" + pos.replaceAll(regexIgnore, "") + "'");
    					singleLine.add("'" + lemma.replaceAll(regexIgnore, "") + "'");
    					singleLine.add("'" + singleToken.getCoveredText().replaceAll(regexIgnore, "") + "'");
    					
    					int hasUppercase = (singleToken.getCoveredText().toUpperCase().equals(singleToken.getCoveredText().toLowerCase())) ? 0 : 1;
    					singleLine.add("" + hasUppercase);
    					
    					Tree<Token> currentToken = null;
    					
    					for(int treeRotate=0;treeRotate<treeCollection.size();treeRotate++) {
    						if(treeCollection.get(treeRotate).findToken(singleToken, treeCollection)!=null) {
    							currentToken = treeCollection.get(treeRotate).findToken(singleToken, treeCollection);
    						}
    					}
    					
    					if(currentToken!=null) {
    						singleLine.add("'" + currentToken.getParentDependencyType() + "'");
    					} else {
    						singleLine.add("");
    					}
    					
    					Collection<Token> searchTokens = new ArrayList<Token>();
    					searchTokens.add(singleToken);
    					Collection<Token> contextTokens = getContext(sentence, treeCollection, 3, 10000, searchTokens);
    					
    					String context = "";
    					for(Token token : contextTokens) {
    						context = context + token.getCoveredText() + " ";
    					}
    					singleLine.add("'" + context.replaceAll(regexIgnore, "") + "'");
    					
    					singleLine.add(singleClass);
    					
    					if(!learningModeActivated) {
    						singleLine.add("false");
    					} else {
    						singleLine.add("?");
    					}
    					
    					valueId++;
    					
    					returnList.add(singleLine);
    					sortedLinesEntity.add(singleLine);	

        			}
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
	        		for(Token singleToken : currentSentence) {
    					stringSentence = stringSentence + singleToken.getCoveredText() + " "; //identical to unigram feature
	        		}
	        		
	        		stringSentence = "'" + stringSentence.toLowerCase().replaceAll(regexIgnore, "") + "'";        		
	        		
					String currentValence=valence.getValenceRating();
					String identifier;
					String focusWord;
					
					if(valence.getValenceRating()==null) {
						currentValence="positive";
					}
					if(t1.getAspect()!=null && t2.getAspect()!=null) {
						if(t1.getAspect().toLowerCase().compareTo("ratingofaspect")==0) {
	    					//non-negated
							identifier = t2.getAspect().replaceAll("[^\\x00-\\x7F]", "") + "-" + currentValence;
							focusWord = "'" + t2.getCoveredText().replaceAll(regexIgnore, "")  + "'";
	    				} else {
	    					//negated
	    					identifier = t1.getAspect().replaceAll("[^\\x00-\\x7F]", "") + "-" + currentValence;
	    					focusWord = "'" + t1.getCoveredText().replaceAll(regexIgnore, "")  + "'";
	    				}
						
						//find the value in returnList and replace it with the new one
						
						for(ArrayList<String> singleLine : sortedLinesEntity) {
							//find sentences that match
							if(singleLine.get(1).compareTo(stringSentence)==0) {
								//find identifier that match
								if(singleLine.get(identifierAttributeAt)!=null) {
									if(singleLine.get(identifierAttributeAt).compareTo(identifier)==0 && singleLine.get(4).compareTo(focusWord)==0) {
										//mark this ratio as checked
										if(!learningModeActivated) {
											singleLine.set(singleLine.size()-1, "true");
										} else {
											//it will be set on "?" or anyway...
										}
									}
								}
							}
						}
					}
				}
			}
		}       
		
		//since we don't need duplicates for every class...
		if(learningModeActivated) {
			int onlyTakeFirst = subSentences.size();
			ArrayList<ArrayList<String>> alternativeReturnList = new ArrayList<ArrayList<String>>();
			
			for(int i=0;i<onlyTakeFirst;i++) {
				alternativeReturnList.add(returnList.get(i));
			}
			
			return alternativeReturnList;
		}
		
		return returnList;
	}

	@Override
	public ArrayList<ArrayList<String>> generateRelations() {
		
		String[] types = new String[16];
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
		
		for(int i=0;i<types.length;i++) {
			ArrayList<String> relations = new ArrayList<String>();
			
			relations.add("id numeric");
			//for nouns, verbs, adjectives
			relations.add("sentence string");
			relations.add("pos string");
			relations.add("lemma string");
			relations.add("surfaceform string");
			relations.add("hasUppercase numeric");
			relations.add("dependency string");
			relations.add("neighbortokens string");
			//relations.add("relatedto " + generateTupel(new String[] {"true", "false"})); //TODO: Where does the data come from?
			
			relations.add("type string");
			
			relations.add("aspecttype " + generateTupel(new String[] {"true", "false"}));	
			
			allClassAttributes.add(types[i]);
			
			ArrayList<String> newList = new ArrayList<String>();
			newList.add(types[i]);
			
			this.relations.add(relations);
		}
		
		ignoreFeatures = new int[2];
		identifierAttributeAt=relations.get(0).indexOf("type string");
		ignoreFeatures[0]=identifierAttributeAt;
		ignoreFeatures[1]=1;
		
		return this.relations;
	}
	
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		myLog.log("Finished! Total of " + data.size() + " entries added.");
		//we'll write out own method...
		//writeOutput();
		
		//cycle through all the 
		myLog.log("Output into Folder activated: " + outputIntoFolderActivated);
		if(outputIntoFolderActivated) {
			myLog.log("Found that many different identifier models: " + allClassAttributes.size());
			
			for(String singleClass : allClassAttributes) {
				setOutputFile(outputPath + "/" + singleClass + ".arff");

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
	    		
				
				for(ArrayList<String> singleLine : sortedLinesEntity) {
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
	
	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
		generateData(arg0, cutoff);
	}

}
