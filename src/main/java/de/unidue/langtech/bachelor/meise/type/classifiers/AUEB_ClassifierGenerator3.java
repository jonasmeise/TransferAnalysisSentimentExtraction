package de.unidue.langtech.bachelor.meise.type.classifiers;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
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

//BASED ON THIS MODEL (LR I PD classifier SECTION 2.3) DESCRIBED IN THIS PAPER:
//http://aclweb.org/anthology/S/S16/S16-1050.pdf
//AUEB-ABSA at SemEval-2016 Task 5: Ensembles of Classifiers and
//Embeddings for Aspect Based Sentiment Analysis
//Dionysios Xenos, Panagiotis Theodorakakos, John Pavlopoulos,
//Prodromos Malakasiotis and Ion Androutsopoulos
//Classification type: Logistic regression
//features were compressed into more basic feature representations since not all features were listed
//input-output format is normalized to match the current Task; the model itself is not changed
//relative-score data statistics of training set are not included, same reasoning as AUEB_ClassifierGenerator
//negation lexicon could not be found - using our short compiled list instead

public class AUEB_ClassifierGenerator3 extends ArffGenerator{

	int cutoff = 200; //Maximale Saetze/Datensatz
	int dataCutoff = 0; //Maximale Datenentries pro Datensatz
	int valueId=0;
	
	String regexIgnore = "[\'\"]";
	String regexSplit = "[\\.\\?,\\-\\!;]";
	
	Collection<String> negationWords;
	ArrayList<SentimentLexicon> sentimentLexicons;
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
	        		ArrayList<String> dependencyWords = new ArrayList<String>();
	        		
	        		singleLine.add("" + valueId);
	        		
	        		int unigramCount=0;
	        		double[] lexi_avg = new double[3];
	        		double[] lexi_high = new double[3];
	        		double[] lexi_low = new double[3];
	        		boolean containsCaps = false;
	        		String unigrams = "";
	        		String pos = "";
	        		String[] split = currentData.getTextContent().split("[ \\.?!]");
	        		
	        		for(String singleWord : split) {
	        			unigramCount++;
	        			
	        			if(singleWord.toUpperCase().equals(singleWord)) {
	        				containsCaps = true;
	        			}
	        			
	        			for(int i=0;i<sentimentLexicons.size();i++) {
	        				double posValue = sentimentLexicons.get(i).fetchPolarity(singleWord, new String[] {"positive"});
	        				double negValue = sentimentLexicons.get(i).fetchPolarity(singleWord, new String[] {"negative"});
	        				
	        				lexi_avg[i] = lexi_avg[i] + (posValue - negValue);
	        				
	        				if(lexi_high[i] < posValue) {
	        					lexi_high[i] = posValue;
	        				}
	        				if(lexi_low[i] > negValue) {
	        					lexi_low[i] = negValue;
	        				}
	        			}
	        			
	        			unigrams = unigrams + singleWord + " ";
	        		}
	        		
	        		for(Token singleToken : selectCovered(Token.class, sentence)) {
	        			pos = pos + singleToken.getPos().getPosValue() + " ";
	        		}
	        		
	        		for(int i=0;i<sentimentLexicons.size();i++) {
	        			if(split.length>0) {
	        				singleLine.add("" + (lexi_avg[i]/split.length));
	        			} else {
	        				singleLine.add("0");
	        			}
	        			singleLine.add("" + lexi_high[i]);
	        			singleLine.add("" + lexi_low[i]);
	        		}
	        		
	        		singleLine.add("'target_" + singleTargetValue.replaceAll(regexIgnore, "") + "'");
	        		singleLine.add("'" + unigrams.replaceAll(regexIgnore, "") + "'");
	        		singleLine.add("'" + pos.replaceAll(regexIgnore, "") +"'");
	        		singleLine.add("" + unigramCount);
	        		singleLine.add("" + ((sentence.getCoveredText().endsWith("!") || sentence.getCoveredText().endsWith("?")) ? 1 : 0));
	        		
	        		singleLine.add("" + categories.size());
	        		singleLine.add("" + (containsCaps ? 1 : 0));
	        		
	        		for(String negationWord : negationWords) {
	        			singleLine.add("" + (sentence.getCoveredText().toLowerCase().contains(negationWord) ? 1 : 0));
	    			}
	        		
	        		singleLine.add(categories.get(counter).toLowerCase());
	        		singleLine.add(polarities.get(counter));
	        		
	        		sortedLines.add(singleLine);
	        		returnList.add(singleLine);
	        		
