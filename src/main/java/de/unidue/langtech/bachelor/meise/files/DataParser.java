package de.unidue.langtech.bachelor.meise.files;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import de.unidue.langtech.bachelor.meise.extra.ConsoleLog;
import de.unidue.langtech.bachelor.meise.type.ReviewData;

public class DataParser {	
	//conversion of ReviewData->XML format and vice versa
	
	private String folderPath = "C:\\Users\\Jonas\\Documents\\Bachelorarbeit";
	private String outputPath = folderPath + "\\review_output.xml";
	private final ConsoleLog myLog;
	private final FileUtils fu;
	
	public DataParser() {
		fu = new FileUtils();
		myLog = new ConsoleLog();
	}
	
	public static void main(String[] args) {
		DataParser dataParser = new DataParser();
		dataParser.run();
	}
	
	
	//parses a "raw-text-file" with data into a
	//XML-based file system. Might need other parseData() instances and versions
	//if the syntax differs
	public void run() {
		ArrayList<ReviewData> dataList = parseData(fu.getFilesInFolder(folderPath,  ".txt", true));
		
		try {
			fu.createWriter(outputPath);
			
			for(ReviewData data : dataList) {
				myLog.log(data.toXML());
				fu.write(data.toXML() + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			fu.close();
		}
	}

	public ArrayList<ReviewData> parseData(String sourceFile) {
		ArrayList<String> sourceFileList = new ArrayList<String>();
		sourceFileList.add(sourceFile);
		
		return parseData(sourceFileList);
	}
	
	public ArrayList<ReviewData> parseData(ArrayList<String> allSourceFiles) {
		ArrayList<String> allSourceData = new ArrayList<String>();
		ArrayList<ReviewData> parsedData = new ArrayList<ReviewData>();
		
		for(String singleFilePath : allSourceFiles) {
			allSourceData.addAll(fu.readFromFileArrayList(singleFilePath));
		}
		
		//actual parsing process starts here
		//parse files (String-ArrayLists) into ReviewData-objects
		//compare the original source file for data structure references
		
		int i=0;
		String currentLine;
		
		while(i < allSourceData.size()) {
			currentLine = allSourceData.get(i);
			if(!currentLine.startsWith(";;;") && !currentLine.isEmpty()) { //comment or empty -> ignore
				if(currentLine.contains("user avatar image")) { //a review starts
					ReviewData newData = new ReviewData();
					newData.setUserName(allSourceData.get(i+1));
					newData.setScore(Double.valueOf(allSourceData.get(i+3)));
					newData.setDate(allSourceData.get(i+4).split("Reviewed:")[1]);
					newData.setTitle(allSourceData.get(i+5));
					
					//extract actual text
					i=i+6; //skip to the current location
					while(!allSourceData.get(i).contains("Stayed in") && !allSourceData.get(i).contains("user avatar image")) { 
						//we don't actually want to add blank lines, only continue reading
						if(!allSourceData.get(i).isEmpty() && allSourceData.get(i).length()>3) { 
							newData.addText(allSourceData.get(i));
						}
						i++; 
						//skips automatically to end of current data
						//and will continue parsing for new data afterwards 
					}
					parsedData.add(newData);
					i--; //so that we don't skip an important line while looking ahead
				}
			}
			i++;
		}
		
		return parsedData;
	}
	
	public ArrayList<ReviewData> parseXMLToReviewData(String filePath) { //check ReviewData.java for structure & elements
		return parseXMLToReviewData_raw(fu.readFromFileArrayList(filePath));
	}
	
	public String parseXML(String rawLine, String attribute) {
		String startMarker = "<" + attribute + ">";
		String endMarker = "</" + attribute + ">";
		
		if(isValidXML(rawLine, startMarker, endMarker)) {
			return rawLine.substring(rawLine.indexOf(startMarker) + startMarker.length(), rawLine.indexOf(endMarker)); 
		} else {
			myLog.log("Error: Unable to parse attribute '" + attribute + "' in '" + rawLine + "'.");
			return null;
		}
		
	}
	
	public boolean isValidXML(String rawLine, String startMarker, String endMarker) {
		return (rawLine.contains(startMarker) && rawLine.contains(endMarker));
	}
	
	public ArrayList<ReviewData> parseXMLToReviewData_raw(ArrayList<String> rawFile) { //check ReviewData.java for structure & elements
		ArrayList<ReviewData> returnList = new ArrayList<ReviewData>();
		int i=0;
		
		while(i < rawFile.size()) {
			if(rawFile.get(i).contains("<data>")) {
				ReviewData newReviewData = new ReviewData();
				
				//parse a single review	
				newReviewData.setUserName(parseXML(rawFile.get(++i), "userName"));
				newReviewData.setScore(Double.valueOf(parseXML(rawFile.get(++i), "score")));
				newReviewData.setDate(parseXML(rawFile.get(++i), "date"));
				newReviewData.setTitle(parseXML(rawFile.get(++i), "title"));
				
				if(rawFile.get(++i).contains("<text>")) {
					while(!rawFile.get(++i).contains("</text>")) { //parse all sentences
						newReviewData.addText(parseXML(rawFile.get(i), "sentence").substring(4)); //cuts away standard preset characters
					}
				}
				
				returnList.add(newReviewData);
			} else {
				i++;
			}
		}
		
		return returnList;
	}
}
