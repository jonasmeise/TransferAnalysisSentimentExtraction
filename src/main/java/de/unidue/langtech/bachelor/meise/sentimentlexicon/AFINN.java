package de.unidue.langtech.bachelor.meise.sentimentlexicon;

import java.io.IOException;
import java.util.ArrayList;

import de.unidue.langtech.bachelor.meise.type.SentimentLexicon;

public class AFINN extends SentimentLexicon {
	private static String filePath = "src/main/resources/sentimentlexicon/lexicon3/AFINN/AFINN-111.txt"; 
	
	public AFINN() {
		super(false, filePath);

		loadFromFile();
	}
	
	@Override
	public double fetchPolarity(String word, String[] options) {
		double returnValue=0;
		
		if(neuMap.containsKey(word)) {
			returnValue = neuMap.get(word);
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
				neuMap.put(split[0], Double.valueOf(split[1]));
			}
		}
		
		myLog.log("Finished loading a total of " + neuMap.size() + " entries from '" + getFilePath() + "'.");
	}
}
