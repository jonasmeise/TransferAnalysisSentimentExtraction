package de.unidue.langtech.bachelor.meise.files;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.unidue.langtech.bachelor.meise.extra.ConsoleLog;
import de.unidue.langtech.bachelor.meise.type.ReviewData;

public class RawJsonReviewReader extends JCasResourceCollectionReader_ImplBase{

	public static final String PARAM_FOLDER_PATH = "folderPath";
    @ConfigurationParameter(name = PARAM_FOLDER_PATH, mandatory = true)
    public String folderPath;
    
    public static final String PARAM_FILE_EXTENSION = "fileExtension";
    @ConfigurationParameter(name = PARAM_FILE_EXTENSION, mandatory = true)
    public String fileExtension;
    
    public static final String PARAM_SEARCH_SUBFOLDERS = "paramIncludeSubfolders";
    @ConfigurationParameter(name = PARAM_SEARCH_SUBFOLDERS, mandatory = false)
    private String paramIncludeSubfolders;
    public boolean includeSubfolders;
    
    public static final String PARAM_USE_OLD_DATA = "paramUseOldData";
    @ConfigurationParameter(name = PARAM_USE_OLD_DATA, mandatory = false)
    private String paramUseOldData;
    public boolean useOldData;
    
    public static final String PARAM_MAX_DATA_SIZE = "paramMaxDataSize";
    @ConfigurationParameter(name = PARAM_MAX_DATA_SIZE, mandatory = false)
    private String paramMaxDataSize;
    public int maxDataSize;
	
	private ArrayList<String> sourceFiles;
	private ArrayList<ReviewData> reviewList;
	private DataParser dp;
	private ConsoleLog myLog;
	private FileUtils fu;
	int i; 
	@Override
	public void getNext(JCas jCas) throws IOException, CollectionException {
		jCas.setDocumentLanguage(PARAM_LANGUAGE);
		
		if(!useOldData) {
			jCas.setDocumentText(reviewList.get(i).getContent());
		} else {
			jCas.setDocumentText(reviewList.get(i).getTextContent());
			//myLog.log(jCas.getDocumentText());
		}
		i++;
	}
	
	@Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);
        
		if(paramUseOldData==null || paramUseOldData.length()==0) {
        	useOldData = true;
        } else {
        	useOldData = Boolean.valueOf(paramUseOldData);
        }
           
        if(paramMaxDataSize==null) {
        	maxDataSize = -1;
        } else {
        	maxDataSize = Integer.valueOf(paramMaxDataSize);
        }
        
        external_Initialize(maxDataSize, useOldData);
        
    }

	private void initFiles(int maxDataSize, boolean useOldData) {
		sourceFiles = fu.getFilesInFolder(folderPath, fileExtension, includeSubfolders);
		
		myLog.log("Found " + sourceFiles.size() + " source files.");
		
		for(String singleSourceFile : sourceFiles) {
			reviewList.addAll(dp.parseXMLToReviewData(singleSourceFile, maxDataSize, useOldData));
		}
		
		myLog.log("Added a total of " + reviewList.size() + " sentences.");
	}
	
	public void external_Initialize(int maxDataSize, boolean useOldData) {
		if(paramIncludeSubfolders==null || paramIncludeSubfolders.length()==0) {
        	includeSubfolders = true;
        } else {
        	includeSubfolders = Boolean.valueOf(paramIncludeSubfolders);
        }
		     
        myLog = new ConsoleLog();
        dp = new DataParser();
        fu = new FileUtils();
        reviewList = new ArrayList<ReviewData>();
        
        initFiles(maxDataSize, useOldData);
	}
	
	public void external_Initialize(int maxDataSize) {
		external_Initialize(maxDataSize, useOldData);
	}
	
	public int getReviewId(String textContent) {
		for(ReviewData singleReview : reviewList) {
			if(singleReview.getTextContent().contains(textContent)) {
				return singleReview.getId();
			}
		}
		
		return -1;
	}
	
	public double getScore(int id) {
		double returnValue=0;
		
		returnValue = reviewList.get(id).getScore();
		
		return returnValue;
	}
	
	public ArrayList<ReviewData> getReviewList() {
		return reviewList;
	}
	
	public String getText(int id) {
		String returnValue="";
		
		for(String contentLine : reviewList.get(id).getText()) {
			returnValue += contentLine + " ";
		}
		
		return returnValue;
	}
	
	public ReviewData getReview(int id) {
		return reviewList.get(id);
	}
	
	public String getTitle(int id) {
		String returnValue="";
		
		returnValue = reviewList.get(id).getTitle();
		
		return returnValue;
	}
	
	public Progress[] getProgress()
    {
        return null;
    }
	
	public boolean hasNext() {
		return (i < reviewList.size());
	}
}