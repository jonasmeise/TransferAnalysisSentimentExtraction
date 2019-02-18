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
//Classification type: linear SVM
//Training file for word lists is partially included in the form of tag words
//input-output format is normalized to match the current Task; the model itself is not changed


public class GTI_ClassifierGenerator extends ArffGenerator{

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
					
					for(String tagWord : tagWordsForAspects) {
						int currentCounter=0;
						for(Token singleToken : singleSentence) {
							for(String singleTag : tagWord.split(" ")) {
								if(singleToken.getLemma().getValue().toLowerCase().equals(singleTag) && !constrained) {
									currentCounter++;
								}
							}
						}
						
						singleLine.add("" + currentCounter);
					}
					
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
		types[0] = "Ausstattung";
		types[1] = "Hotelpersonal";
		types[2] = "Lage";
		types[3] = "OTHER";
		types[4] = "Komfort";
		types[5] = "Preis-Leistungs-Verhltnis";
		types[6] = "WLAN";
		types[7] = "Sauberkeit";
		
		tagWordsForAspects = new ArrayList<String>();
		tagWordsForAspects.add("positive bar bath pool facility hotel onsen restaurant place spa");
		tagWordsForAspects.add("service staff staffs concierge he she they lady woman ma receptionist");
		tagWordsForAspects.add("area station subway restaurant hotel view metro airport location distance access");
		tagWordsForAspects.add("time check coffee egg water tea experience breakfast stay food everything choice");
		tagWordsForAspects.add("bathroom furniture space door room pillow bed amenities shower");
		tagWordsForAspects.add("money price cost charge value");
		tagWordsForAspects.add("wifi internet");
		tagWordsForAspects.add("room towel window water smell cleaning shower hotel");
		
		
		for(int i=0;i<types.length;i++) {
			ArrayList<String> relations = new ArrayList<String>();
			
			relations.add("id numeric");
			//for nouns, verbs, adjectives
			
			relations.add("words string");
			relations.add("lemmas string");
			relations.add("pos string");
			relations.add("bigrams string");
			
			int counter=1;
			for(String tagWord : tagWordsForAspects) {
				relations.add("tagwordset" + counter + " numeric");
				counter++;
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
