package de.unidue.langtech.bachelor.meise.type.classifiers;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.unidue.langtech.bachelor.meise.files.FileUtils;
import de.unidue.langtech.bachelor.meise.files.RawJsonReviewReader;
import de.unidue.langtech.bachelor.meise.sentimentlexicon.AFINN;
import de.unidue.langtech.bachelor.meise.sentimentlexicon.BingLiu;
import de.unidue.langtech.bachelor.meise.sentimentlexicon.EmoLex;
import de.unidue.langtech.bachelor.meise.type.ArffGenerator;
import de.unidue.langtech.bachelor.meise.type.ReviewData;
import de.unidue.langtech.bachelor.meise.type.SentimentLexicon;
import de.unidue.langtech.bachelor.meise.type.StopwordHandler;
import de.unidue.langtech.bachelor.meise.type.Tree;
import webanno.custom.AspectRating;
import webanno.custom.Valence;

//BASED ON THIS MODEL (APPROACH, SET 2) DESCRIBED IN THIS PAPER:
//http://aclweb.org/anthology/S/S16/S16-1048.pdf
//BUTknot at SemEval-2016 Task 5: SVM and CRF for Aspect Detection and Unsupervised Aspect-Based Sentiment Analysis
//Jakub MachaÅLcek
//Model version includes all features until the "POS filter" domain as presented in table 3
//Removed POS: -
//Classification type: stochastic gradient descent (logistic regression)
//input-output format is normalized to match the current Task; the model itself is not changed



public class BUTknot_ClassifierGenerator extends ArffGenerator{

	int cutoff = 200; //Maximale Saetze/Datensatz
	int dataCutoff = 0; //Maximale Datenentries pro Datensatz
	int valueId=0;
	
	StopwordHandler myHandler;
	
	String regexIgnore = "[\'\"]";
	String regexSplit = "[\\.\\?,\\-\\!;]";
	
	Collection<String> negationWords;
	ArrayList<ArrayList<String>> sortedLines = new ArrayList<ArrayList<String>>();
	ArrayList<String> tagWordsForAspects;
	
