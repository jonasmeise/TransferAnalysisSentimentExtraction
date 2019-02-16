package de.unidue.langtech.bachelor.meise.sentimentlexicon;

import java.io.IOException;
import java.util.ArrayList;

import de.unidue.langtech.bachelor.meise.type.SentimentLexicon;

public class BingLiu extends SentimentLexicon {

	String filePositive = "src/main/resources/sentimentlexicon/lexicon2/positive-words.txt";
	String fileNegative = "src/main/resources/sentimentlexicon/lexicon2/negative-words.txt";
	
	public BingLiu() {
		super(false);
		loadFromFile();
	}

	@Override
	public double fetchPolarity(String word, String[] options) {
		double returnValue = 0;
		
		if(options.length>=1) {
			if(options[0].compareTo("positive")==0) {
				returnValue = posMap.containsKey(word) ? 1 : 0;
			} else if(options[0].compareTo("negative")==0) {
				returnValue = negMap.containsKey(word) ? 1 : 0;
			}
		} else {
			returnValue = (posMap.containsKey(word) || negMap.containsKey(word)) ? 1 : 0;
		}
		
		return returnValue;
	}

	@Override
	public void loadFromFile() {
		try {
			fu.createWriter(filePositive);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ArrayList<String> allDataPositive = fu.readFromFileArrayList(filePositive);
		fu.close();
		
		try {
			fu.createWriter(fileNegative);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ArrayList<String> allDataNegative = fu.readFromFileArrayList(fileNegative);
		fu.close();

		for(String positive : allDataPositive) {
			if(!positive.startsWith(";")) {
				posMap.put(positive, (double) 1);
			}
		}
		
		for(String negative : allDataNegative) {
			if(!negative.startsWith(";")) {
				negMap.put(negative, (double) 1);
			}
		}
		
		myLog.log("Finished loading a total of " + (negMap.size()+posMap.size()) + " entries.");
	}

}
