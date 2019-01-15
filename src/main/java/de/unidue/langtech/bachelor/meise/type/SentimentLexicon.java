package de.unidue.langtech.bachelor.meise.type;

import java.util.HashMap;

import de.unidue.langtech.bachelor.meise.extra.ConsoleLog;
import de.unidue.langtech.bachelor.meise.files.FileUtils;

public abstract class SentimentLexicon {
	public FileUtils fu;
	public ConsoleLog myLog;
	private String filePath;
	public boolean onlyWorksOnLemmas;
	
    public HashMap<String, Double> posMap; //all positive instances
    public HashMap<String, Double> negMap; //all negative instances
    public HashMap<String, Double> neuMap; //total instance count
	
	public SentimentLexicon(boolean onlyWorksOnLemmas) {
		fu = new FileUtils();
		myLog = new ConsoleLog();
		
		posMap = new HashMap<String, Double>();
		negMap = new HashMap<String, Double>();
		neuMap = new HashMap<String, Double>();
		
		this.onlyWorksOnLemmas=onlyWorksOnLemmas;
	}
	
	public String getFilePath() {
		return this.filePath;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public SentimentLexicon(boolean onlyWorksOnLemmas, String filePath) {
		this(onlyWorksOnLemmas);
		setFilePath(filePath);
	}
	
	public abstract double fetchPolarity(String word, String[] options);
	
	public abstract void loadFromFile();
}
