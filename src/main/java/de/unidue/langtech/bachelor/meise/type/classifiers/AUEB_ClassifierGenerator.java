package de.unidue.langtech.bachelor.meise.type.classifiers;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.unidue.langtech.bachelor.meise.files.RawJsonReviewReader;
import de.unidue.langtech.bachelor.meise.type.ArffGenerator;
import de.unidue.langtech.bachelor.meise.type.ReviewData;
import de.unidue.langtech.bachelor.meise.type.SentimentLexicon;
import de.unidue.langtech.bachelor.meise.type.Tree;
import webanno.custom.AspectRating;
import webanno.custom.Valence;

//BASED ON THIS MODEL (ASPECT CATEGORY DETECTION, 2.1) DESCRIBED IN THIS PAPER:
//http://aclweb.org/anthology/S/S16/S16-1050.pdf
//AUEB-ABSA at SemEval-2016 Task 5: Ensembles of Classifiers and
//Embeddings for Aspect Based Sentiment Analysis
//Dionysios Xenos, Panagiotis Theodorakakos, John Pavlopoulos,
//Prodromos Malakasiotis and Ion Androutsopoulos
//Classification type: SVM
//Meta-learning with grid search was not performed, but CVParameterSelection
//Stemming -> Lemmatizing
//since this task evaluates with 10-fold learning, recall/precision/f1 of tokens is not available due to data leakage
//	-> TF-IDF transformation while learning
//uncontained version is not included


public class AUEB_ClassifierGenerator extends ArffGenerator{

	int cutoff = 200; //Maximale Saetze/Datensatz
	int dataCutoff = 0; //Maximale Datenentries pro Datensatz
	int valueId=0;
	
	String regexIgnore = "[\'\"]";
	ArrayList<String> tagWordsForAspects;
	
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
	        	
	        	ArrayList<String> tarValues = currentData.getOpinionTargets();

	        	for(String singleClass : allClassAttributes) {
	        		ArrayList<String> singleLine = new ArrayList<String>();
	        		
	        		singleLine.add("" + valueId);
	        		
	        		String uni = "";
	        		String bi = "";
	        		String uni_lemma = "";
	        		String bi_lemma = "";
	        		String bi_pos = "";

	        		ArrayList<Token> currentSentence = (ArrayList<Token>) selectCovered(Token.class, sentence);
	        		
	        		for(Token singleToken : currentSentence) {
	        			uni = uni + singleToken.getCoveredText() + " ";
	        			uni_lemma = uni_lemma + singleToken.getLemma().getValue() + " ";
	        			
	        			if(currentSentence.indexOf(singleToken)+1 < currentSentence.size()) {
	        				bi = bi + singleToken.getCoveredText() + "_" + currentSentence.get(currentSentence.indexOf(singleToken)+1) + " ";
	        				bi_lemma = bi_lemma + singleToken.getLemma().getValue() + "_" + currentSentence.get(currentSentence.indexOf(singleToken)+1).getLemma().getValue() + " ";
	        				bi_pos = bi_pos + singleToken.getPos().getPosValue() + "_" + currentSentence.get(currentSentence.indexOf(singleToken)+1).getPos().getPosValue() + " ";
	        			}
	        		}

	        		singleLine.add("'" + uni.replaceAll(regexIgnore, "") + "'");
	        		singleLine.add("'" + uni_lemma.replaceAll(regexIgnore, "") +"'");
	        		singleLine.add("'" + bi.replaceAll(regexIgnore, "") +"'");
	        		singleLine.add("'" + bi_lemma.replaceAll(regexIgnore, "") +"'");
	        		singleLine.add("'" + bi_pos.replaceAll(regexIgnore, "") +"'");
	        		
	        		singleLine.add(singleClass);
	        		
	        		boolean containsTarget=false;
	        		for(String target : tarValues) {
	        			if(target.equals(singleClass)) {
	        				containsTarget=true;
	        			}
	        		}
	        		
	        		singleLine.add(containsTarget ? "positive" : "negative");
	        		
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
	        			String bigrams_lemmas = "";
	        			String lemmas = "";
	        			String pos = "";
	        			
	        			String completeSentence="";
	        			
	        			for(int i=0;i<singleSentence.size();i++) {
	        				Token singleToken = singleSentence.get(i);
	        				completeSentence = completeSentence + singleToken.getCoveredText()+" ";
	        				
	        				unigrams = unigrams + singleToken.getCoveredText() + " ";
	        				lemmas = lemmas + singleToken.getLemma().getValue() + " ";
	        				
	        				
	        				if((i+1)<singleSentence.size()) {
	        					bigrams = bigrams + "_" + singleToken.getCoveredText() + "_" + singleSentence.get(i+1).getCoveredText() + " ";
	        					bigrams_lemmas = bigrams_lemmas + "_" + singleToken.getLemma().getValue() + "_" + singleSentence.get(i+1).getLemma().getValue() + " ";
	        					pos = pos + "_" + singleToken.getPos().getPosValue() + "_" + singleSentence.get(i+1).getPos().getPosValue() + " ";
	        				}
	        			}
	        			
	        			unigrams = unigrams.toLowerCase();
	    				bigrams = bigrams.toLowerCase();
	    				lemmas = lemmas.toLowerCase();
	    				bigrams_lemmas = bigrams_lemmas.toLowerCase();
	        			
						ArrayList<String> singleLine = new ArrayList<String>();
						singleLine.add("" + valueId);
						
						singleLine.add("'" + unigrams.replaceAll(regexIgnore, "") + "'");
						singleLine.add("'" + lemmas.replaceAll(regexIgnore, "") + "'");
						singleLine.add("'" + bigrams.replaceAll(regexIgnore, "") + "'");
						singleLine.add("'" + bigrams_lemmas.replaceAll(regexIgnore, "") + "'");
						
						singleLine.add("'" + pos.replaceAll(regexIgnore, "") + "'");
	
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
			myReader.fileExtension = ".xml";
			myReader.external_Initialize(-1);
			
			types = getAspectLabelsOldData();
		}
		for(int i=0;i<types.length;i++) {
			ArrayList<String> relations = new ArrayList<String>();
			
			relations.add("id numeric");
			
			relations.add("unigram string");
			relations.add("unigram_lemma string");
			relations.add("bigram string");
			relations.add("bigram_lemma string");
			relations.add("pos_bigrams string");

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
