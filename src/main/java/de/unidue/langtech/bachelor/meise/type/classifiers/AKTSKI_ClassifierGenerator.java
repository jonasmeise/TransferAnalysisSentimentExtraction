package de.unidue.langtech.bachelor.meise.type.classifiers;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.unidue.langtech.bachelor.meise.files.RawJsonReviewReader;
import de.unidue.langtech.bachelor.meise.sentimentlexicon.*;
import de.unidue.langtech.bachelor.meise.type.ArffGenerator;
import de.unidue.langtech.bachelor.meise.type.ReviewData;
import de.unidue.langtech.bachelor.meise.type.SentimentLexicon;
import de.unidue.langtech.bachelor.meise.type.StopwordHandler;
import de.unidue.langtech.bachelor.meise.type.Tree;
import webanno.custom.AspectRating;
import webanno.custom.Valence;

//BASED ON THIS MODEL (BASIC FEATURE SET, SECTION 3.2) DESCRIBED IN THIS PAPER:
//http://aclweb.org/anthology/S/S16/S16-1051.pdf
//AKTSKI at SemEval-2016 Task 5: Aspect Based Sentiment Analysis for Consumer Reviews
//Pateria, Choubey
//Classification type: SVM-RBF, C=100, gamma=0.001
//tar-values for Hotels are added
//Sentiment lexicons are averaged prior to the evaluation -> as a feature: we assume it's averaged over the SubSent (not clear)
//Keywords for each Term (TF-IDF transformations) are generated in String2word-function within AspectClassifier.java
//input-output format is normalized to match the current Task; the model itself is not changed

public class AKTSKI_ClassifierGenerator extends ArffGenerator{

	int cutoff = 200; //Maximale Saetze/Datensatz
	int dataCutoff = 0; //Maximale Datenentries pro Datensatz
	int valueId=0;
	String regexIgnore = "[^a-zA-Z\\s!_]";
	
	ArrayList<ArrayList<String>> sortedLines = new ArrayList<ArrayList<String>>();
	ArrayList<String> tagWordsForAspects;
	ArrayList<SentimentLexicon> sentimentLexicons;
	Collection<String> neutralWords;
	Collection<String> negationWords;
	StopwordHandler myStopwordHandler;
	
	//for non-pipelined access
	public AKTSKI_ClassifierGenerator() {
		super();
	}
	
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
	        	ArrayList<String> categories = currentData.getOpinionCategory();
	        	ArrayList<String> polarities = currentData.getOpinionPolarity();
	        	
	        	//myLog.log(tarValues);
	        	//myLog.log(categories);
	        	//myLog.log(polarities);
	        	
	        	int counter=0;
	        	
