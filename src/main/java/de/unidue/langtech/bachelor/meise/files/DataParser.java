package de.unidue.langtech.bachelor.meise.files;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.unidue.langtech.bachelor.meise.extra.ConsoleLog;
import de.unidue.langtech.bachelor.meise.type.ReviewData;

public class DataParser {	
	//conversion of ReviewData->XML format and vice versa
	
	private String folderPath = "C:\\Users\\Jonas\\Documents\\Bachelorarbeit\\resources\\dataset2";
	private String outputPath = folderPath + "\\review_2_output.xml";
	private final ConsoleLog myLog;
	private final FileUtils fu;
	private int id = 0;
	
	public DataParser() {
		fu = new FileUtils();
		myLog = new ConsoleLog();
	}
	
	
	//fileType: .txt   Output: .xml
	public DataParser(String inputFolder, String outputFile) {
		this();
		folderPath = inputFolder;
		outputPath = outputFile;
	}
	
	//parses a "raw-text-file" with data into a
	//XML-based file system. Might need other parseData() instances and versions
	//if the syntax differs
	public void run(String fileType) {
		ArrayList<ReviewData> dataList = parseData(fu.getFilesInFolder(folderPath, fileType, true));
		
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
					
					if(allSourceData.get(i+1).split(" ").length >= 3) { //corner-case: Name & Location are set on the same line
						i--;
					}
					
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
	
	public ArrayList<ReviewData> parseXMLToReviewData(String filePath, int max, boolean oldData) { //check ReviewData.java for structure & elements
		return parseXMLToReviewData_raw(fu.readFromFileArrayList(filePath), max, oldData);
	}
	
	public ArrayList<ReviewData> parseXMLToReviewData(String filePath, boolean oldData) { //check ReviewData.java for structure & elements
		return parseXMLToReviewData_raw(fu.readFromFileArrayList(filePath), -1, oldData);
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
	
	public ArrayList<ReviewData> parseXMLToReviewData_raw(ArrayList<String> rawFile, int max, boolean oldData) { //check ReviewData.java for structure & elements
		ArrayList<ReviewData> returnList = new ArrayList<ReviewData>();
		int i=0;
		
		myLog.log("Parsing from file with " + rawFile.size() + " lines up to " + max + " data sets. OldData=" + oldData);
		
		while(i < rawFile.size()) {
			if(!oldData) {
				if(rawFile.get(i).contains("<data>")) {
					ReviewData newReviewData = new ReviewData();
					
					//parse a single review	
					newReviewData.setUserName(parseXML(rawFile.get(++i), "userName"));
					newReviewData.setScore(Double.valueOf(parseXML(rawFile.get(++i), "score")));
					newReviewData.setDate(parseXML(rawFile.get(++i), "date"));
					newReviewData.setTitle(parseXML(rawFile.get(++i), "title"));
					
					if(rawFile.get(++i).contains("<text>")) {
						while(!rawFile.get(++i).contains("</text>")) { //parse all sentences
							//newReviewData.addText(parseXML(rawFile.get(i), "sentence").substring(4)); //cuts away standard preset characters
							newReviewData.addText(parseXML(rawFile.get(i), "sentence"));
						}
					}
					
					if(returnList.size()<max || max==-1) {
						returnList.add(newReviewData);
					}
				} else {
					i++;
				}
			} else {
				if(rawFile.get(i).contains("<Review rid")) {
					String regexSplitValue = "(\".+?\")";
					//parse a single review	
					while(!rawFile.get(++i).contains("</Review>")) {
						if(rawFile.get(++i).contains("<sentence id")) {
							if(rawFile.get(++i).contains("<text>")) {
								ReviewData newReviewData = new ReviewData();
								
								newReviewData.setTitle("");
								newReviewData.setId(id+0);
								String textToAdd = parseXML(rawFile.get(i), "text").replaceAll("&apos;", "\'")
										.replaceAll("\\u2013", "")
										.replaceAll("(\\.)+", ".")
										.replaceAll("\\u2019", "'")
										.replaceAll("\\u2026", ".")
										.replaceAll("!\\)\\.", "!)")
										.replaceAll("\\u2018", "'");
								
								newReviewData.addText(textToAdd);
								
								myLog.log("Added '" + parseXML(rawFile.get(i), "text") + "'.");
								
								if(rawFile.get(++i).contains("<Opinions>")) {
									while(!rawFile.get(++i).contains("</Opinions>")) {
									    Pattern pattern = Pattern.compile(regexSplitValue);
								        Matcher matcher = pattern.matcher(rawFile.get(i));
								        
							        	if(matcher.find()) {
							        		String target = matcher.group(0);
							        		newReviewData.getOpinionTargets().add(target.substring(1, target.length()-1));
							        	}
							        	if(matcher.find()) {
							        		String category = matcher.group(0);
							        		newReviewData.getOpinionCategory().add(category.substring(1, category.length()-1));
							        	}
							        	if(matcher.find()) {
							        		String polarity = matcher.group(0);
							        		newReviewData.getOpinionPolarity().add(polarity.substring(1, polarity.length()-1));
							        	}
									}
		
									if(returnList.size()<max || max==-1) {
										returnList.add(newReviewData);
										id++;
									}
								}
							}
						}
					}
				} else {
					i++;
				}
			}
		}
		
		return returnList;
	}
}
