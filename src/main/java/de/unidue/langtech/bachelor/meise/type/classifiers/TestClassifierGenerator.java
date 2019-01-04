package de.unidue.langtech.bachelor.meise.type.classifiers;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unidue.langtech.bachelor.meise.type.ArffGenerator;

public class TestClassifierGenerator extends ArffGenerator{

	int id;
	
	@Override
	public ArrayList<String> generateRelations() {
		ArrayList<String> relations = new ArrayList<String>();
		
		relations.add("id numeric");
		relations.add("text string");
		relations.add("tokendistance numeric");
		relations.add("dependencytreedistance numeric");
		relations.add("emotionalrating numeric");
		relations.add("negated boolean");
		
		String[] types = new String[16];
		types[0] = "Ausstattung-positive";
		types[1] = "Hotelpersonal-positive";
		types[2] = "Lage-positive";
		types[3] = "OTHER-positive";
		types[4] = "Komfort-positive";
		types[5] = "Preis-Leistungs-Verhaeltnis-positive";
		types[6] = "WLAN-positive";
		types[7] = "Sauberkeit-positive";
		types[8] = "Ausstattung-negative";
		types[9] = "Hotelpersonal-negative";
		types[10] = "Lage-negative";
		types[11] = "OTHER-negative";
		types[12] = "Komfort-negative";
		types[13] = "Preis-Leistungs-Verhaeltnis-negative";
		types[14] = "WLAN-negative";
		types[15] = "Sauberkeit-negative";
		
		relations.add("aspecttype " + generateTupel(types));
		
		return relations;
	}

	@Override
	public ArrayList<ArrayList<String>> generateData(JCas arg0) {
		Collection<Sentence> sentences = JCasUtil.select(arg0, Sentence.class);
		ArrayList<Token> tokens = new ArrayList<Token>();
		
        for(Sentence sentence : sentences) {
        	for(Token token : JCasUtil.select(arg0, Token.class)) {
        		//KA wie man for(Token t : Sentence) implementiert...
        		while(token.getBegin() <= sentence.getEnd()) {
        			tokens.add(token);
        		}
        	}
        	
        	//generate Tokens-Relationship
        	
        	
            System.out.println(sentences.size() + " -" + id + "- " + sentence.getCoveredText());
            id++;
        }
		return new ArrayList<ArrayList<String>>();
	}
}
