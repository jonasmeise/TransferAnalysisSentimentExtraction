package de.unidue.langtech.bachelor.meise.type.classifiers;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.unidue.langtech.bachelor.meise.sentimentlexicon.AFINN;
import de.unidue.langtech.bachelor.meise.sentimentlexicon.BingLiu;
import de.unidue.langtech.bachelor.meise.sentimentlexicon.EmoLex;
import de.unidue.langtech.bachelor.meise.type.ArffGenerator;
import de.unidue.langtech.bachelor.meise.type.SentimentLexicon;
import de.unidue.langtech.bachelor.meise.type.Tree;
import webanno.custom.AspectRating;
import webanno.custom.Valence;

//BASED ON THIS MODEL (BASIC FEATURE SET, SECTION 2.2.1) DESCRIBED IN THIS PAPER:
//http://aclweb.org/anthology/S/S16/S16-1049.pdf
//GTI at SemEval-2016 Task 5: SVM and CRF for Aspect Detection and Unsupervised Aspect-Based Sentiment Analysis
//Tamara Alvarez-Lopez, Jonathan Juncal-Martnez, Milagros Fernandez-Gavilanes Enrique Costa-Montenegro, Francisco Javier Gonzalez-Casta no
//Classification type: linear SVM?
//sub-sentences will be tagged
//input-output format is normalized to match the current Task; the model itself is not changed


public class GTI_ClassifierGenerator3 extends ArffGenerator{

	int cutoff = 200; //Maximale Saetze/Datensatz
	int dataCutoff = 0; //Maximale Datenentries pro Datensatz
	int valueId=0;
	
	String regexIgnore = "[\'\"]";
	String regexSplit = "[\\.\\?,\\-\\!;]";
	
	Collection<String> negationWords;
	ArrayList<SentimentLexicon> sentimentLexicons;
	ArrayList<ArrayList<String>> sortedLines = new ArrayList<ArrayList<String>>();
	
