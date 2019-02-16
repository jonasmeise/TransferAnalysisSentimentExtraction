package de.unidue.langtech.bachelor.meise.type.classifiers;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.unidue.langtech.bachelor.meise.files.RawJsonReviewReader;
import de.unidue.langtech.bachelor.meise.sentimentlexicon.AFINN;
import de.unidue.langtech.bachelor.meise.sentimentlexicon.BingLiu;
import de.unidue.langtech.bachelor.meise.sentimentlexicon.EmoLex;
import de.unidue.langtech.bachelor.meise.type.ArffGenerator;
import de.unidue.langtech.bachelor.meise.type.SentimentLexicon;
import de.unidue.langtech.bachelor.meise.type.StopwordHandler;
import de.unidue.langtech.bachelor.meise.type.Tree;
import webanno.custom.AspectRating;
import webanno.custom.Valence;

public class RQ2_ReviewLevelRegression_ClassifierGenerator extends ArffGenerator{
	public static final String PARAM_XML_PATH = "xmlPath";
    @ConfigurationParameter(name = PARAM_XML_PATH, mandatory = true)
    public String xmlPath;
	
    private RawJsonReviewReader myReader;
    
	int cutoff = 200; //Maximale Saetze/Datensatz
	int dataCutoff = 0; //Maximale Datenentries pro Datensatz
	int valueId=0;
	double currentScore=0;
	
	String regexIgnore = "[\'\"]";
	String regexSplitParts = "( .E )";
	ArrayList<String> negationWords;
	ArrayList<SentimentLexicon> sentimentLexicons;
	StopwordHandler myStopwordHandler;
	String fullSentenceString;
	
	ArrayList<ArrayList<String>> sortedLines = new ArrayList<ArrayList<String>>();
	
