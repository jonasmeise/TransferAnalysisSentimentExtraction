package de.unidue.langtech.bachelor.meise.type;

import java.util.ArrayList;
import java.util.HashMap;

import de.unidue.langtech.bachelor.meise.files.FileUtils;
import weka.core.stopwords.StopwordsHandler;

public class StopwordHandler implements StopwordsHandler{
	HashMap<String, Boolean> myHash = new HashMap<String, Boolean>();
	FileUtils fu;
	
	public StopwordHandler(String filePath) {
		fu = new FileUtils();
		ArrayList<String> allStopwords = fu.readFromFileArrayList(filePath);
		fu.close();
		
		for(String stopword : allStopwords) {
			myHash.put(stopword, true);
		}
	}
	
	public boolean isStopword(String arg0) {
		// TODO Auto-generated method stub
		if(arg0!=null) {
			return myHash.containsKey(arg0);
		} else {
			return false;
		}
	}

}
