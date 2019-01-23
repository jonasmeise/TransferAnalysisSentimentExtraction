package de.unidue.langtech.bachelor.meise.sentimentlexicon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import de.unidue.langtech.bachelor.meise.type.SentimentLexicon;

public class EmoLex extends SentimentLexicon {
	public EmoLex() {
		super(true, "src/main/resources/sentimentlexicon/lexicon1/NRC-Emotion-Lexicon-v0.92/NRC-Emotion-Lexicon-Senselevel-v0.92.txt");
	}
	
	@Override
	public double fetchPolarity(String word, String[] options) {
		double returnValue=0;
		String type;
		
		if(options.length > 0) {
			type = options[0];
		} else {
			type = "positive";
		}
		
		if(type.compareTo("positive")==0) {
			if(posMap.containsKey(word)) {
				returnValue = posMap.get(word) / neuMap.get(word);
			} else {
				returnValue = 0;
			}
		} else if(type.compareTo("negative")==0) {
			if(negMap.containsKey(word)) {
				returnValue = negMap.get(word) / neuMap.get(word);
			} else {
				returnValue = 0;
			}
		} else {
			returnValue = 0;
		}
		
		return returnValue;
	}

	@Override
	public void loadFromFile() {
		try {
			fu.createWriter(getFilePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ArrayList<String> allData = fu.readFromFileArrayList(getFilePath());
		
		for(int i=0;i<allData.size();i=i+10) {
			String currentWord = allData.get(i).split("--")[0];
			
			if(i+5<allData.size()) {
				Double positiveValue=Double.valueOf(allData.get(i+5).split("\t")[2]);
				Double negativeValue=Double.valueOf(allData.get(i+6).split("\t")[2]);
				
				if(!neuMap.containsKey(currentWord)) {
					neuMap.put(currentWord, (double) 1);
				} else {
					neuMap.put(currentWord, neuMap.get(currentWord) + 1);
				}
				
				if(!posMap.containsKey(currentWord)) {
					posMap.put(currentWord, positiveValue);
				} else {
					posMap.put(currentWord, posMap.get(currentWord) + positiveValue);
				}
				
				if(!negMap.containsKey(currentWord)) {
					negMap.put(currentWord, negativeValue);
				} else {
					negMap.put(currentWord, negMap.get(currentWord) + negativeValue);
				}
			}
		}
		
		myLog.log("Finished loading a total of " + neuMap.size() + " entries from '" + getFilePath() + "'.");
	}

}
