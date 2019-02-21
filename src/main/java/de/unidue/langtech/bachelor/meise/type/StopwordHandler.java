package de.unidue.langtech.bachelor.meise.type;

import java.util.ArrayList;
import java.util.HashMap;

import de.unidue.langtech.bachelor.meise.files.FileUtils;
import weka.core.stopwords.StopwordsHandler;

public class StopwordHandler implements StopwordsHandler, java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4488466685733581117L;
	
	HashMap<String, Boolean> myHash = new HashMap<String, Boolean>();
	
	public StopwordHandler() {
		this("src\\main\\resources\\stopwords.txt");
	}
	
	public StopwordHandler(String filePath) {
		FileUtils fu = new FileUtils();
		ArrayList<String> allStopwords = fu.readFromFileArrayList("src\\main\\resources\\stopwords.txt");
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
