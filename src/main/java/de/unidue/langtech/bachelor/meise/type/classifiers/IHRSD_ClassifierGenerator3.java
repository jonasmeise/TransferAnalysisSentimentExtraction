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

//BASED ON THIS MODEL (SYSTEM DESCRIPTION, 3) DESCRIBED IN THIS PAPER:
//http://aclweb.org/anthology/S/S16/S16-1047.pdf
//IHS-RD-Belarus at SemEval-2016 Task 5:
//Detecting Sentiment Polarity Using the Heatmap of Sentence
//Maryna Chernyshevich
//Classification type: back-propagation network
//SO lexica are produced with sentimentLexica instead of online reviews -> change from unconstrained to constrained
//separator for opinion extraction has pairs excluded due to architecture constraints


public class IHRSD_ClassifierGenerator3 extends ArffGenerator{
	int cutoff = 200; //Maximale Saetze/Datensatz
	int dataCutoff = 0; //Maximale Datenentries pro Datensatz
	int valueId=0;
	
	int lowest, highest=0;
	
	String regexIgnore = "[\'\"]";
	String regexSplit = "[\\.\\?,\\-\\!;]";
	
	Collection<String> negationWords;
	ArrayList<SentimentLexicon> sentimentLexicons;
	ArrayList<ArrayList<String>> sortedLines = new ArrayList<ArrayList<String>>();
	