	        		counter++;
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
	        	}
	
				for(Valence singleValence : selectCovered(Valence.class, sentence)) {
					ArrayList<String> singleLine = new ArrayList<String>();
	        		Token t1 = null;
	        		AspectRating ar = null;
	        		
	        		if(singleValence.getDependent()!=null && singleValence.getDependent().getAspect()!=null && singleValence.getGovernor()!=null && singleValence.getGovernor().getAspect()!=null && singleValence.getValenceRating()!="null" && singleValence.getValenceRating()!=null) {
		        		if(singleValence.getDependent().getAspect().toLowerCase().equals("ratingofaspect")) {
	        				t1 = JCasUtil.selectCovered(Token.class, singleValence.getGovernor()).get(0);
	        				ar = singleValence.getGovernor();
		        		} else {
		        			t1 = JCasUtil.selectCovered(Token.class, singleValence.getDependent()).get(0);
		        			ar = singleValence.getDependent();
		        		}
	        		
		        		singleLine.add("" + valueId);
		        		
		        		int unigramCount=0;
		        		double[] lexi_avg = new double[3];
		        		double[] lexi_high = new double[3];
		        		double[] lexi_low = new double[3];
		        		boolean containsCaps = false;
		        		String pos = "";
		        		Collection<Token> split = selectCovered(Token.class, sentence);
		        		
		        		for(Token singleToken : split) {
		        			unigramCount++;
		        			String singleWord = singleToken.getCoveredText();
		        			
		        			if(singleWord.toUpperCase().equals(singleWord)) {
		        				containsCaps = true;
		        			}
		        			
		        			for(int i=0;i<sentimentLexicons.size();i++) {
		        				double posValue = sentimentLexicons.get(i).fetchPolarity(singleWord, new String[] {"positive"});
		        				double negValue = sentimentLexicons.get(i).fetchPolarity(singleWord, new String[] {"negative"});
		        				
		        				lexi_avg[i] = lexi_avg[i] + (posValue - negValue);
		        				
		        				if(lexi_high[i] < posValue) {
		        					lexi_high[i] = posValue;
		        				}
		        				if(lexi_low[i] > negValue) {
		        					lexi_low[i] = negValue;
		        				}
		        			}
		        		}
		        		
		        		for(Token singleToken : selectCovered(Token.class, sentence)) {
		        			pos = pos + singleToken.getPos().getPosValue() + " ";
		        		}
		        		
		        		for(int i=0;i<sentimentLexicons.size();i++) {
		        			if(split.size()>0) {
		        				singleLine.add("" + (lexi_avg[i]/split.size()));
		        			} else {
		        				singleLine.add("0");
		        			}
		        			singleLine.add("" + lexi_high[i]);
		        			singleLine.add("" + lexi_low[i]);
		        		}
		        		
		        		singleLine.add("'target_" + t1.getCoveredText().replaceAll(regexIgnore, "") + "'");
		        		singleLine.add("'" + sentence.getCoveredText().replaceAll(regexIgnore, "") + "'");
		        		singleLine.add("'" + pos.replaceAll(regexIgnore, "") +"'");
		        		singleLine.add("" + unigramCount);
		        		singleLine.add("" + ((sentence.getCoveredText().endsWith("!") || sentence.getCoveredText().endsWith("?")) ? 1 : 0));
		        		
		        		singleLine.add("" + selectCovered(Valence.class, sentence).size());
		        		singleLine.add("" + (containsCaps ? 1 : 0));
		        		
		        		for(String negationWord : negationWords) {
		        			singleLine.add("" + (sentence.getCoveredText().toLowerCase().contains(negationWord) ? 1 : 0));
		    			}
		        		
		        		singleLine.add(ar.getAspect().replaceAll("[^\\x00-\\x7F]", ""));
		        		singleLine.add(singleValence.getValenceRating().replaceAll("rate-me", "negative").replaceAll("UNSURE", "negative"));
		        		
		        		sortedLines.add(singleLine);
		        		returnList.add(singleLine);
		        		
		        		valueId++;
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
		sentimentLexicons = new ArrayList<SentimentLexicon>();
		sentimentLexicons.add(new AFINN());
		sentimentLexicons.add(new BingLiu());
		sentimentLexicons.add(new EmoLex());
		
		negationWords = new ArrayList<String>();
		negationWords.add("not");
		negationWords.add("n't");
		negationWords.add("non");
		negationWords.add("don't");
		negationWords.add("didn't");
		negationWords.add("but");
		
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
			relations.add("lexi1_avg numeric");
			relations.add("lexi1_high numeric");
			relations.add("lexi1_low numeric");
			relations.add("lexi2_avg numeric");
			relations.add("lexi2_high numeric");
			relations.add("lexi2_low numeric");
			relations.add("lexi3_avg numeric");
			relations.add("lexi3_high numeric");
			relations.add("lexi3_low numeric");
			
			relations.add("target string");
			relations.add("unigrams string");
			relations.add("POS string");
			relations.add("unigrams_count numeric");
			relations.add("ends_on_special numeric");
			
			relations.add("different_aspects numeric");
			relations.add("includes_capslock numeric");
			
			int counter=1;
			for(String negationWord : negationWords) {
				relations.add("negation_type_" + counter + " numeric");
				counter++;
			}
			
			relations.add("type string");
			
			if(!useOldData) {
				relations.add("aspecttype " + generateTupel(new String[] {"positive", "negative"}));
			} else {
				relations.add("aspecttype " + generateTupel(new String[] {"positive", "neutral", "negative"}));
			}
			
			allClassAttributes.add(types[i]);
			
			ArrayList<String> newList = new ArrayList<String>();
			newList.add(types[i]);
			
			this.relations.add(relations);
			identifierAttributeAt = relations.size()-2;
		}
		
		ignoreFeatures = new int[1];
		ignoreFeatures[0] = identifierAttributeAt;
		
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