	        	for(String singleTargetValue : tarValues) {
	        		ArrayList<String> singleLine = new ArrayList<String>();
	        		ArrayList<String> dependencyWords = new ArrayList<String>();
	        		
	        		singleLine.add("" + valueId);
	        		
	        		if(singleTargetValue.equals("NULL")) {
	        			for(String singleWordInSentence : sentence.getCoveredText().replaceAll("[\\.,]", "").split(" ")) {
	        				for(String tagWord : tagWordsForAspects.get(0).split(" ")) {
	        					if(tagWord.equals(singleWordInSentence.toLowerCase())) {
	        						singleTargetValue = tagWord;
	        					}
	        				}
	        			}
	        		}
	        		
	        		if(singleTargetValue.equals("NULL")) {
	        			for(Token singleToken : selectCovered(Token.class, sentence)) {
        					if(singleToken.getPos().getPosValue().contains("NN")) {
        						singleTargetValue = singleToken.getCoveredText();
	        				}
	        			}
	        		}
	        		
	        		for(Dependency dpElement : dependencies) {
	        			for(String singleValue : singleTargetValue.split(" ")) {
		        			if(dpElement.getDependent().getCoveredText().equals(singleValue)) {
		        				if(!dependencyWords.contains(dpElement.getGovernor().getCoveredText())) {
		        					dependencyWords.add(dpElement.getGovernor().getCoveredText());
		        				}
		        			} else if(dpElement.getGovernor().getCoveredText().equals(singleValue)) {
		        				if(!dependencyWords.contains(dpElement.getDependent().getCoveredText())) {
		        					dependencyWords.add(dpElement.getDependent().getCoveredText());
		        				}
		        			}
	        			}
	        		}
	        		
	        		String dependencyOutputUni = "";
	        		String dependencyOutputBi = "";
	        		double dependencyOutputSentimentAvg = 0;
	        		boolean sentenceContainsNegation = false;
	        		
	        		for(String negationWord : negationWords) {
	        			if(sentence.getCoveredText().toLowerCase().contains(negationWord)) {
	        				sentenceContainsNegation = true;
	        			}
	        		}
	        		
	        		for(String singleWord : dependencyWords) {
	        			if(!myStopwordHandler.isStopword(singleWord.toLowerCase())) {
		        			dependencyOutputUni = dependencyOutputUni + "dep_" + singleWord + " ";
		        			
		        			if(dependencyWords.indexOf(singleWord)+1 < dependencyWords.size()) {
		        				dependencyOutputBi = dependencyOutputBi + "dep_" + singleWord + "_" + dependencyWords.get(dependencyWords.indexOf(singleWord)+1) + " ";
		        			}
		        			
		        			for(SentimentLexicon singleLexi : sentimentLexicons) {
		        				dependencyOutputSentimentAvg = dependencyOutputSentimentAvg + singleLexi.fetchPolarity(singleWord, new String[] {"positive"}) -singleLexi.fetchPolarity(singleWord, new String[] {"negative"}); 
		        			}
	        			}
	        		}
	        		
	        		dependencyOutputSentimentAvg = ((sentenceContainsNegation ? -1 : 1 ) * dependencyOutputSentimentAvg) / (dependencyWords.size() * sentimentLexicons.size());
	        		
	        		if(dependencyWords.size() > 0) {
	        			singleLine.add("" + dependencyOutputSentimentAvg);
	        		} else {
	        			singleLine.add("0");
	        		}
	        		
	        		singleLine.add("'" + singleTargetValue.replaceAll(regexIgnore, "") + "'");
	        		singleLine.add("'" + dependencyOutputUni.replaceAll(regexIgnore, "") +"'");
	        		singleLine.add("'" + dependencyOutputBi.replaceAll(regexIgnore, "") +"'");
	        		
	        		for(String neutralWord : neutralWords) {
	        			singleLine.add("" + (sentence.getCoveredText().toLowerCase().contains(neutralWord) ? 1 : 0));
	    			}
	        		
	        		singleLine.add("" + (sentence.getCoveredText().toLowerCase().contains("!") ? 1 : 0));
	        		singleLine.add("" + (sentence.getCoveredText().toLowerCase().contains("?") ? 1 : 0));
	        		
	        		singleLine.add(categories.get(counter).toLowerCase());
	        		singleLine.add(polarities.get(counter));
	        		
	        		sortedLines.add(singleLine);
	        		returnList.add(singleLine);
	        		
	        		counter++;
	        		valueId++;
	        	}
			} else {
				Collection<Dependency> dependencies =  selectCovered(Dependency.class, sentence);
				
	        	ArrayList<Token> roots = new ArrayList<Token>();
	        	for(Dependency dpElement : dependencies) {
					if(dpElement.getDependencyType().toLowerCase().compareTo("root") == 0) {
						roots.add(dpElement.getGovernor());
					}
				}
	        	
	        	Collection<Valence> valences = selectCovered(Valence.class, sentence);
	        	
	        	for(Valence singleValence : valences) {
	        		ArrayList<String> singleLine = new ArrayList<String>();
	        		Token t1;
	        		AspectRating ar;
	        		
	        		if(singleValence.getDependent()!=null && singleValence.getDependent().getAspect()!=null && singleValence.getGovernor()!=null && singleValence.getGovernor().getAspect()!=null && singleValence.getValenceRating()!="null" && singleValence.getValenceRating()!=null) {
		        		if(singleValence.getDependent().getAspect().toLowerCase().equals("ratingofaspect")) {
	        				t1 = JCasUtil.selectCovered(Token.class, singleValence.getGovernor()).get(0);
	        				ar = singleValence.getGovernor();
		        		} else {
		        			t1 = JCasUtil.selectCovered(Token.class, singleValence.getDependent()).get(0);
		        			ar = singleValence.getDependent();
		        		}
		        		
		        		ArrayList<String> dependencyWords = new ArrayList<String>();
		        		
		        		singleLine.add("" + valueId);
		        		
		        		for(Dependency dpElement : dependencies) {
		        			if(dpElement.getDependent().equals(t1)) {
		        				if(!dependencyWords.contains(dpElement.getGovernor().getCoveredText())) {
		        					dependencyWords.add(dpElement.getGovernor().getCoveredText());
		        				}
		        			} else if(dpElement.getGovernor().equals(t1)) {
		        				if(!dependencyWords.contains(dpElement.getDependent().getCoveredText())) {
		        					dependencyWords.add(dpElement.getDependent().getCoveredText());
		        				}
		        			}
		        		}
		        		
		        		String dependencyOutputUni = "";
		        		String dependencyOutputBi = "";
		        		double dependencyOutputSentimentAvg = 0;
		        		boolean sentenceContainsNegation = false;
		        		
		        		for(String negationWord : negationWords) {
		        			if(sentence.getCoveredText().toLowerCase().contains(negationWord)) {
		        				sentenceContainsNegation = true;
		        			}
		        		}
		        		
		        		for(String singleWord : dependencyWords) {
		        			if(!myStopwordHandler.isStopword(singleWord.toLowerCase())) {
			        			dependencyOutputUni = dependencyOutputUni + "dep_" + singleWord + " ";
			        			
			        			if(dependencyWords.indexOf(singleWord)+1 < dependencyWords.size()) {
			        				dependencyOutputBi = dependencyOutputBi + "dep_" + singleWord + "_" + dependencyWords.get(dependencyWords.indexOf(singleWord)+1) + " ";
			        			}
			        			
			        			for(SentimentLexicon singleLexi : sentimentLexicons) {
			        				dependencyOutputSentimentAvg = dependencyOutputSentimentAvg + singleLexi.fetchPolarity(singleWord, new String[] {"positive"}) -singleLexi.fetchPolarity(singleWord, new String[] {"negative"}); 
			        			}
		        			}
		        		}
		        		
		        		dependencyOutputSentimentAvg = ((sentenceContainsNegation ? -1 : 1 ) * dependencyOutputSentimentAvg) / (dependencyWords.size() * sentimentLexicons.size());
		        		
		        		if(dependencyWords.size() > 0) {
		        			singleLine.add("" + dependencyOutputSentimentAvg);
		        		} else {
		        			singleLine.add("0");
		        		}
		        		
		        		singleLine.add("'" + t1.getCoveredText().replaceAll(regexIgnore, "") + "'");
		        		singleLine.add("'" + dependencyOutputUni.replaceAll(regexIgnore, "") +"'");
		        		singleLine.add("'" + dependencyOutputBi.replaceAll(regexIgnore, "") +"'");
		        		
		        		for(String neutralWord : neutralWords) {
		        			singleLine.add("" + (sentence.getCoveredText().toLowerCase().contains(neutralWord) ? 1 : 0));
		    			}
		        		
		        		singleLine.add("" + (sentence.getCoveredText().toLowerCase().contains("!") ? 1 : 0));
		        		singleLine.add("" + (sentence.getCoveredText().toLowerCase().contains("?") ? 1 : 0));
		        		
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

	//acts as initialize2.0
	@Override
	public ArrayList<ArrayList<String>> generateRelations() {
		myStopwordHandler = new StopwordHandler(stopwordsFile);
		
		sentimentLexicons = new ArrayList<SentimentLexicon>();
		sentimentLexicons.add(new AFINN());
		sentimentLexicons.add(new BingLiu());
		sentimentLexicons.add(new EmoLex());
		
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
		
		negationWords = new ArrayList<String>();
		negationWords.add("not");
		negationWords.add("n't");
		negationWords.add("non");
		negationWords.add("don't");
		negationWords.add("didn't");
		negationWords.add("but");
		
		tagWordsForAspects = new ArrayList<String>();
		
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
			
			/*tagWordsForAspects.add("positive bar bath pool facility hotel onsen restaurant place spa");
			tagWordsForAspects.add("service staff staffs concierge he she they lady woman ma receptionist");
			tagWordsForAspects.add("area station subway restaurant hotel view metro airport location distance access");
			tagWordsForAspects.add("time check coffee egg water tea experience breakfast stay food everything choice");
			tagWordsForAspects.add("bathroom furniture space door room pillow bed amenities shower");
			tagWordsForAspects.add("money price cost charge value");
			tagWordsForAspects.add("wifi internet");
			tagWordsForAspects.add("room towel window water smell cleaning shower hotel");*/
		} else {
			//load data separately for cross-checking
			myReader = new RawJsonReviewReader();
			myReader.folderPath = "src\\main\\resources\\SEABSA16_data";
			myReader.useOldData = true;
			myReader.fileExtension = ".xml";
			myReader.external_Initialize(-1);
			types = getAspectLabelsOldData();
			tagWordsForAspects.add("food drink service waiter price staff ambiance he she they");
		}
		
		for(int i=0;i<types.length;i++) {
			ArrayList<String> relations = new ArrayList<String>();
			
			relations.add("id numeric");
			relations.add("sentiment_average numeric");
			relations.add("target_word string");

			relations.add("dependency_related_unigrams string");
			relations.add("dependency_related_bigrams string");
			
			int counter=0;
			for(String neutralWord : neutralWords) {
				counter++;
				relations.add("feature_" + counter + " numeric");
			}

			
			relations.add("punctuation_exclamation numeric");
			relations.add("punctuation_question numeric");
			
			relations.add("type " + generateTupel(types));
			
			if(!useOldData) {
				relations.add("aspecttype " + generateTupel(new String[] {"positive", "negative"}));
			} else {
				relations.add("aspecttype " + generateTupel(new String[] {"positive", "neutral", "negative"}));
			}
			
			allClassAttributes.add(types[i]);
			
			identifierAttributeAt=relations.size()-2; //second-to-last element contains identifier attribute
			ignoreFeatures = new int[1];
			ignoreFeatures[0]=identifierAttributeAt;
			
			this.relations.add(relations);
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