	@Override
	public Collection<ArrayList<String>> generateFeaturesFromCas(Sentence sentence) {
		ArrayList<ArrayList<String>> returnList = new ArrayList<ArrayList<String>>();
		
		if(valueId < dataCutoff || dataCutoff==0) { //check if max amount of data is enabled	
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
				        		double[] heatMap = new double[81];
				        		String unigram = "";
				        		String bigram = "";
				        		String trigram = "";
				        		
				        		for(int n=0;n<heatMap.length;n++) {
				        			heatMap[n] = 0;
				        		}
				        		
								String identifier;
								
								String currentValence=valence.getValenceRating();
								
								if(valence.getValenceRating()==null) {
									currentValence="negative";
								} else {
									currentValence = currentValence.replaceAll("UNSURE", "negative").replace("rate-me", "negative");
								}
								
								for(int uniPos=0;uniPos<splitSubSentence.size();uniPos++) {
									double newValueUni = 0;
									double newValueBi = 0;
									double newValueTri = 0;
									
									unigram = splitSubSentence.get(uniPos).getLemma().getValue().toLowerCase();
									
									if(uniPos+1 < splitSubSentence.size()) {
										bigram = unigram + " " + splitSubSentence.get(uniPos+1).getLemma().getValue().toLowerCase();
									}
									
									if(uniPos+2 < splitSubSentence.size()) {
										trigram = bigram + " " + splitSubSentence.get(uniPos+2).getLemma().getValue().toLowerCase();
									}
									
									myLog.log(unigram);
									myLog.log(bigram);
									myLog.log(trigram);
									
									for(SentimentLexicon lexi : sentimentLexicons) {
										newValueUni += (lexi.fetchPolarity(unigram, new String[] {"positive"}) - lexi.fetchPolarity(unigram, new String[] {"negative"}));
									}
									
									for(SentimentLexicon lexi : sentimentLexicons) {
										for(String part : bigram.split(" ")) {
											newValueBi += (lexi.fetchPolarity(part, new String[] {"positive"}) - lexi.fetchPolarity(part, new String[] {"negative"}));
										}
									}
									newValueBi = newValueBi/2;
									
									for(SentimentLexicon lexi : sentimentLexicons) {
										for(String part : bigram.split(" ")) {
											newValueTri += (lexi.fetchPolarity(part, new String[] {"positive"}) - lexi.fetchPolarity(part, new String[] {"negative"}));
										}
									}
									newValueTri = newValueTri/3;
									
									boolean negated=false;
									//3**27=81
									
									for(String negationWord : negationWords) {
										if(bigram.contains(negationWord)) {
											negated=true;
											break;
										}
									}
									if(negated) {
										if(currentValence.equals("positive")) {
											newValueBi = -newValueBi;
										} else {
											newValueBi = (int) Math.round(0.5*newValueBi);
										}
									}
									
									negated=false;
									for(String negationWord : negationWords) {
										if(trigram.contains(negationWord)) {
											negated=true;
											break;
										}
									}
									if(negated) {
										if(currentValence.equals("positive")) {
											newValueTri = -newValueTri;
										} else {
											newValueTri = (int) Math.round(0.5*newValueTri);
										}
									}
									
									newValueUni = Math.round(newValueUni*19+21);
									newValueBi = Math.round(newValueBi*19+21);
									newValueTri = Math.round(newValueTri*19+21);
									
									myLog.log(newValueUni);
									myLog.log(newValueBi);
									myLog.log(newValueTri);
									
									
									heatMap[(int) newValueUni] = heatMap[(int) newValueUni] + 1;
									heatMap[(int) newValueBi] = heatMap[(int) newValueBi] + 1;
									heatMap[(int) newValueTri] = heatMap[(int) newValueTri] + 1;
								}
								
								String stringSentence = "";
								for(Token token : splitSubSentence) {
									stringSentence = stringSentence + token.getCoveredText() + " ";
								}
									
								ArrayList<String> fullList = new ArrayList<String>();

								if(t1.getAspect()!=null && t2.getAspect()!=null) {
									if(t1.getAspect().compareTo("RatingOfAspect")==0) {
				    					//non-negated
			    						identifier = t2.getAspect().replaceAll("RatingOfAspect", "Komfort").replaceAll("[^\\x00-\\x7F]", "");
				    				} else {
				    					//negated
			    						identifier = t1.getAspect().replaceAll("RatingOfAspect", "Komfort").replaceAll("[^\\x00-\\x7F]", "");
				    				}
									
									fullList.add("" + id);
									
									for(int i=0;i<heatMap.length;i++) {
										fullList.add("" + heatMap[i]);
									}
									
									//fullList.add(identifier);
									
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
	
	@Override
	public ArrayList<ArrayList<String>> generateRelations() {
		sentimentLexicons = new ArrayList<SentimentLexicon>();
		sentimentLexicons.add(new AFINN());
		sentimentLexicons.add(new BingLiu());
		sentimentLexicons.add(new EmoLex());
		
		negationWords = new ArrayList<String>();
		negationWords.add("not");
		negationWords.add("never");
		negationWords.add("none");
		negationWords.add("nobody");
		
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

			for(int n=0;n<81;n++) {
				relations.add("heat" + (n-40) + " numeric");
			}
			
			//relations.add("currentcategory " + generateTupel(types2));
			
			relations.add("aspecttype " + generateTupel(new String[] {"positive", "negative"}));	
			
			allClassAttributes.add(types[i]);
			
			ArrayList<String> newList = new ArrayList<String>();
			newList.add(types[i]);
			
			this.relations.add(relations);
			identifierAttributeAt = relations.size()-2;
		}
		
		ignoreFeatures = new int[0];
		
		return this.relations;
	}

	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
		generateData(arg0, cutoff);
	}
	
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		myLog.log("Finished! Total of " + data.size() + " entries added.");
		//We'll write out own method...
		//writeOutput();
		
		//cycle through all the 
		myLog.log("Output into Folder activated: " + outputIntoFolderActivated);
		if(outputIntoFolderActivated) {
			myLog.log("Found that many different identifier models: " + allClassAttributes.size());
			
			setOutputFile(outputPath + "/" + "output.arff");

			String completeOutput;
			completeOutput = "@relation " + relationName + "\n\n";
    		 
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
				completeOutput = completeOutput + generateArffLine(singleLine) + "\n";
			}
			
			fu.write(completeOutput.substring(0, completeOutput.length()-1));
			myLog.log("Wrote to '" + outputPath + "/" + "output.arff" + "'");
			
			
			fu.close();
		} else {
			myLog.log("Output path not a folder, can't generate output files....");
		}
	}
}
