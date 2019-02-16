package de.unidue.langtech.bachelor.meise.type.classifiers;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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

public class RQ2_HotelLevelRegression_ClassifierGenerator extends ArffGenerator{

    private RawJsonReviewReader myReader;
    
	int cutoff = 200; //Maximale Saetze/Datensatz
	int dataCutoff = 0; //Maximale Datenentries pro Datensatz
	int valueId=0;
	double currentScore=0;
	
	HashMap<String, Integer> myHashMap;
	
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
    			
        		int positive=0, negative=0;
        		
    			for(Valence valence : valences) {
    				if(valence.getGovernor().getAspect()!=null && valence.getDependent().getAspect()!=null) {
	    				if(singleClass.startsWith(valence.getGovernor().getAspect().substring(0,4)) || singleClass.startsWith(valence.getDependent().getAspect().substring(0,4))) {
	    					if(valence.getValenceRating()!=null) {
	    						if(!myHashMap.containsKey(singleClass + "-" + valence.getValenceRating())) {
	    							myHashMap.put(singleClass + "-" + valence.getValenceRating(), 1);
	    						} else {
	    							myHashMap.put(singleClass + "-" + valence.getValenceRating(),myHashMap.get(singleClass + "-" + valence.getValenceRating()+1));
	    						}
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
		myStopwordHandler = new StopwordHandler("src\\main\\resources\\stopwords.txt");
		myHashMap = new HashMap<String, Integer>();
		
		String[] types = new String[8];
		types[0] = "Ausstattung";
		types[1] = "Hotelpersonal";
		types[2] = "Lage";
		types[3] = "OTHER";
		types[4] = "Komfort";
		types[5] = "Preis-Leistungs-Verhltnis";
		types[6] = "WLAN";
		types[7] = "Sauberkeit";
		
		sentimentLexicons = new ArrayList<SentimentLexicon>();
		sentimentLexicons.add(new AFINN());
		sentimentLexicons.add(new BingLiu());
		sentimentLexicons.add(new EmoLex());
		
		for(int i=0;i<types.length;i++) {
			ArrayList<String> relations = new ArrayList<String>();
			
			relations.add("id numeric");
			
			relations.add("pos_examples numeric");
			relations.add("neg_examples numeric");
			
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
		for(String type : allClassAttributes) {	
			try {
				fu.createWriter(outputPath + "/type.arff");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			fu.write("@relation " + relationName + "\n");
			fu.write("@attribute id numeric");
			fu.write("@attribute pos_examples numeric");
			fu.write("@attribute neg_examples numeric");
			fu.write("@attribute score numeric\n");
			
			fu.write("@data");
			fu.
		}
		
	}	
	
	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
		generateData(arg0, cutoff);
	}
	
}
