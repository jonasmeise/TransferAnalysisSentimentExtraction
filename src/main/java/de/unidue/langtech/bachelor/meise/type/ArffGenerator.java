package de.unidue.langtech.bachelor.meise.type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.unidue.langtech.bachelor.meise.extra.ConsoleLog;
import de.unidue.langtech.bachelor.meise.files.FileUtils;

public abstract class ArffGenerator extends JCasAnnotator_ImplBase{
	
	public static final String PARAM_OUTPUT_PATH = "outputPath";
    @ConfigurationParameter(name = PARAM_OUTPUT_PATH, mandatory = true)
    private String outputPath;
    private FileUtils fu;
    public int cutoff, id=0;
    private ConsoleLog myLog;
	
	//Header for .arff file, 
	public static final String PARAM_RELATION_NAME = "relationName";
    @ConfigurationParameter(name = PARAM_RELATION_NAME, mandatory = true)
	public String relationName;
	
	//every data-entry refers to a single line
	public ArrayList<String> relations;
	public ArrayList<ArrayList<String>> data; //matrix of Sentence/relation
	
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
    	super.initialize(aContext);
    	relationName = "";
    	fu = new FileUtils();
    	myLog = new ConsoleLog();
    	data = new ArrayList<ArrayList<String>>();
    	
		relations = generateRelations();
    	
    	try {
			fu.createWriter(outputPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
    public String generateTupel(String[] tupel) {
    	String returnString;
    	if(tupel.length > 0) {
    		returnString = "{" + tupel[0];
    		
    		for(int i=1;i<tupel.length;i++) {
    			returnString = returnString + ", " + tupel[i];
    		}
    		
    		returnString = returnString + "}";
    	} else {
    		returnString = "{}";
    	}
    	
    	return returnString;
    }
    
    public void writeOutput() {
    	String completeOutput;
    	if(fu.fileWriter!=null) {	
    		completeOutput = "@relation " + relationName + "\n\n";
    		 
    		//write all relational attributes
    		for(String relation : relations) {
    			completeOutput = completeOutput + "@attribute " + relation + "\n";
    		}
    		
    		completeOutput = completeOutput + "\n@data\n";
    		
    		for(ArrayList<String> dataLine : data) {
    			if(dataLine.size() != relations.size()) {
    				myLog.log("Argument mismatch: " + relations.size() + " arguments expected, " + dataLine.size() + " arguments found in + " + dataLine + "!");    				
    				break;
    			} else {
	    			for(String singleData : dataLine) {
	    				completeOutput = completeOutput + singleData + ",";
	    			}
	    			
	    			//TODO: check if -1 wegmachen, falls zuviel weggeccuttet wird....
	    			completeOutput = completeOutput.substring(0,completeOutput.length()-1) + "\n";
    			}
    		}
    		
    		fu.write(completeOutput);
    	} else {
    		myLog.log("Writer isn't available!");
    	}
    	
    	if (fu.fileWriter != null) {
    		fu.close();
    	}
    }
    
    public abstract ArrayList<String> generateRelations();
    
    public abstract ArrayList<ArrayList<String>> generateData(JCas arg0);
    
	@Override
	public abstract void process(JCas arg0) throws AnalysisEngineProcessException;	
	
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		myLog.log("Finished! Total of " + data.size() + " entries added.");
		writeOutput();
	}
}
