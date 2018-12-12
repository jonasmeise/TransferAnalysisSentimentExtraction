package de.unidue.langtech.bachelor.meise.pipeline;

import java.util.ArrayList;
import java.io.File;

import de.unidue.langtech.bachelor.meise.extra.ConsoleLog;
import de.unidue.langtech.bachelor.meise.files.FileUtils;
import de.unidue.langtech.bachelor.meise.type.DoubleLink;
import de.unidue.langtech.bachelor.meise.type.FullReview;
import de.unidue.langtech.bachelor.meise.type.Token;

//Class for import of .TSV annotated file into 'fullReview' data type.
//Functions as an intern representation of the annotated data and parses all necessary information to it
public class TSVImporter {
	
	private FileUtils fu;
	private ConsoleLog myLog;
	private int offsetRead = 8; //Beginning with line 8 in the .tsv document, the data starts
	private int columnCount = 12;
	
	private int idPosition = 0;
	private int beginEndPosition = 1;
	private int wordPosition = 2;
	private int posPosition = 3;
	private int aspectPosition = 4;
	private int selfSemPosition = 5;
	private int selfTagPosition = 6;
	private int dependencyPosition = 7;
	private int dependencyTokenPosition = 9;
	private int aspectSemPosition = 10;
	private int aspectRelationPosition = 11;
	private String blank = "_";
	private String line = "-";
	
	public TSVImporter() {
		fu = new FileUtils();
		myLog = new ConsoleLog();
	}
	
	public static void main(String[] args) {
		TSVImporter tsvImporter = new TSVImporter();
		tsvImporter.test();
	}
	
	public void test() {
		String filePath = "C:\\Users\\Jonas\\Documents\\Bachelorarbeit\\output_annotated.tsv";
		ArrayList<FullReview> fullReviews = importFromTsv(filePath);
		
		for(FullReview fullReview : fullReviews) {
			myLog.log(fullReview.getRawReview().getContent());
		}
	}
	
	public ArrayList<FullReview> importFromTsv(String filePath) {
		return importFromTsv(new File(filePath));
	}
	
	public ArrayList<FullReview> importFromTsv(File filePath) {
		int readerLinePos=offsetRead-1;
		int idCounter=0;
		
		ArrayList<FullReview> returnList = new ArrayList<FullReview>();
		ArrayList<String> rawFileContent = fu.readFromFileArrayList(filePath);
		
		myLog.log("Read '" + filePath.getAbsolutePath() + "', " + rawFileContent.size() + " lines read!");
		
		while(readerLinePos < rawFileContent.size()) {
			//find relevant lines first:
			ArrayList<String> singleReviewData = new ArrayList<String>();
			
			while(rawFileContent.get(readerLinePos).length()>3) { //random check: if nothing good written in there => ignore
				singleReviewData.add(rawFileContent.get(readerLinePos));
				readerLinePos++;
			}
			
			//since we've read all relevant lines, parsing is next;
			returnList.add(parseFullReviewData(singleReviewData, idCounter));
			readerLinePos++;
		}
		
		return returnList;
	}
	
	public FullReview parseFullReviewData(ArrayList<String> rawString, int idCounter) { //rawString = the entire chunk of raw data for a single review
		FullReview newReview = new FullReview(idCounter);
		ArrayList<String[]> allContent = new ArrayList<String[]>();
		
		if(!rawString.get(0).startsWith("#Text=")) {
			myLog.log("ERROR in '" + rawString + "': Can't read original text line! (has to start with #Text=)");
			return null; //basically error
		} 
		
		String[] titleAndText = rawString.remove(0).substring(6).split(". ");
		myLog.log("Found " + (titleAndText.length-1) + " different sentences in the text");
		newReview.getRawReview().setTitle(titleAndText[0]);
		
		for(int i=1;i<titleAndText.length;i++) {
			newReview.getRawReview().addText(titleAndText[i]);
		}
		
		for(String line : rawString) {
			allContent.add(line.split("\t"));
		}
		
		for(String line : rawString) {
			myLog.log(parseFullReviewContent(line, allContent, newReview));
		}
		
		return newReview;
	}
	
	public int parseFullReviewContent(String rawLine, ArrayList<String[]> complete, FullReview output) {
		int returnId; //for sanity-check
		String[] split = rawLine.split("\t");
		
		if(split.length != columnCount) {
			myLog.log("Can't parse '" + split + "', wrong amount of columns/data fields: " + split.length);
		} else {
			//structure:	id-n	begin-end	word	POS	Aspect	SelfTag SelfSem	Dependency	*	DepToken	AspectSem|...	AspectRelation|... 
			//Rating besitzt AspectSem/AspectRelation Wert
			
			//parse begin-end
			String id = split[idPosition];
			String beginEnd = split[beginEndPosition];
			
			returnId = getSplit(id, line, 0);
			
			Token token = new Token(getSplit(id, line, 1));
			token.setBegin(getSplit(beginEnd, line, 0));
			token.setEnd(getSplit(beginEnd, line, 1));
			
			String word = split[wordPosition];
			token.setWord(word);
			
			String pos = split[posPosition];
			token.setPos(pos);
			
			String aspect = split[aspectPosition];
			
			if(aspect.compareTo(blank)!=0) { //actually relevant position
				if(split[selfTagPosition].compareTo(split[idPosition])==0) { //sanity-check for actual self-reference
					DoubleLink<Token, Integer, String> selfDependency = new DoubleLink<Token, Integer, String>();
					selfDependency.setObjectA(token);
					selfDependency.setObjectB(token.getId());
					selfDependency.setDescription(split[selfSemPosition]);
					output.getSentiments().add(selfDependency);
				}
			}
			
			DoubleLink<Token, Integer, String> dependency = new DoubleLink<Token, Integer, String>();
			dependency.setObjectA(token);
			dependency.setObjectB(getSplit(split[dependencyTokenPosition],line,1));
			dependency.setDescription(split[dependencyPosition]);
			output.getDependencies().add(dependency);
			
			//check for all available s
			int multipleOccurencesCount = split[aspectSemPosition].split("|").length;
			for(int i=0;i<multipleOccurencesCount;i++) {
				DoubleLink<Token, Integer, String> newSentimentDependency = new DoubleLink<Token, Integer, String>();
				
				String currentSentiment = getSplitString(split[aspectSemPosition],"|",i); 
				
				if(currentSentiment.compareTo(blank)!=0) {
					int currentRelation = getSplit(split[aspectRelationPosition],"|",i);
					
					
					newSentimentDependency.setObjectA(token);
					newSentimentDependency.setObjectB(currentRelation);
					newSentimentDependency.setDescription(split[aspectPosition]);
					newSentimentDependency.setSentiment(currentSentiment);
					
					output.getSentiments().add(newSentimentDependency);	
				}
			}
			
			output.getTokens().add(token);
			
			return returnId;
		}
		
		return -1;
	}
	
	public int getSplit(String string, String splitSymbol, int number) {
		//check for validity beforehand or just BlessRNG
		return(Integer.parseInt(string.split(splitSymbol)[number]));
	}
	public String getSplitString(String string, String splitSymbol, int number) {
		//check for validity beforehand or just BlessRNG
		return(string.split(splitSymbol)[number]);
	}
}
