package de.unidue.langtech.bachelor.meise.classifier;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.unidue.langtech.bachelor.meise.extra.ConsoleLog;
import de.unidue.langtech.bachelor.meise.files.FileUtils;
import de.unidue.langtech.bachelor.meise.pipeline.TSVExporter;
import de.unidue.langtech.bachelor.meise.type.ArffGenerator;
import de.unidue.langtech.bachelor.meise.type.classifiers.TestClassifierGenerator;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class ClassifierHandler extends JCasAnnotator_ImplBase{
	AspectClassifier aspectClassifier;
	ArffGenerator arffGenerator;
	FileUtils fu;
	ConsoleLog myLog;
	ArrayList<ArrayList<String>> tsvData;
	Instances instanceScheme;
	TSVExporter tsvExporter;
	
	//Value of minAcceptanceValue
		public static final String PARAM_ACCEPTANCE_VALUE = "featureMinAcceptanceValue";
	    @ConfigurationParameter(name = PARAM_ACCEPTANCE_VALUE, mandatory = false)
	    private String featureMinAcceptanceValue = "0.02";
		private double minAcceptanceValue;
	
	//Path of the .model-Path
		public static final String PARAM_MODEL_FILE = "modelFileInput";
	    @ConfigurationParameter(name = PARAM_MODEL_FILE, mandatory = true)
		private String modelFileInput;
	
  //Path of the .model-Path
  		public static final String PARAM_ARFF_FILE = "arffFileInput";
  	    @ConfigurationParameter(name = PARAM_ARFF_FILE, mandatory = true)
  		private String arffFileInput;
  	
	    
	public ClassifierHandler() {

	}
	
	public void loadModels(String inputFile) throws Exception {
		aspectClassifier.loadModels(inputFile);
		myLog.log("Loaded model from '" + inputFile + "'.");
	}
	
	 public void initialize(UimaContext aContext) throws ResourceInitializationException {
	    	super.initialize(aContext);
	    	fu = new FileUtils();
	    	myLog = new ConsoleLog();
	    	tsvExporter = new TSVExporter();
			
	    	minAcceptanceValue = Double.valueOf(featureMinAcceptanceValue);
	    	arffGenerator = new TestClassifierGenerator();
	    	arffGenerator.setLearningModeActivated(true);
			tsvData = new ArrayList<ArrayList<String>>();
			aspectClassifier = new AspectClassifier();
			
			try {
				instanceScheme = aspectClassifier.getData(arffFileInput, 1);
				instanceScheme.deleteAttributeAt(0); //deletes ID
				loadModels(modelFileInput);
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }
	
	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
		Collection<Sentence> sentences = JCasUtil.select(arg0, Sentence.class);
		
		for(Sentence sentence : sentences) {
			Collection<ArrayList<String>> dataLines = arffGenerator.generateFeaturesFromCas(sentence);
			for(ArrayList<String> singleLineFeatures : dataLines) {
				Instance newInstance = new DenseInstance(instanceScheme.numAttributes());
				int position=0, type;
				
				singleLineFeatures.remove(0);
				
				for(String singleFeature : singleLineFeatures) {
					Object value = null;
					type = instanceScheme.attribute(position).type();

					if(type==2) {
						value = (String)singleFeature;
						newInstance.setValue(instanceScheme.attribute(position), (String)value);
					} else if(type==0) {
						value = Double.valueOf(singleFeature);
						newInstance.setValue(instanceScheme.attribute(position), (Double)value);
					}
					
					position++;
				}
				
				newInstance.setDataset(instanceScheme);
				myLog.log("Instance: " + newInstance);
				
			   double clsLabel;
			   
			   try {
				clsLabel = aspectClassifier.getClassifier().classifyInstance(newInstance);
				
				newInstance.setClassValue(clsLabel);
				   myLog.log("Found: " + newInstance.toString(newInstance.classAttribute()));
				   
				   double[] prediction=aspectClassifier.getClassifier().distributionForInstance(newInstance);

				   ArrayList<String> predictionValues = new ArrayList<String>();
			       //output predictions
			       for(int i=0; i<prediction.length; i=i+1)
			       {
			    	   if(prediction[i]>minAcceptanceValue) {
			    		   predictionValues.add(newInstance.classAttribute().value(i) + " " + Double.toString(prediction[i]));
			    		   myLog.log("Probability of class "+newInstance.classAttribute().value(i)+" : "+Double.toString(prediction[i]));
			    	   }
			       }
			       
	    		   ArrayList<String> newDataList = new ArrayList<String>();
	    		   newDataList.add(sentence.getCoveredText());
			       
	    		   for(int x=0;x<newInstance.numAttributes();x++) {
	    			   newDataList.add(newInstance.attribute(x).toString());
	    		   }
	    		   newDataList.addAll(predictionValues);
	    		   
	    		   //TODO: Fuck this
				   tsvData.add(newDataList);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void writeOutput() {
		
	}
	
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		myLog.log("Finished! Total of " + tsvData.size() + " entries added.");
		writeOutput();
	}

	public String getArffFileInput() {
		return arffFileInput;
	}


	public void setArffFileInput(String arffFileInput) {
		this.arffFileInput = arffFileInput;
	}


	public String getModelFileInput() {
		return modelFileInput;
	}


	public void setModelFileInput(String modelFileInput) {
		this.modelFileInput = modelFileInput;
	}


	public AspectClassifier getAspectClassifier() {
		return aspectClassifier;
	}


	public void setAspectClassifier(AspectClassifier aspectClassifier) {
		this.aspectClassifier = aspectClassifier;
	}

}
