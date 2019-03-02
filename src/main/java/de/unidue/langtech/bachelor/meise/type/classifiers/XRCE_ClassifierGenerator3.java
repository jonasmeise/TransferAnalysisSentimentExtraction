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
//CLASS IS NOT IMPLEMENTED:
//-feature dataset link that is given in the task does not work

public class XRCE_ClassifierGenerator3 extends ArffGenerator {
	int cutoff = 200; //Maximale Saetze/Datensatz
	int dataCutoff = 0; //Maximale Datenentries pro Datensatz
	int valueId=0;
	
	ArrayList<String> tagWordsForAspects;
	ArrayList<String> sortedLines;
	
	String regexIgnore = "[\': ;\"]";
	
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
    		for(ArrayList<Token> singleSentence : subSentences) {	
    			String sentenceString = "";
    			
    			for(Token token : singleSentence) {
    				sentenceString = sentenceString + token.getCoveredText() + " ";
    				
        			ArrayList<String> singleLine = new ArrayList<String>();
        			singleLine.add("-");
        			
        			ArrayList<Token> searchToken = new ArrayList<Token>();
        			searchToken.add(token);
        			ArrayList<Token> contextTokens = getContext(sentence, treeCollection, 20, 1000, searchToken);
        			
        			myLog.log("Found " + contextTokens.size() + " context tokens for token '" + token.getCoveredText() + "' in '" + singleSentence.size() + "'.");
        			
        			int counter=-(contextTokens.indexOf(token));
        			
        			for(Token contextToken : contextTokens) {
        				singleLine.add("pos[" + counter + "]=" + contextToken.getPos().getPosValue().replaceAll(regexIgnore, ""));
        				singleLine.add("lemma[" + counter + "]=" + contextToken.getLemma().getValue().replaceAll(regexIgnore, ""));
        				singleLine.add("surface[" + counter + "]=" + contextToken.getCoveredText().replaceAll(regexIgnore, ""));
        				singleLine.add("uppercase[" + counter + "]=" + (contextToken.getCoveredText().toLowerCase()!=contextToken.getCoveredText().toUpperCase()));

        				int hit=0;
        				
        				for(String tag : tagWordsForAspects) {
        					for(String singleTag : tag.split(" ")) {
        						if(contextToken.getCoveredText().toLowerCase().contains(singleTag)) {
        							hit = tagWordsForAspects.indexOf(tag)+1;
        						}
        					}
        				}
        				
        				singleLine.add("includes[" + counter + "]=" + hit);
        				
        				counter++;
        			}
        			
        			String completeOutput = "";
        			
        			for(String singleTag : singleLine) {
    					if(singleTag.equals("-")) {
    						completeOutput = completeOutput + singleTag + "\t";
    					} else {
    						completeOutput = completeOutput + singleTag + " ";
    					}
    				}
 
    				sortedLines.add(completeOutput);
        			//sentiment-analysis structure...?
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
	        		
					String currentValence=valence.getValenceRating();
					String identifier;
					String focusWord;
					
					if(valence.getValenceRating()==null) {
						currentValence="positive";
					}
					if(t1.getAspect()!=null && t2.getAspect()!=null) {
						if(t1.getAspect().toLowerCase().compareTo("ratingofaspect")==0) {
	    					//non-negated
							focusWord = t2.getCoveredText().replaceAll(regexIgnore, "");
							identifier = t2.getAspect();
	    				} else {
	    					//negated
	    					focusWord = t1.getCoveredText().replaceAll(regexIgnore, "");
	    					identifier = t1.getAspect();
	    				}
						
						//find the value in returnList and replace it with the new one
						
						//backwards because it's more likely
						for(int i=sortedLines.size()-1;i>0;i--) {
							//find sentences that match
							String[] singleLine = sortedLines.get(i).split(" ");
							boolean found=false;
							
							for(String singleTag : singleLine) {
								if(singleTag.startsWith("surface[0]")) {
									//myLog.log(singleTag);
									//myLog.log(sortedLines.get(i));
									if(singleTag.split("=").length>1 && singleTag.split("=")[1].equals(focusWord) && singleLine[0].startsWith("-")) {
										sortedLines.set(i, currentValence +"\taspect[0]=" + identifier + ":3 " + sortedLines.get(i).split("\\t")[1]);
										found=true;
										break;
									}
								}
							}
							
							if(found) {
								break;
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
		this.relations = new ArrayList<ArrayList<String>>();
		
		sortedLines = new ArrayList<String>();
		
		tagWordsForAspects = new ArrayList<String>();
		tagWordsForAspects.add("positive bar bath pool facility hotel onsen restaurant place spa");
		tagWordsForAspects.add("service staff staffs concierge he she they lady woman ma receptionist");
		tagWordsForAspects.add("area station subway restaurant hotel view metro airport location distance access");
		tagWordsForAspects.add("time check coffee egg water tea experience breakfast stay food everything choice");
		tagWordsForAspects.add("bathroom furniture space door room pillow bed amenities shower");
		tagWordsForAspects.add("money price cost charge value");
		tagWordsForAspects.add("wifi internet");
		tagWordsForAspects.add("room towel window water smell cleaning shower hotel");
		
		return this.relations;
	}
	
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		myLog.log("Finished! Total of " + data.size() + " entries added.");
		//we'll write out own method...
		//writeOutput();
		
		//cycle through all the 
		myLog.log("Output into Folder activated: " + outputIntoFolderActivated);
		if(outputIntoFolderActivated) {
			FileUtils fu = new FileUtils();
			
			try {
				fu.createWriter(outputPath + "\\output.txt");
			} catch (IOException e) {
				e.printStackTrace();
			}

			int counter=0; 
			
			for(String singleLine : sortedLines) {
				if(counter%1000==0) {
					myLog.log("Generating " + counter + "/" + sortedLines.size());
				}
				
				counter++;
				
				if(!singleLine.startsWith("-")) {
					fu.write(singleLine.substring(0,singleLine.length()-1) + "\n");
				}
			}
			
			fu.close();
			
		} else {
			myLog.log("Output path not a folder, can't generate output files....");
		}
	}	
	
	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
		generateData(arg0, cutoff);
	}
}
