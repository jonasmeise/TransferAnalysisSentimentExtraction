package de.unidue.langtech.bachelor.meise.type.classifiers;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.unidue.langtech.bachelor.meise.sentimentlexicon.*;
import de.unidue.langtech.bachelor.meise.type.ArffGenerator;
import de.unidue.langtech.bachelor.meise.type.SentimentLexicon;
import de.unidue.langtech.bachelor.meise.type.StopwordHandler;
import de.unidue.langtech.bachelor.meise.type.Tree;
import webanno.custom.AspectRating;
import webanno.custom.Valence;

public class OwnClassifier_ClassifierGenerator extends ArffGenerator{
	
	int cutoff = 200; //Maximale Saetze/Datensatz
	int dataCutoff = 0; //Maximale Datenentries pro Datensatz
	int valueId=0;
	int countNN, countJJ, countVB, countTotal;
	int distanceRatingAspect;
	
	String regexIgnore = "[\'\"]";
	ArrayList<String> negationWords;
	ArrayList<SentimentLexicon> sentimentLexicons;
	StopwordHandler myStopwordHandler;
	
	ArrayList<ArrayList<String>> sortedLines = new ArrayList<ArrayList<String>>();
	
	@Override
	public Collection<ArrayList<String>> generateFeaturesFromCas(Sentence sentence) {
		ArrayList<ArrayList<String>> returnList = new ArrayList<ArrayList<String>>();
		Collection<ArrayList<Token>> subSentences = new ArrayList<ArrayList<Token>>();
		
		if(valueId < dataCutoff || dataCutoff==0) { //check if max amount of data is enabled

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
        			String lemmas = "";
        			String dependencyString = "";
        			double lexicons_min=0;
        			double lexicons_max=0;
        			int sentenceLength;
        			int contains_negation = 0;
        			
        			
        			String completeSentence="";
        			
        			for(int i=0;i<singleSentence.size();i++) {
        				Token singleToken = singleSentence.get(i);
        				completeSentence = completeSentence + singleToken.getCoveredText()+" ";
        				String current_pos = singleToken.getPos().getPosValue();
        				
        				if(current_pos.contains("NN") || current_pos.contains("JJ") || current_pos.contains("VB") || current_pos.contains("RB")) {
        					if(constrained || !myStopwordHandler.isStopword(singleToken.getLemma().getValue().toLowerCase())) {
        						lemmas = lemmas + singleToken.getLemma().getValue() + " ";
        						
	            				for(SentimentLexicon singleLexicon : sentimentLexicons) {
	            					double newPosValue = singleLexicon.fetchPolarity(singleToken.getLemma().getValue(), new String[] {"positive"});
	            					double newNegValue = singleLexicon.fetchPolarity(singleToken.getLemma().getValue(), new String[] {"negative"});
	            					
	            					if(newPosValue > lexicons_max) {
	            						lexicons_max = newPosValue;
	            					}
	            					if(newNegValue < lexicons_min) {
	            						lexicons_min = newNegValue;
	            					}
	            				}
        					}
        				}
        			}
        			
    				for(Dependency singleDependency : dependencies) {
    					if(singleSentence.contains(singleDependency.getGovernor())) {
							dependencyString = dependencyString + singleDependency.getDependencyType() + "_" + 
									singleDependency.getGovernor().getLemma().getValue() + "_" +
									singleDependency.getDependent().getLemma().getValue() + " ";
    					}
    				}
        			
        			if(valueId%100==0) {
        				myLog.log(valueId);
        			}
        			
    				lemmas = lemmas.toLowerCase();
    				dependencyString = dependencyString.toLowerCase();
        			sentenceLength = completeSentence.length();
    				
    				boolean containsNegation = false;
    				for(String negationWord : negationWords) {
    					if(completeSentence.toLowerCase().contains(negationWord)) {
    						containsNegation = true;
    					}
    				}
    				
    				contains_negation = containsNegation ? 1 : 0;
    				
					ArrayList<String> singleLine = new ArrayList<String>();
					singleLine.add("" + valueId);
					
					singleLine.add("'" + lemmas.replaceAll(regexIgnore, "") + "'");
					singleLine.add("'" + dependencyString.replaceAll(regexIgnore, "") + "'");
					if(!constrained) {
						singleLine.add("" + lexicons_min);
						singleLine.add("" + lexicons_max);
						singleLine.add("" + Math.abs(lexicons_max - lexicons_min));
					} else {
						singleLine.add("0");
						singleLine.add("0");
						singleLine.add("0");
					}
					singleLine.add("" + contains_negation);
					singleLine.add("" + sentenceLength);
					
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
	        			String current_pos = singleToken.getPos().getPosValue();
        				
        				if(current_pos.contains("NN") || current_pos.contains("JJ") || current_pos.contains("VB") || current_pos.contains("RB")) {
        					if(constrained || !myStopwordHandler.isStopword(singleToken.getLemma().getValue().toLowerCase())) {
        						stringSentence = stringSentence + singleToken.getLemma().getValue() + " ";
        					}
        				}
	        		}
	        		
	        		stringSentence = "'" + stringSentence.toLowerCase().replaceAll(regexIgnore, "") + "'";        		
	        		
					String identifier;
					Token identifierToken;
					
					if(t1.getAspect()!=null && t2.getAspect()!=null) {
						if(t1.getAspect().toLowerCase().compareTo("ratingofaspect")==0) {
	    					//non-negated
							identifier = t2.getAspect().replaceAll("[^\\x00-\\x7F]", "");
							identifierToken = selectCovered(Token.class, t2).get(0);
	    				} else {
	    					//negated
	    					identifier = t1.getAspect().replaceAll("[^\\x00-\\x7F]", "");
	    					identifierToken = selectCovered(Token.class, t2).get(0);
	    				}
						
						if(identifierToken.getPos().getPosValue().contains("JJ")) {
							countJJ++;
						} else if(identifierToken.getPos().getPosValue().contains("VB")) {
							countVB++;
						} else if(identifierToken.getPos().getPosValue().contains("NN")) {
							countNN++;
						}
						
						countTotal++;
						int higherValue = (t1.getBegin() > t2.getBegin()) ? t1.getBegin() : t2.getBegin(); 
						int lowerValue = (t1.getBegin() > t2.getBegin()) ? t2.getBegin() : t1.getBegin();
						higherValue -= sentence.getBegin();
						lowerValue -= sentence.getBegin();
						distanceRatingAspect += sentence.getCoveredText().substring(lowerValue, higherValue).split(" ").length;
						
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
		myStopwordHandler = new StopwordHandler("src\\main\\resources\\stopwords.txt");	
		
		String[] types = new String[8];
		types[0] = "Ausstattung";
		types[1] = "Hotelpersonal";
		types[2] = "Lage";
		types[3] = "OTHER";
		types[4] = "Komfort";
		types[5] = "Preis-Leistungs-Verhltnis";
		types[6] = "WLAN";
		types[7] = "Sauberkeit";
		
		negationWords = new ArrayList<String>();
		negationWords.add("no");
		negationWords.add("not");
		negationWords.add("nt");
		negationWords.add("n't");
		negationWords.add("need");
		negationWords.add("must");
		
		sentimentLexicons = new ArrayList<SentimentLexicon>();
		sentimentLexicons.add(new AFINN());
		sentimentLexicons.add(new BingLiu());
		sentimentLexicons.add(new EmoLex());
		
		for(int i=0;i<types.length;i++) {
			ArrayList<String> relations = new ArrayList<String>();
			
			relations.add("id numeric");
			
			//of NN+, JJ+, VB+, RB
			relations.add("lemma string");
			relations.add("dependencies string");
			relations.add("lexicons_min numeric");
			relations.add("lexicons_max numeric");
			relations.add("lexicons_extremes_difference numeric");
			relations.add("contains_negation numeric");
			relations.add("sentence_length numeric");
			
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
		myLog.log("Sentiments with...\n...NN\t" + countNN + "\n...VB\t" + countVB + "\n...JJ\t" + countJJ + "\n\ntotal:\t" + countTotal);
		myLog.log("Avg. distance between rating and aspect:\n" + ((double)distanceRatingAspect / (double)countTotal));
		//writeOutput(sortedLines);
	}	
	
	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
		generateData(arg0, cutoff);
	}
}
