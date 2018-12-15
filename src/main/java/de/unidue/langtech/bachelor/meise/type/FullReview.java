package de.unidue.langtech.bachelor.meise.type;

import java.util.ArrayList;

public class FullReview {
//a class which handles all data within a single Review;
//ReviewData as base source, POS, Dependency, Lemmas,
	
	private int id;
	private ReviewData rawReview;
	private ArrayList<Token> tokens;
	//Token, Integer, String refers to: 
	//Token = the token itself, Integer = position of reference token (can fetch if necessary), String = description/sentiment type
	private ArrayList<DoubleLink<Token, Integer, String>> dependencies;
	private ArrayList<DoubleLink<Token, Integer, String>> sentiments;
	
	//true: A->B positiv, false:A->B negativ
	//usually: ObjectA is Aspect, ObjectB is AspectRating (reverse within a negated sentence)
	private ArrayList<DoubleLink<Token, Token, Boolean>> aspectRatingLinks; 
	
	public FullReview() {
		//prevents Null-access errors
		rawReview = new ReviewData();
		dependencies = new ArrayList<DoubleLink<Token, Integer, String>>();
		sentiments = new ArrayList<DoubleLink<Token, Integer, String>>();
		aspectRatingLinks = new ArrayList<DoubleLink<Token, Token, Boolean>>();
		tokens = new ArrayList<Token>();
	}
	
	public Token fetchToken(int pos) {
		if((pos-1)>=0 && (pos-1)<getTokens().size()) {
			return getTokens().get(pos-1);
		} else {
			System.out.println("Can't fetch Token Pos@" + pos + ": Only " + getTokens().size() + " Tokens available!");
			return null;
		}
	}
	
	public FullReview(int id) {
		this();
		this.id = id+1;
		id++;
	}

	public int getId() {
		return id;
	}
	public ArrayList<Token> getTokens() {
		return tokens;
	}
	public ReviewData getRawReview() {
		return rawReview;
	}
	public void setRawReview(ReviewData rawReview) {
		this.rawReview = rawReview;
	}
	public ArrayList<DoubleLink<Token, Integer, String>> getDependencies() {
		return dependencies;
	}
	public void setDependencies(ArrayList<DoubleLink<Token, Integer, String>> dependencies) {
		this.dependencies = dependencies;
	}
	public ArrayList<DoubleLink<Token, Token, Boolean>> getAspectRatingLinks() {
		return aspectRatingLinks;
	}
	public void setAspectRatingLinks(ArrayList<DoubleLink<Token, Token, Boolean>> aspectRatingLinks) {
		this.aspectRatingLinks = aspectRatingLinks;
	}
	public ArrayList<DoubleLink<Token, Integer, String>> getSentiments() {
		return sentiments;
	}
	public void setSentiments(ArrayList<DoubleLink<Token, Integer, String>> sentiments) {
		this.sentiments = sentiments;
	}
}