	@Override
	public Collection<ArrayList<String>> generateFeaturesFromCas(Sentence sentence) {
		ArrayList<ArrayList<String>> returnList = new ArrayList<ArrayList<String>>();
		
		if(valueId < dataCutoff || dataCutoff==0) { //check if max amount of data is enabled
			for(String singleClass : allClassAttributes) {	
				Collection<ArrayList<Token>> subSentences = new ArrayList<ArrayList<Token>>();
				
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
		        	subSentences.add(tree.getAllObjects());	
	        	}
	
	        	for(ArrayList<Token> subSentence : subSentences) {
	        		ArrayList<ArrayList<Token>> newSplits = splitListByRegex(subSentence, regexSplit);
	
	        		for(ArrayList<Token> splitSubSentence : newSplits) {
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
								int found = 0;
								for(Token singleToken : splitSubSentence) {
									if(singleToken.equals(selectCovered(Token.class, t1).get(0)) || singleToken.equals(selectCovered(Token.class, t2).get(0))) {
										found++;
									}
								}
								
								if(found>=2) {
					        		String stringSentence = "";
				
					        		double[] lexica = new double[3];
					        		lexica[0] = 0;
					        		lexica[1] = 0;
					        		lexica[2] = 0;
					        		for(int i=0;i<sentimentLexicons.size();i++) {
					        			for(Token singleToken : splitSubSentence) {
					        				if(sentimentLexicons.get(i).onlyWorksOnLemmas) {
					        					lexica[i] = lexica[i] + sentimentLexicons.get(i).fetchPolarity(singleToken.getLemma().getValue(), new String[] {"positive"}) - sentimentLexicons.get(i).fetchPolarity(singleToken.getLemma().getValue(), new String[] {"negative"}) ;
					        				} else {
					        					lexica[i] = lexica[i] + sentimentLexicons.get(i).fetchPolarity(singleToken.getCoveredText(), new String[] {"positive"}) - sentimentLexicons.get(i).fetchPolarity(singleToken.getCoveredText(), new String[] {"negative"}) ;
					        				}
					        			}
					        		}
									String currentValence=valence.getValenceRating();
									String identifier;
	
									for(Token token : splitSubSentence) {
										stringSentence = stringSentence + token.getCoveredText() + " ";
									}
									
									stringSentence = "'" + stringSentence.toLowerCase().replaceAll(regexIgnore, "")+  "'";
									
									ArrayList<String> fullList = new ArrayList<String>();
									
									if(valence.getValenceRating()==null) {
										currentValence="true";
									} else {
										currentValence = currentValence.replaceAll("UNSURE", "UNSURE").replace("rate-me", "UNSURE");
										
										if(currentValence.equals(singleClass)) {
											currentValence = "true";
										} else {
											currentValence = "false";
										}
									}
									if(t1.getAspect()!=null && t2.getAspect()!=null) {
										if(t1.getAspect().compareTo("RatingOfAspect")==0) {
					    					//non-negated
				    						identifier = t2.getAspect().replaceAll("RatingOfAspect", "Komfort").replaceAll("[^\\x00-\\x7F]", "");
					    				} else {
					    					//negated
				    						identifier = t1.getAspect().replaceAll("RatingOfAspect", "Komfort").replaceAll("[^\\x00-\\x7F]", "");
					    				}
										
										fullList.add("" + id);
										
										if(!constrained) {
											fullList.add("" + (lexica[0]/(double)splitSubSentence.size()));
											fullList.add("" + (lexica[1]/(double)splitSubSentence.size()));
											fullList.add("" + (lexica[2]/(double)splitSubSentence.size()));
										} else {
											fullList.add("" + 0);
											fullList.add("" + 0);
											fullList.add("" + 0);
										}
										
										fullList.add(identifier);
										fullList.add(stringSentence);
										
										for(String negationWord : negationWords) {
											if(stringSentence.toLowerCase().contains(negationWord)) {
												fullList.add("" + 1);
											} else {
												fullList.add("" + 0);
											}
										}
										
										fullList.add(singleClass);
										
										fullList.add(currentValence);
										sortedLines.add(fullList);
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
			}
		}
		
		return returnList;
	}
	
	public ArrayList<ArrayList<Token>> splitListByRegex(ArrayList<Token> list, String regex) {
		ArrayList<ArrayList<Token>> returnList = new ArrayList<ArrayList<Token>>();
		int counter=0;
		
		while(counter<list.size()) {
			ArrayList<Token> toCut = new ArrayList<Token>();
			
			for(int i=counter;i<list.size();i++) {
				Token token = list.get(i);
				toCut.add(token);
				
				if(token.getCoveredText().matches(regex) || (i==list.size()-1)) {
					returnList.add(toCut);
					counter=counter+toCut.size();
					break;
				}
			}
		}

		
		return returnList;
	}

	public ArrayList<Token> returnTokensBetweenTwoTokens(Token t1, Token t2, ArrayList<Token> sentence) {
		ArrayList<Token> returnList = new ArrayList<Token>();
		
		int start = sentence.indexOf(t1);
		int end = sentence.indexOf(t2);
		
		if(start < end) {
			for(int i=start;i<=end;i++) {
				returnList.add(sentence.get(i));
			}
		} else {
			for(int i=end;i<=start;i++) {
				returnList.add(sentence.get(i));
			}
		}
		
		return returnList;
	}
	
	@Override
	public ArrayList<ArrayList<String>> generateRelations() {
		sentimentLexicons = new ArrayList<SentimentLexicon>();
		sentimentLexicons.add(new AFINN());
		sentimentLexicons.add(new BingLiu());
		sentimentLexicons.add(new EmoLex());
		
		negationWords = new ArrayList<String>();
		negationWords.add("not");
		negationWords.add("n't");
		negationWords.add("non");
		negationWords.add("don't");
		negationWords.add("didn't");
		negationWords.add("but");
		
		String[] types = new String[2];
		types[0] = "positive";
		types[1] = "negative";
		
		String[] types2 = new String[8];
		types2[0] = "Ausstattung";
		types2[1] = "Hotelpersonal";
		types2[2] = "Lage";
		types2[3] = "OTHER";
		types2[4] = "Komfort";
		types2[5] = "Preis-Leistungs-Verhltnis";
		types2[6] = "WLAN";
		types2[7] = "Sauberkeit";
		
		for(int i=0;i<types.length;i++) {
			ArrayList<String> relations = new ArrayList<String>();
			
			relations.add("id numeric");
			//for nouns, verbs, adjectives
			relations.add("AFINN numeric");
			relations.add("BingLiu numeric");
			relations.add("EmoLex numeric");
			
			relations.add("currentcategory " + generateTupel(types2));
			
			relations.add("currentsentence string");
			
			int counter=1;
			for(String negationWord : negationWords) {
				relations.add("negation_type_" + counter + " numeric");
				counter++;
			}
			
			relations.add("type string");
			
			relations.add("aspecttype " + generateTupel(new String[] {"true", "false"}));	
			
			allClassAttributes.add(types[i]);
			
			ArrayList<String> newList = new ArrayList<String>();
			newList.add(types[i]);
			
			this.relations.add(relations);
			identifierAttributeAt = relations.size()-2;
		}
		
		ignoreFeatures = new int[1];
		ignoreFeatures[0] = identifierAttributeAt;
		
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
	
	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
		generateData(arg0, cutoff);
	}
}
