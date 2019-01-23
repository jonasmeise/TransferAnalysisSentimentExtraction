package de.unidue.langtech.bachelor.meise.classifier;

import java.io.File;
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
import de.unidue.langtech.bachelor.meise.type.ArffGenerator;
import de.unidue.langtech.bachelor.meise.type.classifiers.TestClassifierGenerator;
import weka.classifiers.Evaluation;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class ClassifierHandler extends JCasAnnotator_ImplBase{
	AspectClassifier aspectClassifier;
	ArffGenerator arffGenerator;
	FileUtils fu;
	ConsoleLog myLog;
	Instances instanceScheme;
	
	//Value of minAcceptanceValue
	public static final String PARAM_IGNORE_FEATURES = "ignoreFeaturesAt";
    @ConfigurationParameter(name = PARAM_IGNORE_FEATURES, mandatory = false)
    private String ignoreFeaturesAt;
	private int[] ignoreFeatures;
	
	//Value of minAcceptanceValue
		public static final String PARAM_ACCEPTANCE_VALUE = "featureMinAcceptanceValue";
	    @ConfigurationParameter(name = PARAM_ACCEPTANCE_VALUE, mandatory = false)
	    private String featureMinAcceptanceValue = "0";
		private double minAcceptanceValue;
	
	//Path of the .model-Path
		public static final String PARAM_MODEL_FILE = "modelFileInput";
	    @ConfigurationParameter(name = PARAM_MODEL_FILE, mandatory = false)
		private String modelFileInput;
	    private Collection<String> modelFileInputFolder;
	
    //Path of the .arff-Path
  		public static final String PARAM_ARFF_FILE = "arffFileInput";
  	    @ConfigurationParameter(name = PARAM_ARFF_FILE, mandatory = true)
  		private String arffFileInput;
  	    private ArrayList<String> arffFileInputs;
  	    
  	  public static final String PARAM_KERNEL_TYPE = "paramKernelType";
	    @ConfigurationParameter(name = PARAM_KERNEL_TYPE, mandatory = false)
		private String paramKernelType;
	    private int kernelType;
	    
	    public static final String PARAM_SVM_TYPE = "paramSVMType";
  	    @ConfigurationParameter(name = PARAM_SVM_TYPE, mandatory = false)
  		private String paramSVMType;
  	    private int svmType;
  	    
  	//Argument of NUM_FOLDS
    //WARNING: DOES NOT ACTUALLY PROCESS ANYTHING. ONLY WORKS BASED ON ALREADY EXISTING ARFFS
  		public static final String PARAM_NUM_FOLDS = "foldNum";
  	    @ConfigurationParameter(name = PARAM_ARFF_FILE, mandatory = true)
  		private String numFold;
  	    private int numFolds;
  	    private boolean foldAnalysisEnabled=false;
  	    
   //Path of the .arff-Path
  		public static final String PARAM_ANALYIS_OUTPUT_PATH = "outputAnalysisPath";
  	    @ConfigurationParameter(name = PARAM_ANALYIS_OUTPUT_PATH, mandatory = true)
  		private String outputAnalysisPath;
  	    
  	    public int classAttributeAt=1;
  	    private String allData;
  	
	public void loadModels(String inputFile) throws Exception {
		aspectClassifier.loadModels(inputFile);
		myLog.log("Loaded model from '" + inputFile + "'.");
	}
	
	 public void initialize(UimaContext aContext) throws ResourceInitializationException {
	    	super.initialize(aContext);
	    	fu = new FileUtils();
	    	myLog = new ConsoleLog();
	    	allData = "";
	    	arffFileInputs = fu.getFilesInFolder(arffFileInput, ".arff", false);
	    		    	
	    	minAcceptanceValue = Double.valueOf(featureMinAcceptanceValue);
	    	
	    	if(paramKernelType!=null) {
	    		kernelType = Integer.valueOf(paramKernelType);
	    	} else {
	    		kernelType = 0;
	    	}
	    	
	    	if(paramSVMType!=null) {
	    		svmType = Integer.valueOf(paramSVMType);
	    	} else {
	    		svmType = 0;
	    	}
	    	
	    	if(numFold!=null) {
	    		numFolds = Integer.valueOf(numFold);
	    		foldAnalysisEnabled=true;
	    		
	    		generateFoldsAndLearn(fu.getFilesInFolder(arffFileInput,".arff",false), numFolds, classAttributeAt, kernelType, svmType, outputAnalysisPath);
	    	} else {
		    	if(ignoreFeaturesAt!=null) {
		    		String[] split=ignoreFeaturesAt.split(" ");
		    		int i=0;
		    		ignoreFeatures = new int[split.length];
		    		
		    		for(String singleSplit : split) {
		    			ignoreFeatures[i] = Integer.valueOf(singleSplit);
		    			i++;
		    		}    		
		    	} else {
		    		ignoreFeatures = new int[0];
		    	}
			    	
		    	if(new File(arffFileInput).isDirectory()) {
		    		if(fu.getFilesInFolder(modelFileInput, ".model", false).size()==0) {
			    		//generate .model files first for each .arff file in arffFileInputs
			    		arffFileInputs.addAll(fu.getFilesInFolder(arffFileInput, ".arff", true));
			    		
			    		for(String singleArffFile : arffFileInputs ) {
			    			aspectClassifier = new AspectClassifier(); 
			    			
			    			try {
								aspectClassifier.learnAndExport(singleArffFile, singleArffFile + ".model");
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			    			
			    			myLog.log("Learned model for '" + singleArffFile + "'. ");
			    		}		
			    		
		    			modelFileInput = arffFileInput;
		    		}	
		    	} else {
		    		arffFileInputs.add(arffFileInput);
		    	}
		    	
		    	modelFileInputFolder = new ArrayList<String>();
		    	if(modelFileInput==null) {
		    		myLog.log("FATAL ERROR: Couldn't find models to learn from! Please set the according parameter correctly.");
		    	}
		    	else if(new File(modelFileInput).isDirectory()) {
		    		modelFileInputFolder.addAll(fu.getFilesInFolder(modelFileInput, ".model", false));
		    	} else {
		    		modelFileInputFolder.add(modelFileInput);
		    	}
		    	
		    	myLog.log("Loaded [" + modelFileInputFolder.size() + "] models for learning.");
		    	
		    	arffGenerator = new TestClassifierGenerator();
		    	arffGenerator.setLearningModeActivated(true);
		    	arffGenerator.generateRelations();
	
				aspectClassifier = new AspectClassifier();
				
				try {
					instanceScheme = aspectClassifier.getData(arffFileInputs.get(0), 1);
					instanceScheme.deleteAttributeAt(0); //TODO: MAKE IT WORK AAAAAAAAAH
				} catch (Exception e) {
					e.printStackTrace();
				}
	    	}
	    }
	
	 public void generateFoldsAndLearn(Collection<String> arffFileInputs, int numFolds, int classAttributeAt, int kernelType, int svmType, String outputPath) {
		 ArrayList<String> analysisString = new ArrayList<String>();
		 boolean manuallyOutputData = false;
		 if(myLog==null && allData==null && fu==null) {
			 //this class is called from outside....
			 myLog = new ConsoleLog(); 
			 fu = new FileUtils();
			 allData="";
			 manuallyOutputData=true;
			 
			 myLog.log("This class is called from outside. Manually setting attributes...");
		 }
		 
		 myLog.log("WARNING: This current method will not generate any data, but only will analyse already existing .arff files with crossfold validation");	 
		 
		for(String arffFileInput : arffFileInputs) {
			try {
				//if learning for folds: continue from her
				AspectClassifier foldClassifier = new AspectClassifier();
				Instances data = foldClassifier.getData(arffFileInput, classAttributeAt);
				System.out.println(classAttributeAt);
				
				foldClassifier.folds = numFolds;
				ArrayList<Evaluation> allEvaluations = foldClassifier.learn(data);
				
				double precision=0, recall=0, fMeasure=0, accuracy=0;
						
				for(Evaluation singleEval : allEvaluations) {
					precision += singleEval.precision(0);
					recall += singleEval.recall(0);
					fMeasure += singleEval.fMeasure(0);
					accuracy += singleEval.pctCorrect();
				}
					
				System.out.println("Precision\t" + precision/numFolds);
				System.out.println("Recall\t" + recall/numFolds);
				System.out.println("fMeasure" + fMeasure/numFolds);
				System.out.println("Accuracy\t" + accuracy/numFolds);
				
				analysisString.add(foldClassifier.sourcePath);
				analysisString.add("Precision\t" + precision/numFolds);
				analysisString.add("Recall\t" + recall/numFolds);
				analysisString.add("fMeasure" + fMeasure/numFolds);
				analysisString.add("Accuracy\t" + accuracy/numFolds);
				analysisString.add("");
				myLog.log("Completed " + numFolds +"-folded learning for '" + arffFileInput + "'.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		for(String line : analysisString) {
			allData = allData + line + "\n";
		}
		
		if(manuallyOutputData) {
			try {
				fu.createWriter(outputPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			fu.write(allData);
			fu.close();
		}
	 }
	 
	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
		if(!foldAnalysisEnabled) {
			ArrayList<AspectClassifier> aspectClassifierList = new ArrayList<AspectClassifier>();
			
			for(String singleModelFile : modelFileInputFolder) {
				try {
					AspectClassifier newAspectClassifier = new AspectClassifier(kernelType, svmType);
					newAspectClassifier.loadModels(singleModelFile);
					aspectClassifierList.add(newAspectClassifier);
					
					myLog.log("Loaded model '" + singleModelFile + "' for classification purpose.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
				
				
			Collection<Sentence> sentences = JCasUtil.select(arg0, Sentence.class);	
			
			for(Sentence sentence : sentences) {
				Collection<ArrayList<String>> dataLines = arffGenerator.generateFeaturesFromCas(sentence);
				for(ArrayList<String> singleLineFeatures : dataLines) {
					Instance newInstance = new DenseInstance(instanceScheme.numAttributes());
					int position=0, type;
	
					//TODO: CHeck if that always works...
					if(ignoreFeatures.length>0) {
						for(int ignoreAt : ignoreFeatures) {
							singleLineFeatures.remove(ignoreAt);
						}
					}
					
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
				   
				   ArrayList<String> predictionValues = new ArrayList<String>();
				   predictionValues.add("" + newInstance);
				   
				   for(AspectClassifier currentClassifier : aspectClassifierList) {
					   try {
						clsLabel = currentClassifier.getClassifier().classifyInstance(newInstance);
						newInstance.setClassValue(clsLabel);
						   //myLog.log("Found: " + newInstance.toString(newInstance.classAttribute()));
						   
						   double[] prediction=currentClassifier.getClassifier().distributionForInstance(newInstance);
		
					       //output predictions
						       for(int i=0; i<prediction.length; i=i+1)
						       {
						    	   if(prediction[i]>minAcceptanceValue) {
						    		   predictionValues.add(currentClassifier.sourcePath + "\t" + newInstance.classAttribute().value(i) + "\t" + Double.toString(prediction[i]));
						    		   //myLog.log(Double.toString(prediction[i]) + "@" + newInstance.classAttribute().value(i) + "- Probability of class " + currentClassifier.sourcePath + "@");
						    	   }
						       }
						} catch (Exception e) {
							e.printStackTrace();
						}
				   }
				   
				   processData(predictionValues);
				}
			}
		}
	}
	
	public void processData(ArrayList<String> something) {
		allData = allData + "#text=" + something.get(0) + "\n";
		
		for(int i=1;i<something.size();i++) {
			allData = allData + something.get(i) + "\n";
		}
		
		allData = allData + "\n";
	}

	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		if(!foldAnalysisEnabled) {
			try{
				fu.createWriter(outputAnalysisPath);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			fu.write(allData);
			
			fu.close();
		}
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
