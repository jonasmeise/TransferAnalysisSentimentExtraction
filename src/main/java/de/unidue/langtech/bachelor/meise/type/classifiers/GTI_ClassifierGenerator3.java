package de.unidue.langtech.bachelor.meise.type.classifiers;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.unidue.langtech.bachelor.meise.files.RawJsonReviewReader;
import de.unidue.langtech.bachelor.meise.sentimentlexicon.AFINN;
import de.unidue.langtech.bachelor.meise.sentimentlexicon.BingLiu;
import de.unidue.langtech.bachelor.meise.sentimentlexicon.CustomLexicon;
import de.unidue.langtech.bachelor.meise.sentimentlexicon.EmoLex;
import de.unidue.langtech.bachelor.meise.type.ArffGenerator;
import de.unidue.langtech.bachelor.meise.type.ReviewData;
import de.unidue.langtech.bachelor.meise.type.SentimentLexicon;
import de.unidue.langtech.bachelor.meise.type.StopwordHandler;
import de.unidue.langtech.bachelor.meise.type.Tree;
import webanno.custom.AspectRating;
import webanno.custom.Valence;

//BASED ON THIS MODEL (BASIC FEATURE SET, SECTION 2.2.1) DESCRIBED IN THIS PAPER:
//http://aclweb.org/anthology/S/S16/S16-1049.pdf
//GTI at SemEval-2016 Task 5: SVM and CRF for Aspect Detection and Unsupervised Aspect-Based Sentiment Analysis
//Tamara Alvarez-Lopez, Jonathan Juncal-Martnez, Milagros Fernandez-Gavilanes Enrique Costa-Montenegro, Francisco Javier Gonzalez-Casta no
//Classification type: NONE, Syntactic-rules only (no learning necessary)
//Lexica used: AFINN, BingLiu, EmoLex
//input-output format is normalized to match the current Task; the model itself is not changed


public class GTI_ClassifierGenerator3 extends ArffGenerator{
	int cutoff = 200; //Maximale Saetze/Datensatz
	int dataCutoff = 0; //Maximale Datenentries pro Datensatz
	int valueId=0;
	
	public int hit=0, noHit=0;
	
	String regexIgnore = "[\'\"]";
	String regexSplit = "[\\.\\?,\\-\\!;]";
	
	RawJsonReviewReader trainingReader;
	
	Collection<String> negationWords;
	ArrayList<SentimentLexicon> sentimentLexicons;
	ArrayList<ArrayList<String>> sortedLines = new ArrayList<ArrayList<String>>();
	
	public void splitAndGuess(String splitRegex, String text, Collection<Token> sentenceTokens, ArrayList<String> tarValues, ArrayList<String> polarities) {
		String[] split = text.split(splitRegex);
		for(int i=0;i<split.length;i++) {
			String guess;
			double currentSentiment=0;
			
			for(int x=0;x<split[i].split(" ").length;x++) {
				String singleString = split[i].split(" ")[x];			
				boolean negated=false;
				
				if(x>0) {
					for(String negationWord : negationWords) {
						if(split[i].split(" ")[x-1].toLowerCase().contains(negationWord)) {
							for(Token singleToken : sentenceTokens) {
								if(singleToken.getPos().getPosValue().contains("JJ") && singleToken.getCoveredText().equals(split[i].split(" ")[x-1])) {
        							negated=true;
								}
							}
						}
					}
				}
				
				for(SentimentLexicon singleLexi : sentimentLexicons) {
					double change = singleLexi.fetchPolarity(singleString.toLowerCase(), new String[] {"positive"}) - singleLexi.fetchPolarity(singleString.toLowerCase(), new String[] {"negative"});
					if(change>=0) {
						currentSentiment += (negated ? -1 : 1) * change;
					} else {
						currentSentiment += (negated ? 0.5 : 1) * change;
					}
        		}
				negated=false;
				
				currentSentiment = currentSentiment/(split[i].split(" ").length * sentimentLexicons.size());
			}
			
			for(String singleTarget : tarValues) {
				if(split[i].contains(singleTarget)) {
					if(currentSentiment==0) {
        				guess = "neutral";
	        		} else if(currentSentiment > 0) {
	        			guess = "positive";
	        		} else {
	        			guess = "negative";
	        		}
					
					myLog.log("Guess:" + guess + "  - " + polarities.get(tarValues.indexOf(singleTarget)).toLowerCase());
	        				
					if(guess.equals(polarities.get(tarValues.indexOf(singleTarget)).toLowerCase())) {
						hit++;
					} else {
						noHit++;
					}
				}
			}
		}
	}
	
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
	        	