	@Override
	public Collection<ArrayList<String>> generateFeaturesFromCas(Sentence sentence) {
		ArrayList<ArrayList<String>> returnList = new ArrayList<ArrayList<String>>();
		
		if(valueId < dataCutoff || dataCutoff==0) { //check if max amount of data is enabled
        	for(String singleClass : allClassAttributes) {
        		Collection<Valence> valences = selectCovered(Valence.class, sentence);
        		Collection<Token> fullSentenceToken = selectCovered(Token.class, sentence);
    			double lexicons_min=0;
    			double lexicons_max=0;

    			String fullSentenceString = myReader.getText(valueId);
    			myLog.log(fullSentenceString);
    			myLog.log(sentence.getCoveredText());
    			
				currentScore = myReader.getScore(valueId);
				//process the entire sentence
    			if(valueId%100==0) {
    				myLog.log(valueId);
    			}
    			
    			ArrayList<String> singleLine = new ArrayList<String>();
    			String positivePart = "";
    			String negativePart = "";
    			String[] splitReview = fullSentenceString.split(regexSplitParts);
    		
    			double maxPos=0, maxNeg=0, minPos=0, minNeg=0;
    			
    			myLog.log("Split review into " + splitReview.length + " parts.");
    			
    			if(splitReview.length==2) {
    				//we don't know if it's positive or negative....
    				//TODO: negation and shifters
    				for(Token singleToken : fullSentenceToken) {
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
        				
        			if(lexicons_max > (-1 * lexicons_min)) {
        				positivePart = splitReview[1];
        			} else {
        				negativePart = splitReview[1];
        			}
    			} else if(splitReview.length==3) {
    				positivePart = splitReview[1];
    				negativePart = splitReview[2];
    			} else {
    				myLog.log("Unknown message structure!");
    			}
    			
    			ArrayList<Token> posTokens = new ArrayList<Token>();
    			ArrayList<Token> negTokens = new ArrayList<Token>();
    			
    			for(Token singleToken : fullSentenceToken) {
    				if(positivePart.contains(singleToken.getCoveredText())) {
    					posTokens.add(singleToken);
    				} else if (negativePart.contains(singleToken.getCoveredText())){
    					negTokens.add(singleToken);
    				} else {
    					myLog.log("Unsure what to do about '" + singleToken.getCoveredText() + "' in " + fullSentenceString);
    				}
    			}
    			
    			for(Token singleToken : posTokens) {
    				for(SentimentLexicon singleLexicon : sentimentLexicons) {	
    					double newPosValue = singleLexicon.fetchPolarity(singleToken.getLemma().getValue(), new String[] {"positive"});
    					double newNegValue = singleLexicon.fetchPolarity(singleToken.getLemma().getValue(), new String[] {"negative"});
    					
    					if(newPosValue > maxPos) {
    						maxPos = newPosValue;
    					}
    					if(newNegValue < minPos) {
    						minPos = newNegValue;
    					}
    				}
				}
    			
    			for(Token singleToken : negTokens) {
    				for(SentimentLexicon singleLexicon : sentimentLexicons) {	
    					double newPosValue = singleLexicon.fetchPolarity(singleToken.getLemma().getValue(), new String[] {"positive"});
    					double newNegValue = singleLexicon.fetchPolarity(singleToken.getLemma().getValue(), new String[] {"negative"});
    					
    					if(newPosValue > maxNeg) {
    						maxNeg = newPosValue;
    					}
    					if(newNegValue < minNeg) {
    						minNeg = newNegValue;
    					}
    				}
				}
    			
    			double relationFromPosToNeg = 0;
    			if(negativePart.length()>0) {
    				relationFromPosToNeg = positivePart.length()/negativePart.length();
    			}

    			ArrayList<Valence> posValences = new ArrayList<Valence>();
    			ArrayList<Valence> negValences = new ArrayList<Valence>();
    			
    			for(Valence valence : valences) {
    				if(posTokens.contains(selectCovered(Token.class, valence.getGovernor()).get(0))) {
    					posValences.add(valence);
    				} else if(negTokens.contains(selectCovered(Token.class, valence.getGovernor()).get(0))) {
    					negValences.add(valence);
    				} else {
    					if(valence.getValenceRating()!=null && valence.getValenceRating().equals("positive")) {
    						posValences.add(valence);
    					} else if(valence.getValenceRating()!=null && valence.getValenceRating().equals("negative")){
    						negValences.add(valence);
    					} else {
    						myLog.log("Couldn't classify valence!");
    					}
    				}
    			}
    			
    			String title = myReader.getTitle(valueId);
    			
    			String positiveLemmas = "";
    			String negativeLemmas = "";
    			String positiveValences = "";
    			String negativeValences = "";
    			
    			for(Token singleToken : posTokens) {
    				positiveLemmas += singleToken.getLemma().getValue() + " ";
    			}
    			for(Token singleToken : negTokens) {
    				negativeLemmas += singleToken.getLemma().getValue() + " ";
    			}
    			for(Valence singleValence : posValences) {
    				String prefix = "";
    				
    				if(singleValence.getValenceRating()=="negative") {
    					prefix = "_";
    				}
    				
    				positiveValences += prefix + singleValence.getGovernor().getAspect() + " " + prefix + singleValence.getDependent().getAspect() + " ";
    			}
    			for(Valence singleValence : negValences) {
    				String prefix = "";
    				
    				if(singleValence.getValenceRating()=="negative") {
    					prefix = "_";
    				}
    				
    				negativeValences += prefix + singleValence.getGovernor().getAspect() + " " + prefix + singleValence.getDependent().getAspect() + " ";
    			}
    			
				singleLine.add("" + valueId);
				
				singleLine.add("'" + title.replaceAll(regexIgnore, "").toLowerCase() + "'");
				singleLine.add("'" + positiveLemmas.replaceAll(regexIgnore, "").toLowerCase() + "'");
				singleLine.add("'" + negativeLemmas.replaceAll(regexIgnore, "").toLowerCase() + "'");
				
				if(!constrained) {
					singleLine.add("'" + positiveValences.replaceAll(regexIgnore, "").replaceAll("RatingOfAspect", "").toLowerCase() + "'");
					singleLine.add("'" + negativeValences.replaceAll(regexIgnore, "").replaceAll("RatingOfAspect", "").toLowerCase() + "'");
				} else {
					//TODO: implement bag-of-words for thematic features
					singleLine.add("0");
					singleLine.add("0");
				}
				singleLine.add("" + maxPos);
				singleLine.add("" + maxNeg);
				singleLine.add("" + minPos);
				singleLine.add("" + minNeg);
				
				singleLine.add("" + relationFromPosToNeg);
				
				singleLine.add(singleClass);
				
				if(!learningModeActivated) {
					singleLine.add("" + currentScore);
				} else {
					singleLine.add("?");
				}
				
				valueId++;
				
				returnList.add(singleLine);
				sortedLines.add(singleLine);
			}
    	}
		
		return returnList;
	}
	
	@Override
	public ArrayList<ArrayList<String>> generateRelations() {
		myReader = new RawJsonReviewReader();
		myReader.folderPath = xmlPath;
		myReader.fileExtension = "output.xml";
		myReader.external_Initialize(200);
		
		myStopwordHandler = new StopwordHandler("src\\main\\resources\\stopwords.txt");	
		
		String[] types = new String[1];
		types[0] = "review";
		
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
			
			relations.add("title string");
			relations.add("positive_part_lemma string");
			relations.add("negative_part_lemma string");
			relations.add("aspects_in_positive string");
			relations.add("aspects_in_negative string");
			relations.add("sentiment_max_in_pos numeric");
			relations.add("sentiment_max_in_neg numeric");
			relations.add("sentiment_min_in_pos numeric");
			relations.add("sentiment_min_in_neg numeric");
			
			relations.add("relation_of_type numeric");
			
			relations.add("type string");
			
			relations.add("score numeric");	
			
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
