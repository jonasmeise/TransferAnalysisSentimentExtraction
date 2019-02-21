package de.unidue.langtech.bachelor.meise.type.classifiers;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
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

//BASED ON THIS MODEL (BASIC FEATURE SET, SECTION 2.2.1) DESCRIBED IN THIS PAPER:
//http://aclweb.org/anthology/S/S16/S16-1049.pdf
//GTI at SemEval-2016 Task 5: SVM and CRF for Aspect Detection and Unsupervised Aspect-Based Sentiment Analysis
//Tamara Alvarez-Lopez, Jonathan Juncal-Martnez, Milagros Fernandez-Gavilanes Enrique Costa-Montenegro, Francisco Javier Gonzalez-Casta no
//Classification type: linear SVM
//Word List is generated previously on training data and added as external feature
//input-output format is normalized to match the current Task; the model itself is not changed


public class GTI_ClassifierGenerator extends ArffGenerator{

	int cutoff = 200; //Maximale Saetze/Datensatz
	int dataCutoff = 0; //Maximale Datenentries pro Datensatz
	int valueId=0;
	
	String regexIgnore = "[\'\"]";
	ArrayList<String> tagWordsForAspects;
	RawJsonReviewReader trainingReader;
	
	ArrayList<ArrayList<String>> sortedLines = new ArrayList<ArrayList<String>>();
	
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
	        	ReviewData currentData = myReader.getReview(currentDataId);
	        	
	        	myLog.log(sentence.getCoveredText() + " -> (" + currentDataId + "): " + currentData.getTextContent());
	        	
	        	ArrayList<String> categories = currentData.getOpinionCategory();

	        	for(String singleClass : allClassAttributes) {
	        		ArrayList<String> singleLine = new ArrayList<String>();
	        		
	        		
	        		String uni = "";
	        		String bi = "";
	        		String pos_string = "";
	        		String uni_lemma = "";

	        		Collection<Token> currentSentence =  selectCovered(Token.class, sentence);
	        		String oldString = null;
	        		
	        		for(String singleString : currentData.getTextContent().split(" ")) {
	        			Token singleToken = null;
	        			
	        			for(Token currentToken : currentSentence) {
	        				if(singleString.equals(currentToken.getCoveredText())) {
	        					singleToken = currentToken;
	        				}
	        			}
	        			
	        			String pos = "";
	        			if(singleToken!=null) {
	        				pos = singleToken.getPos().getPosValue();
	        			}
	        			
	        			if(singleToken==null || pos.contains("JJ") || pos.contains("NN") || pos.contains("VB")) {
		        			uni = uni + singleString + " ";
		        			
		        			if(singleToken!=null) {
		        				uni_lemma = uni_lemma + singleToken.getLemma().getValue() + " ";
		        			}
		        			
		        			pos_string = pos_string + pos + " ";
		        			
		        			if(oldString!=null) {
		        				bi = bi + oldString + "_" + singleString + " "; 

		        				oldString = singleString;
		        			}
		        			
		        			if(oldString==null) {
		        				oldString = singleString;
		        			}
	        			}
	        		}

	        		singleLine.add("" + valueId);
	        		
	        		singleLine.add("'" + uni.replaceAll(regexIgnore, "") + "'");
	        		singleLine.add("'" + uni_lemma.replaceAll(regexIgnore, "") +"'");
	        		singleLine.add("'" + pos_string.replaceAll(regexIgnore, "") +"'");
	        		singleLine.add("'" + bi.replaceAll(regexIgnore, "") +"'");
	        		
					for(String singleTagWordLine : tagWordsForAspects) {
						int toAdd=0;
						
						for(String singleTagWord : singleTagWordLine.split(" ")) {
							if(sentence.getCoveredText().toLowerCase().contains(singleTagWord)) {
								toAdd++;
							}
						}
						
						singleLine.add("" + toAdd);
					}
	        		
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
	        			String unigrams = "";
	        			String bigrams = "";		
	        			String lemmas = "";
	        			String pos = "";
	        			
	        			String completeSentence="";
	        			
	        			for(int i=0;i<singleSentence.size();i++) {
	        				Token singleToken = singleSentence.get(i);
	        				completeSentence = completeSentence + singleToken.getCoveredText()+" ";
	        				
	        				if(singleToken.getPos().getPosValue().contains("NN") || singleToken.getPos().getPosValue().contains("JJ") || singleToken.getPos().getPosValue().contains("VB")) {
		        				unigrams = unigrams + singleToken.getCoveredText() + " ";
		        				lemmas = lemmas + singleToken.getLemma().getValue() + " ";
		        				pos = pos + singleToken.getPos().getPosValue() + " ";
		        				
		        				
		        				if((i+1)<singleSentence.size()) {
		        					bigrams = bigrams + "_" + singleToken.getCoveredText() + "_" + singleSentence.get(i+1).getCoveredText() + " ";
		        				}
	        				}
	        			}
	        			
	        			unigrams = unigrams.toLowerCase();
	    				bigrams = bigrams.toLowerCase();
	    				lemmas = lemmas.toLowerCase();
	        			
						ArrayList<String> singleLine = new ArrayList<String>();
						singleLine.add("" + valueId);
						
						singleLine.add("'" + unigrams.replaceAll(regexIgnore, "") + "'");
						singleLine.add("'" + lemmas.replaceAll(regexIgnore, "") + "'");
						singleLine.add("'" + pos.replaceAll(regexIgnore, "") + "'");
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
		        			if(singleToken.getPos().getPosValue().contains("NN") || singleToken.getPos().getPosValue().contains("JJ") || singleToken.getPos().getPosValue().contains("VB")) {
		        				stringSentence = stringSentence + singleToken.getCoveredText() + " "; //identical to unigram feature
		        			}
		        		}
		        		
