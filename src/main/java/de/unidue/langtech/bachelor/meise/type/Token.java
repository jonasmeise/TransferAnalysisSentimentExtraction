package de.unidue.langtech.bachelor.meise.type;

public class Token {
	private String word;
	private int begin, end;
	private String pos;
	private String lemma;
	private int id;
	
	public Token() {
		setWord("");
		setBegin(0);
		setEnd(0);
		setPos("?");
		setLemma("");
		setId(0);
	}
	
	public Token(int id) {
		this();
		setId(id);
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public int getBegin() {
		return begin;
	}
	public void setBegin(int begin) {
		this.begin = begin;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	public String getPos() {
		return pos;
	}
	public void setPos(String pos) {
		this.pos = pos;
	}
	public String getLemma() {
		return lemma;
	}
	public void setLemma(String lemma) {
		this.lemma = lemma;
	}
}
