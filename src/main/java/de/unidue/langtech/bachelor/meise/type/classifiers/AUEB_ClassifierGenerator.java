package de.unidue.langtech.bachelor.meise.type.classifiers;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.unidue.langtech.bachelor.meise.type.ArffGenerator;
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
//Stems -> Lemma
//since this task evaluates with 10-fold learning, recall/precision/f1 of test data is not available due to data leakage
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
