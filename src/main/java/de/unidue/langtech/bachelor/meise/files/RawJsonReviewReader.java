package de.unidue.langtech.bachelor.meise.files;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.unidue.langtech.bachelor.meise.extra.ConsoleLog;
import de.unidue.langtech.bachelor.meise.type.ReviewData;

public class RawJsonReviewReader extends JCasResourceCollectionReader_ImplBase{

	/*public static final String PARAM_SOURCE_LOCATION = "sourceFilePath";
    @ConfigurationParameter(name = PARAM_SOURCE_LOCATION, mandatory = true, defaultValue = "review_output.json")
    private String sourceLocation;*/
	
	private ArrayList<ReviewData> reviewList;
	private DataParser dp;
	private String filePath;
	private ConsoleLog myLog;
	int i; //i=reviewCounter
	
	@Override
	public void getNext(JCas jCas) throws IOException, CollectionException {
		jCas.setDocumentLanguage(PARAM_LANGUAGE);
		jCas.setDocumentText(reviewList.get(i).getContent());
		i++;
	}
	
	@Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);
        myLog = new ConsoleLog();
        dp = new DataParser();
        initFiles();
    }

	private void initFiles() {
		for (Resource r : getResources()) {
            try {
				filePath = r.getResource().getFile().getAbsolutePath();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		
		myLog.log("Found " + getResources().size() + " source files: " + filePath);
		
		reviewList = dp.parseXMLToReviewData(filePath);
	}
	
	public Progress[] getProgress()
    {
        return null;
    }
	
	public boolean hasNext() {
		return (i < reviewList.size());
	}
}