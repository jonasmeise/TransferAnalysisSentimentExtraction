package de.unidue.langtech.bachelor.meise.type;

import java.util.ArrayList;

public class ReviewData{
	String userName;
	double score;
	String date;
	String title;
	ArrayList<String> text;
	int id;
	private ArrayList<String> opinionTargets;
	private ArrayList<String> opinionCategory;
	private ArrayList<String> opinionPolarity;
	
	public ArrayList<String> getOpinionTargets() {
		return opinionTargets;
	}

	public void setOpinionTargets(ArrayList<String> opinionTargets) {
		this.opinionTargets = opinionTargets;
	}

	public ArrayList<String> getOpinionCategory() {
		return opinionCategory;
	}

	public void setOpinionCategory(ArrayList<String> opinionCategory) {
		this.opinionCategory = opinionCategory;
	}

	public ArrayList<String> getOpinionPolarity() {
		return opinionPolarity;
	}

	public void setOpinionPolarity(ArrayList<String> opinionPolarity) {
		this.opinionPolarity = opinionPolarity;
	}

	public ArrayList<String> getContentAsArrayList() {
		ArrayList<String> returnList = new ArrayList<String>();
		returnList.add(getTitle());
		returnList.addAll(getText());
		
		return returnList;
	}
	
	public ReviewData() {
		init();
	}
	
	private void init() {
		userName = "";
		score = 0;
		date = "";
		title = "";
		text = new ArrayList<String>();
		opinionTargets = new ArrayList<String>();
		opinionCategory = new ArrayList<String>();
		opinionPolarity = new ArrayList<String>();
	}

	public String getContent() {
		String text = getTitle();
		for(String line : getText()) {
			if(text.endsWith(".") || text.endsWith("!") || text.endsWith("?")) {
				text += " " + line;
			} else {
				text += ". " + line;
			}
		}
		
		return text;
	}
	
	public String getTextContent() {
		String text = getText().get(0);
		for(int i=1;i<getText().size();i++) {
			String line = getText().get(i);
			if(text.endsWith(".") || text.endsWith("!") || text.endsWith("?")) {
				text += " " + line;
			} else {
				text += ". " + line;
			}
		}
		
		return text;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public ArrayList<String> getText() {
		return text;
	}
	public void addText(String text) {
		this.text.add(text);
	}
	
	public String toXML() {
		String returnString = "";
		
		returnString += "<data>\n";
		
		returnString += "\t<userName>";
		returnString += getUserName();
		returnString += "</userName>\n";
		
		returnString += "\t<score>";
		returnString += getScore();
		returnString += "</score>\n";
		
		returnString += "\t<date>";
		returnString += getDate();
		returnString += "</date>\n";
		
		returnString += "\t<title>";
		returnString += getTitle();
		returnString += "</title>\n";
		
		returnString += "\t<text>\n";
		
		for(String text : getText()) {
			returnString  += "\t\t<sentence>" + text + "</sentence>\n";
		}
		
		returnString += "\t</text>\n";
		
		returnString += "</data>";
		
		return returnString;
	}
}