	@Override
	public Collection<ArrayList<String>> generateFeaturesFromCas(Sentence sentence) {
		ArrayList<ArrayList<String>> returnList = new ArrayList<ArrayList<String>>();
		Collection<ArrayList<Token>> subSentences = new ArrayList<ArrayList<Token>>();
		
		if(valueId < dataCutoff || dataCutoff==0) { //check if max amount of data is enabled
			if(useOldData) {
				Collection<Dependency> dependencies =  selectCovered(Dependency.class, sentence);
				
	        	ArrayList<Token> roots = new ArrayList<Token>();
	        	for(Dependency dpElement : dependencies) {
					if(dpElement.getDependencyType().toLowerCase().compareTo("root") == 0) {
						roots.add(dpElement.getGovernor());
					}
				}
	        	
	        	int currentDataId = myReader.getReviewId(sentence.getCoveredText());
	        	if(currentDataId>=0) {
		        	ReviewData currentData = myReader.getReview(currentDataId);
		        	
		        	myLog.log(sentence.getCoveredText() + " -> (" + currentDataId + "): " + currentData.getTextContent());
		        	
		        	ArrayList<String> categories = currentData.getOpinionCategory();
	
		        	for(String singleClass : allClassAttributes) {
		        		ArrayList<String> singleLine = new ArrayList<String>();
		        		
		        		singleLine.add("" + valueId);
		        		
		        		String uni = "";
		        		String bi = "";
		        		String uni_lemma = "";
	
		        		Collection<Token> currentSentence =selectCovered(Token.class, sentence);
		        		String oldString = null;
		        		
		        		for(String singleString : currentData.getTextContent().split(" ")) {
		        			String currentLemma = "";
		        			if(singleString.length()>=3) {
		        				uni = uni + singleString + " ";
		        				
		        				for(Token singleToken : currentSentence) {
		        					if(singleToken.getCoveredText().equals(singleString)) {
		        						currentLemma = singleToken.getLemma().getValue();
		        						uni_lemma = uni_lemma + currentLemma + " ";
		        					}
		        				}
		        			}
		        			
		        			if(oldString!=null) {
		        				bi = bi + oldString + "_" + currentLemma + " "; 
	
		        				oldString = currentLemma;
		        			}
		        			
		        			if(oldString==null) {
		        				oldString = currentLemma;
		        			}
		        		}
	
		        		uni = uni.replaceAll("\\d", "#number#");
		        		uni = uni.replaceAll("(.)\\1", "\\1");
		        		
		        		
		        		singleLine.add("'" + uni.replaceAll(regexIgnore, "") + "'");
		        		singleLine.add("'" + uni_lemma.replaceAll(regexIgnore, "") +"'");
		        		singleLine.add("'" + bi.replaceAll(regexIgnore, "") +"'");
		        		
		        		singleLine.add(singleClass);
		        		
		        		boolean containsTarget=false;
		        		for(String target : categories) {
		        			if(target.toLowerCase().equals(singleClass)) {
		        				containsTarget=true;
		        			}
		        		}
		        		
		        		singleLine.add(containsTarget ? "true" : "false");
		        			
		        		sortedLines.add(singleLine);
		        		returnList.add(singleLine);
		        		
		        		valueId++;
		        	}
	        	}
			} else {
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
		        	
	        	
	        	subSentences = divideIntoSubSentences(sentence, treeCollection);
	        	//generate all outputs, then overwrite specific ones
	        	for(String singleClass : allClassAttributes) {
	        		for(ArrayList<Token> singleSentence : subSentences) {
	        			String bigrams = "";		
	        			String lemmas = "";
	        			String pos = "";
	        			
	        			String completeSentence="";
	        			
	        			for(int i=0;i<singleSentence.size();i++) {
	        				Token singleToken = singleSentence.get(i);
	        				completeSentence = completeSentence + singleToken.getCoveredText()+" ";
	        				String currentLemma = singleToken.getLemma().getValue().toLowerCase();
	        				
	        				if(singleToken.getCoveredText().length()>=2 && !myHandler.isStopword(currentLemma)) {	
	
		        				pos =singleToken.getPos().getPosValue().toLowerCase();     				
		        				
		        				//remove non-interesting pos
		        				if(!pos.equals("det") && !pos.equals(".") && !pos.equals("cd")) {
		        					for(String tagGroup : tagWordsForAspects) {
		        						for(String split : tagGroup.split(" ")) {
		        							if(currentLemma.equals(split)) {
		        								currentLemma = tagGroup.split("#")[1];
		        							}
		        						}
		        					}
		        				}
		        				
	    	        				lemmas = lemmas + currentLemma + " ";
		        						
		        				if((i+1)<singleSentence.size()) {
		        					bigrams = bigrams + "_" + currentLemma + "_" + singleSentence.get(i+1).getLemma().getValue().toLowerCase() + " ";
		        				}
	        				}
	        			}
	        			
	    				bigrams = bigrams.toLowerCase();
	    				lemmas = lemmas.toLowerCase();
	        			
						ArrayList<String> singleLine = new ArrayList<String>();
						singleLine.add("" + valueId);
						
						completeSentence = completeSentence.replaceAll("\\d", "#number#");
						completeSentence = completeSentence.replaceAll("(.)\\1", "\\1");
						
						singleLine.add("'" + completeSentence + "'");
						
						singleLine.add("'" + lemmas.replaceAll(regexIgnore, "") + "'");
						singleLine.add("'" + bigrams.replaceAll(regexIgnore, "") + "'");
	
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
		        		for(Token singleToken : currentSentence) {
		        			stringSentence = stringSentence + singleToken.getCoveredText() + " "; //identical to unigram feature
		        		}
		        		
		        		stringSentence = stringSentence.replaceAll("\\d", "#number#");
		        		stringSentence = stringSentence.replaceAll("(.)\\1", "\\1");
		        		
		        		stringSentence = "'" + stringSentence + "'";
		        		
						String identifier;
						
						if(t1.getAspect()!=null && t2.getAspect()!=null) {
							if(t1.getAspect().toLowerCase().compareTo("ratingofaspect")==0) {
		    					//non-negated
								identifier = t2.getAspect().replaceAll("[^\\x00-\\x7F]", "");
		    				} else {
		    					//negated
		    					identifier = t1.getAspect().replaceAll("[^\\x00-\\x7F]", "");
		    				}
							
							//find the value in returnList and replace it with the new one
							
							for(ArrayList<String> singleLine : sortedLines) {
								//find sentences that match
								if(singleLine.get(1).compareTo(stringSentence)==0) {
									//find identifier that match
									if(singleLine.get(identifierAttributeAt)!=null) {
										if(singleLine.get(identifierAttributeAt).compareTo(identifier)==0) {
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
		String[] types = new String[8];
		
		if(!useOldData) {
			types[0] = "Ausstattung";
			types[1] = "Hotelpersonal";
			types[2] = "Lage";
			types[3] = "OTHER";
			types[4] = "Komfort";
			types[5] = "Preis-Leistungs-Verhltnis";
			types[6] = "WLAN";
			types[7] = "Sauberkeit";
		} else {
			//load data separately for cross-checking
			myReader = new RawJsonReviewReader();
			myReader.folderPath = "src\\main\\resources\\SEABSA16_data";
			myReader.useOldData = true;
			
			if(!isTestData ) {
				myReader.fileExtension = ".xml";
			} else {
				myReader.fileExtension = ".gold";
			}
			
			myReader.external_Initialize(-1);
			
			types = getAspectLabelsOldData();
		}
		
		for(int i=0;i<types.length;i++) {
			ArrayList<String> relations = new ArrayList<String>();
			
			relations.add("id numeric");
			//for nouns, verbs, adjectives
			relations.add("sentence string");
			relations.add("lemmas string");
			relations.add("bigrams string");
			
			relations.add("type string");
			
			relations.add("aspecttype " + generateTupel(new String[] {"true", "false"}));
			//relations.add("aspecttype numeric");
			
			allClassAttributes.add(types[i]);
			
			ArrayList<String> newList = new ArrayList<String>();
			newList.add(types[i]);
			
			this.relations.add(relations);
			identifierAttributeAt = relations.size()-2;
		}
		
		ignoreFeatures = new int[2];
		ignoreFeatures[0] = identifierAttributeAt;
		ignoreFeatures[1] = 1;
		
		return this.relations;
	}

	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		myLog.log("Finished! Total of " + data.size() + " entries added.");
		//we'll write out own method...
		//writeOutput();
		
		writeOutput(sortedLines);
	}	
	
	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
		generateData(arg0, cutoff);
	}
}
