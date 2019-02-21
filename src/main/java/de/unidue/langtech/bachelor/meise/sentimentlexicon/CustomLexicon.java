package de.unidue.langtech.bachelor.meise.sentimentlexicon;

import java.io.IOException;
import java.util.ArrayList;

import de.unidue.langtech.bachelor.meise.type.SentimentLexicon;

public class CustomLexicon extends SentimentLexicon {
	public CustomLexicon() {
		super(false);
	}
	
	@Override
	public double fetchPolarity(String word, String[] options) {
		double returnValue=0;
		
		if(neuMap.containsKey(word) && options[0].equals("positive")) {
			returnValue = neuMap.get(word);
		}
		
		return returnValue;
	}

	@Override
	public void loadFromFile() {
		myLog.log("Nope. Doesn't work. Please feed the data separately!");
	}
	
	public void feedData(String word, double rating) {
		neuMap.put(word, rating);
	}
}
