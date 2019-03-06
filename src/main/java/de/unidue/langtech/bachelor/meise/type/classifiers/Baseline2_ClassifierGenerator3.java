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
import de.unidue.langtech.bachelor.meise.sentimentlexicon.AFINN;
import de.unidue.langtech.bachelor.meise.sentimentlexicon.BingLiu;
import de.unidue.langtech.bachelor.meise.sentimentlexicon.EmoLex;
import de.unidue.langtech.bachelor.meise.type.ArffGenerator;
import de.unidue.langtech.bachelor.meise.type.ReviewData;
import de.unidue.langtech.bachelor.meise.type.SentimentLexicon;
import de.unidue.langtech.bachelor.meise.type.Tree;
import webanno.custom.AspectRating;
import webanno.custom.Valence;

//based on SE-ABSA16's Baseline for Slot 1
//the 1000-words-to-keep threshold is manually set in AspectClassifier's createS2W-method


public class Baseline2_ClassifierGenerator3 extends ArffGenerator{

	int cutoff = 200; //Maximale Saetze/Datensatz
	int dataCutoff = 0; //Maximale Datenentries pro Datensatz
	int valueId=0;
	
	String regexIgnore = "[\'\"]";

	ArrayList<ArrayList<String>> sortedLines = new ArrayList<ArrayList<String>>();
	
	@Override
	public Collection<ArrayList<String>> generateFeaturesFromCas(Sentence sentence) {
		ArrayList<ArrayList<String>> returnList = new ArrayList<ArrayList<String>>();
		
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
	        	ArrayList<String> categories = currentData.getOpinionCategory();
	        	ArrayList<String> polarities = currentData.getOpinionPolarity();
	        	
	        	int counter=0;
	        	
	        	for(String singleTargetValue : tarValues) {
	        		ArrayList<String> singleLine = new ArrayList<String>();
	        		
	        		singleLine.add("" + valueId);
	        		
	        		String unigrams = "";
	        		String[] split = currentData.getTextContent().split("[ \\.?!]");
	        		
	        		for(String singleWord : split) {
	        			unigrams = unigrams + singleWord + " ";
	        		}
	        		singleLine.add(categories.get(counter));
	        		singleLine.add("'" + unigrams.replaceAll(regexIgnore, "") + "'");
	
	        		singleLine.add("valence");
	        		
	        		singleLine.add(polarities.get(counter));
	        		
	        		sortedLines.add(singleLine);
	        		returnList.add(singleLine);
	        		
	        		counter++;
	        		valueId++;
	        	}
			} else {
			
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
	        	
				for(Valence valence : selectCovered(Valence.class, sentence)) {
					AspectRating t1 = valence.getDependent();
	        		AspectRating t2 = valence.getGovernor();
	        		
					String currentValence, identifier, identifierString;
					
					currentValence = valence.getValenceRating();
					
					if(valence.getValenceRating()==null) {
						currentValence="positive";
					} else {
						currentValence = currentValence.replaceAll("UNSURE", "negative").replace("rate-me", "negative");
					}
					if(t1.getAspect()!=null && t2.getAspect()!=null) {
						if(t1.getAspect().compareTo("RatingOfAspect")==0) {
	    					//non-negated
							identifier = t2.getAspect().replaceAll("RatingOfAspect", "Komfort").replaceAll("[^\\x00-\\x7F]", "");
							identifierString = t2.getCoveredText();
	    				} else {
	    					//negated
							identifier = t1.getAspect().replaceAll("RatingOfAspect", "Komfort").replaceAll("[^\\x00-\\x7F]", "");
							identifierString = t1.getCoveredText();
	    				}
						
						String unigrams = "";
						ArrayList<String> fullList = new ArrayList<String>();
						fullList.add("" + id);
						
						for(Token singleToken : selectCovered(Token.class, sentence)) {
							unigrams += singleToken.getLemma().getValue() + " ";
						}
						
						fullList.add("'target_" + identifier.replaceAll(regexIgnore, "") + "'");
						fullList.add("'" + unigrams.replaceAll(regexIgnore, "") + "'");
	
						fullList.add("valence");
	
						fullList.add(currentValence);
						sortedLines.add(fullList);
						returnList.add(fullList);
					}
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
		String[] types = new String[1];
		types[0] = "valence";
		
		String[] types2 = new String[1];
		types2[0] = "valence";
		
		if(!useOldData) {
			//change nothing
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
		
		for(int i=0;i<types2.length;i++) {
			ArrayList<String> relations = new ArrayList<String>();
			
			relations.add("id numeric");
			//for nouns, verbs, adjectives
			relations.add("aspectlabel string");
			relations.add("unigrams string");

			relations.add("type string");
			
			if(!useOldData) {
				relations.add("polarity " + generateTupel(new String[] {"positive", "negative"}));
			} else {
				relations.add("polarity " + generateTupel(new String[] {"positive", "negative", "neutral"}));
			}
			
			allClassAttributes.add(types2[i]);
			
			ArrayList<String> newList = new ArrayList<String>();
			newList.add(types2[i]);
			
			this.relations.add(relations);
			identifierAttributeAt = relations.size()-2;
		}
		
		ignoreFeatures = new int[1]; 
		ignoreFeatures[0] = identifierAttributeAt;
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