	        	double avgPolarity = 0;
	        	
	        	Collection<Token> sentenceTokens = selectCovered(Token.class, sentence);
	        	
	        	for(String singleString : currentData.getTextContent().split(" ")) {
	        		for(SentimentLexicon singleLexi : sentimentLexicons) {
	        			avgPolarity += singleLexi.fetchPolarity(singleString.toLowerCase(), new String[] {"positive"}) - singleLexi.fetchPolarity(singleString.toLowerCase(), new String[] {"negative"});
	        		}
	        	}
	        	
	        	avgPolarity = avgPolarity / (sentimentLexicons.size() * sentenceTokens.size());
	        	String guess="";
        	
	        	if(tarValues.size()==1 || tarValues.size()==0) {
        			if(categories.size() > 0) {
		        		if(avgPolarity==0) {
	        				guess = "neutral";
		        		} else if(avgPolarity > 0) {
		        			guess = "positive";
		        		} else {
		        			guess = "negative";
		        		}
		        		
		        		myLog.log("Guess:" + guess + "  - " + polarities.get(0).toLowerCase());
		        		
		        		if(guess.equals(polarities.get(0).toLowerCase())) {
    						hit++;
    					} else {
    						noHit++;
    					}
        			}
	        	} else if(tarValues.size()>1 && !categories.get(0).equals(categories.get(1))) {
	        		if(currentData.getTextContent().toLowerCase().contains("but")) {
	        			splitAndGuess("but", currentData.getTextContent(), sentenceTokens, tarValues, polarities);
	        		} else {
	        			splitAndGuess("[;,\\.\\?!-]", currentData.getTextContent(), sentenceTokens, tarValues, polarities);
	        		}
	        	}
        	} else {
        		ArrayList<String> tarValues = new ArrayList<String>();
	        	ArrayList<String> categories = new ArrayList<String>();
	        	ArrayList<String> polarities = new ArrayList<String>();
	        	
        		
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
		        		
		        		tarValues.add(t1.getCoveredText());
		        		categories.add(ar.getAspect().replaceAll("[^\\x00-\\x7F]", ""));
		        		polarities.add(singleValence.getValenceRating().replaceAll("rate-me", "negative").replaceAll("UNSURE", "negative"));
	        		}	
				}
        		
        		Collection<Token> split = selectCovered(Token.class, sentence);

	        	double avgPolarity = 0;
	        	
	        	Collection<Token> sentenceTokens = selectCovered(Token.class, sentence);
	        	
	        	for(Token singleToken : split) {
	        		String singleString = singleToken.getCoveredText();
	        		for(SentimentLexicon singleLexi : sentimentLexicons) {
	        			avgPolarity += singleLexi.fetchPolarity(singleString.toLowerCase(), new String[] {"positive"}) - singleLexi.fetchPolarity(singleString.toLowerCase(), new String[] {"negative"});
	        		}
	        	}
	        	
	        	avgPolarity = avgPolarity / (sentimentLexicons.size() * sentenceTokens.size());
	        	String guess="";
        	
	        	if(tarValues.size()==1 || tarValues.size()==0) {
        			if(categories.size() > 0) {
		        		if(avgPolarity==0) {
	        				guess = "neutral";
		        		} else if(avgPolarity > 0) {
		        			guess = "positive";
		        		} else {
		        			guess = "negative";
		        		}
		        		
		        		myLog.log("Guess:" + guess + "  - " + polarities.get(0).toLowerCase());
		        		
		        		if(guess.equals(polarities.get(0).toLowerCase())) {
    						hit++;
    					} else {
    						noHit++;
    					}
        			}
	        	} else if(tarValues.size()>1) {
	        		if(sentence.getCoveredText().contains("but")) {
	        			splitAndGuess("but", sentence.getCoveredText(), sentenceTokens, tarValues, polarities);
	        		} else {
	        			splitAndGuess("[;,\\.\\?!-]", sentence.getCoveredText(), sentenceTokens, tarValues, polarities);
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
		
		ArrayList<ArrayList<String>> a = new ArrayList<ArrayList<String>>();
		
		negationWords = new ArrayList<String>();
		negationWords.add("not");
		negationWords.add("n't");
		negationWords.add("non");
		negationWords.add("don't");
		negationWords.add("didn't");
		negationWords.add("no");
		
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
			types = getAspectLabelsOldData();
			
			myReader = new RawJsonReviewReader();
			myReader.folderPath = "src\\main\\resources\\SEABSA16_data";
			myReader.useOldData = true;
			
			StopwordHandler myHandler = new StopwordHandler();
			
			 trainingReader = new RawJsonReviewReader();
			 trainingReader.folderPath = myReader.folderPath;
			 trainingReader.useOldData = true;
			 trainingReader.fileExtension = ".xml"; //training data
			 
			 trainingReader.external_Initialize(-1);
			 
			 myLog.log("Initializing Test Reader.");
			 
			 CustomLexicon newLexi = new CustomLexicon();
			 
			 HashMap<String, Integer> neuContext = new HashMap<String, Integer>();
			 HashMap<String, Integer> posContext = new HashMap<String, Integer>();
			 HashMap<String, Integer> negContext = new HashMap<String, Integer>();
			 
			for(int i=0;i<trainingReader.getReviewList().size();i++) {
				String currentString = trainingReader.getReview(i).getTextContent();
				
				for(String word : currentString.split(" ")) {
					if(word.length() >= 3 && !myHandler.isStopword(word)) {
						for(String polarity : trainingReader.getReview(i).getOpinionPolarity()) {
							if(polarity.equals("positive")) {
								if(!posContext.containsKey(word)) {
									posContext.put(word, 1);
								} else {
									posContext.put(word, posContext.get(word) + 1);
								}
							} else if(polarity.equals("negative")) {
								if(!negContext.containsKey(word)) {
									negContext.put(word, 1);
								} else {
									negContext.put(word, negContext.get(word) + 1);
								}
							}
							
							if(!neuContext.containsKey(word)) {
								neuContext.put(word, 1);
							} else {
								neuContext.put(word, neuContext.get(word) + 1);
							}
						}
					}
				}
			}
			
			for(String key : neuContext.keySet()) {
				if(posContext.containsKey(key) && negContext.containsKey(key)) {
					newLexi.feedData(key, (posContext.get(key) - negContext.get(key))/neuContext.get(key));
				} else if(posContext.containsKey(key)) {
					newLexi.feedData(key, 1);
				} else if(negContext.containsKey(key)) {
					newLexi.feedData(key, -1);
				} else {
					newLexi.feedData(key, 0);
				}
			}
			
			sentimentLexicons.add(newLexi);
			
			if(!isTestData ) {
				myReader.fileExtension = ".xml";
			} else {
				myReader.fileExtension = ".gold";
			}
			
			myReader.external_Initialize(-1);
			
		}

		return a;
	}

	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		myLog.log("Finished! Total of " + data.size() + " entries added.");
		//we'll write out own method...
		//writeOutput();
		//writeOutput(sortedLines);
		
		myLog.log(hit + "-" + noHit);
	}	
	
	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
		generateData(arg0, cutoff);
	}
}
