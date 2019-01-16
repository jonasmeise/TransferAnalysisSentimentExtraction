package de.unidue.langtech.bachelor.meise.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import de.unidue.langtech.bachelor.meise.extra.ConsoleLog;

public class FileUtils {
	private ConsoleLog myLog = new ConsoleLog(); //for logging purposes
	public FileWriter fileWriter;
	
	public ArrayList<String> getFilesInFolder(File folder, String fileType, boolean includeSubfolders) {
		ArrayList<String> returnList = new ArrayList<String>();
		
		
		if(folder!=null) {	
		    for (final File fileEntry : folder.listFiles()) {
		        if (fileEntry.isDirectory() && includeSubfolders) {
		            returnList.addAll(getFilesInFolder(fileEntry, fileType, true));
		        } else {
		        	if(fileType.isEmpty() || (!fileType.isEmpty() & fileEntry.getAbsolutePath().endsWith(fileType))) {
		        		returnList.add(fileEntry.getAbsolutePath());
		        		myLog.log("Added '" + fileEntry.getAbsolutePath() + "' to the list!");
		        	}
		        }
		    }
		}
	    
	    myLog.log("--------");
	    myLog.log("Found " + returnList.size() + " files in folder '" + folder.getPath() + "'.");
	    
		return returnList;
	}
	
	public ArrayList<String> getFilesInFolder(String folder, String fileType, boolean includeSubfolders) {
		ArrayList<String> returnList = new ArrayList<String>();
		
		if(folder!=null) {
			returnList.addAll(getFilesInFolder(new File(folder), fileType, includeSubfolders));
		}
		
		return returnList;
	}
	
	public ArrayList<String> readFromFileArrayList(String filePath) {
		ArrayList<String> list = new ArrayList<String>();
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),"UTF-8"));
			
		    String line;
		    while ((line = br.readLine()) != null) {
		    	//if(!line.isEmpty() && line!="") {
					list.add(line);
				//}
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	
	public ArrayList<String> readFromFileArrayList(File filePath) {
		return readFromFileArrayList(filePath.getAbsolutePath());
	}
	
	public String readFromFile(String filePath) {
		Path path = Paths.get(filePath);
		Charset charset = StandardCharsets.ISO_8859_1;

		String content = "";
		try {
			content = new String(Files.readAllBytes(path), charset);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}
	
	public String readFromFile(File filePath) {
		return readFromFile(filePath.getAbsolutePath());
	}
	
	public void close() {
		if(fileWriter!=null) {
			try {
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void write(String text) {
		if(fileWriter==null) {
			myLog.log("No writer open at the moment!");
		} else {
			try {
				fileWriter.write(text + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void createWriter(String path) throws IOException {
		myLog.log(path);
		File file = new File(path);
		fileWriter = new FileWriter(file, true);
	}
}
