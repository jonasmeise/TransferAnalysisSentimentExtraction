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
import de.unidue.langtech.bachelor.meise.type.Tree;
import webanno.custom.AspectRating;
import webanno.custom.Valence;

//BASED ON THIS MODEL (BASIC FEATURE SET, SECTION 3.2) DESCRIBED IN THIS PAPER:
//http://aclweb.org/anthology/S/S16/S16-1051.pdf
//AKTSKI at SemEval-2016 Task 5: Aspect Based Sentiment Analysis for Consumer Reviews
//Pateria, Choubey
//Classification type: SVM-RBF, C=100, gamma=0.001
//Keywords for each Term (TF-IDF transformations) are generated in String2word-function within AspectClassifier.java

public class AKTSKI_ClassifierGenerator extends ArffGenerator{

	int cutoff = 200; //Maximale Saetze/Datensatz
	int dataCutoff = 0; //Maximale Datenentries pro Datensatz
	int valueId=0;
	String regexIgnore = "[^a-zA-Z\\s!_]";
	
	ArrayList<ArrayList<String>> sortedLines = new ArrayList<ArrayList<String>>();
	ArrayList<SentimentLexicon> sentimentLexicons;
	Collection<String> neutralWords;
	
	//for non-pipelined access
	public AKTSKI_ClassifierGenerator() {
		super();
	}
	
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
        			Double[] sentiments = new Double[3];
        			sentiments[0] = (double) 0;        			
        			sentiments[1] = (double) 0;
        			sentiments[2] = (double) 0;
        			
        			for(int i=0;i<singleSentence.size();i++) {
        				Token singleToken = singleSentence.get(i);
        			
        				//TODO: Include negation term
        				for(int n=0;n<sentimentLexicons.size();n++) {
        					SentimentLexicon currentLexicon = sentimentLexicons.get(n);
        					
        					if(currentLexicon.onlyWorksOnLemmas) {
        						sentiments[n] += currentLexicon.fetchPolarity(singleToken.getLemma().getValue(), new String[] {"positive"}) - currentLexicon.fetchPolarity(singleToken.getLemma().getValue(), new String[] {"negative"});
        					} else {
        						sentiments[n] += currentLexicon.fetchPolarity(singleToken.getCoveredText(), new String[] {"positive"}) - currentLexicon.fetchPolarity(singleToken.getCoveredText(), new String[] {"negative"});
        					}
        				}
        				
        				unigrams = unigrams + singleToken.getCoveredText() + " ";
        				if((i+1)<singleSentence.size()) {
        					bigrams = bigrams + "_" + singleToken.getCoveredText() + "_" + singleSentence.get(i+1).getCoveredText() + " ";
        				}
        			}
        			
        			unigrams = unigrams.toLowerCase();
    				bigrams = bigrams.toLowerCase();
        			
					ArrayList<String> singleLine = new ArrayList<String>();
					singleLine.add("" + valueId);
					singleLine.add("" + (sentiments[0]/singleSentence.size()));
					singleLine.add("" + (sentiments[1]/singleSentence.size()));
					singleLine.add("" + (sentiments[2]/singleSentence.size()));
					
					singleLine.add("'" + unigrams.replaceAll(regexIgnore, "") + "'");
					singleLine.add("'" + bigrams.replaceAll(regexIgnore, "") + "'");
					
					for(String neutralWord : neutralWords) {
						int check = unigrams.contains(neutralWord) ? 1 : 0;
						singleLine.add("" + check);
					}
					
					int check = unigrams.contains("!") ? 1 : 0;
					singleLine.add("" + check);
					
					 check = unigrams.contains("?") ? 1 : 0;
					singleLine.add("" + check);
					
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
	        			stringSentence = stringSentence + token.getCoveredText() + " "; //identical to unigram feature
	        		}
	        		
	        		stringSentence = "'" + stringSentence.toLowerCase().replaceAll(regexIgnore, "") + "'";        		
	        		
					String currentValence=valence.getValenceRating();
					String identifier;
					
					if(valence.getValenceRating()==null) {
						currentValence="positive";
					}
					if(t1.getAspect()!=null && t2.getAspect()!=null) {
						if(t1.getAspect().toLowerCase().compareTo("ratingofaspect")==0) {
	    					//non-negated
							identifier = t2.getAspect().replaceAll("[^\\x00-\\x7F]", "") + "-" + currentValence;
	    				} else {
	    					//negated
	    					identifier = t1.getAspect().replaceAll("[^\\x00-\\x7F]", "") + "-" + currentValence;
	    				}
						
						//find the value in returnList and replace it with the new one
						
						for(ArrayList<String> singleLine : sortedLines) {
							//find sentences that match
							if(singleLine.get(4).compareTo(stringSentence)==0) {
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
		sentimentLexicons = new ArrayList<SentimentLexicon>();
		sentimentLexicons.add(new AFINN());
		sentimentLexicons.add(new BingLiu());
		sentimentLexicons.add(new EmoLex());
		
		sentimentLexicons.get(0).loadFromFile();
		sentimentLexicons.get(1).loadFromFile();
		sentimentLexicons.get(2).loadFromFile();
		
		neutralWords = new ArrayList<String>();
		neutralWords.add("average");
		neutralWords.add("normal");
		neutralWords.add("simple");
		neutralWords.add("okay");
		neutralWords.add("ok");
		neutralWords.add("not great");
		neutralWords.add("nothing great");
		neutralWords.add("moderate");
		neutralWords.add("typical");
		neutralWords.add("alright");
		neutralWords.add("fair");
		neutralWords.add("mediocre");
		neutralWords.add("just");
		neutralWords.add("fine");
		neutralWords.add("not too good");
		neutralWords.add("good enough");
		
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
			relations.add("AFINN numeric");
			relations.add("BingLiu numeric");
			relations.add("EmoLex numeric");
			relations.add("unigrams string");
			relations.add("bigrams string");
			
			for(String neutralWord : neutralWords) {
				relations.add("feature_" + stringToFeatureName(neutralWord) + " numeric");
			}
			
			relations.add("punctuation_exclamation numeric");
			relations.add("punctuation_question numeric");
			
			relations.add("type string");
			relations.add("aspecttype " + generateTupel(new String[] {"true", "false"}));	
			
			allClassAttributes.add(types[i]);
			
			this.relations.add(relations);
		}
		
		identifierAttributeAt=relations.get(0).size()-2; //second-to-last element contains identifier attribute
		ignoreFeatures = new int[1];
		ignoreFeatures[0]=identifierAttributeAt;
		
		return this.relations;
	}

	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		myLog.log("Finished! Total of " + data.size() + " entries added.");
		//We'll write out own method...
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
