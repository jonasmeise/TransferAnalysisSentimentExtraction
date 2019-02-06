package de.unidue.langtech.bachelor.meise.sentimentlexicon;

import java.io.IOException;
import java.util.ArrayList;

import de.unidue.langtech.bachelor.meise.type.SentimentLexicon;

public class AFINN extends SentimentLexicon {
	public AFINN() {
		super(false, "src/main/resources/sentimentlexicon/lexicon3/AFINN/AFINN-111.txt");
	}
	
	@Override
	public double fetchPolarity(String word, String[] options) {
		double returnValue=0;
		
		if(neuMap.containsKey(word)) {
			returnValue = neuMap.get(word);
			
			if(options[0].compareTo("negative")==0 && returnValue>=0) {
				returnValue=0;
			} else if(options[0].compareTo("positive")==0 && returnValue<0){
				returnValue=0;
			}
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
		
		for(String data : allData) {
			String[] split = data.split("\t");
			
			if(split.length==2) {
				neuMap.put(split[0], Double.valueOf(split[1])/5);
			}
		}
		
		myLog.log("Finished loading a total of " + neuMap.size() + " entries from '" + getFilePath() + "'.");
	}
}