		        		stringSentence = "'" + stringSentence.toLowerCase().replaceAll(regexIgnore, "") + "'";        		
		        		
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
			types = getAspectLabelsOldData();
			
			myReader = new RawJsonReviewReader();
			myReader.folderPath = "src\\main\\resources\\SEABSA16_data";
			myReader.useOldData = true;
			
			StopwordHandler myHandler = new StopwordHandler();
			
			 trainingReader = new RawJsonReviewReader();
			 trainingReader.folderPath = myReader.folderPath;
			 trainingReader.useOldData = true;
			 trainingReader.fileExtension = ".xml"; //training data
			 
			 trainingReader.external_Initialize(-1);
			 
			 myLog.log("Initializing Test Reader.");
			 
			 tagWordsForAspects = new ArrayList<String>();
			 
			for(String singleType : types) {
				String toAdd = "";
				HashMap<String, Integer> currentHashMap = new HashMap<String, Integer>();
				
				for(int i=0;i<trainingReader.getReviewList().size();i++) {
					boolean cont = false;
					for(String singleCategory : trainingReader.getReview(i).getOpinionCategory()) {
						if(singleCategory.toLowerCase().equals(singleType)) {
							cont = true;
						}
					}
					
					if(cont) {
						String currentString = trainingReader.getReview(i).getTextContent();
						
						for(String word : currentString.split(" ")) {
							if(word.length() >= 3 && !myHandler.isStopword(word)) {
								if(!currentHashMap.containsKey(word)) {
									currentHashMap.put(word, 1);
								} else {
									currentHashMap.put(word, currentHashMap.get(word)+1);
								}
							}
						}
					}
				}
				
				for(String key : currentHashMap.keySet()) {
					if(currentHashMap.get(key) > 4) {
						//add to tagwordList
						toAdd += key.toLowerCase() + " ";
					}
				}
				
				tagWordsForAspects.add(toAdd.substring(0, toAdd.length()-1));
				myLog.log("Found following key words for '" + singleType + "': " + toAdd);
			}
			
			if(!isTestData ) {
				myReader.fileExtension = ".xml";
			} else {
				myReader.fileExtension = ".gold";
			}
			
			myReader.external_Initialize(-1);
		}
		
		for(int i=0;i<types.length;i++) {
			ArrayList<String> relations = new ArrayList<String>();
			
			relations.add("id numeric");
			//for nouns, verbs, adjectives
			
			relations.add("words string");
			relations.add("lemmas string");
			relations.add("pos string");
			relations.add("bigrams string");
			
			for(int n=0;n<types.length;n++) {
				relations.add("feature_appearance_of_type" + (n+1) + " numeric");
			}
			
			relations.add("type string");
			
			relations.add("aspecttype " + generateTupel(new String[] {"true", "false"}));	
			
			allClassAttributes.add(types[i]);
			
			this.relations.add(relations);
			
			
			ignoreFeatures = new int[1];
			identifierAttributeAt=relations.size()-2;
			ignoreFeatures[0]=identifierAttributeAt;
		}
		
		return this.relations;
	}

	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		writeOutput(sortedLines);
	}	
	
	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
		generateData(arg0, cutoff);
	}
}
