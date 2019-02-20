package de.unidue.langtech.bachelor.meise.classifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import de.unidue.langtech.bachelor.meise.extra.ConsoleLog;
import de.unidue.langtech.bachelor.meise.files.FileUtils;
import de.unidue.langtech.bachelor.meise.type.StopwordHandler;
import weka.attributeSelection.ClassifierAttributeEval;
import weka.attributeSelection.ClassifierSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SGD;
import weka.classifiers.functions.SGDText;
import weka.classifiers.meta.CVParameterSelection;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.Stopwords;
import weka.core.stopwords.AbstractStopwords;
import weka.core.stopwords.StopwordsHandler;
import weka.core.tokenizers.Tokenizer;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToWordVector;
import libsvm.svm_parameter;

public class AspectClassifier {
	//a single Aspect Classifier for a set of Instances
	
	public String sourcePath;
	public FilteredClassifier classifier;
	public Instances instances;
	public Evaluation evaluation;
	private FileUtils fu;
	private ConsoleLog myLog;
	private int kernelType=0;
	private int svmType=0;
	public boolean idfTransformEnabled;
	public boolean regression=false;
	public Classifier outerParameterClassifier; //if called from outside, this thing needs to be set
	public boolean mapToHistogram = true;
	public double parameterHistogram = 0.8;
	
	public CVParameterSelection cps;
	
	int seed;
	int folds = 10;
	
	public AspectClassifier() {
		fu = new FileUtils();
		sourcePath = "";
		instances = null;
		classifier = new FilteredClassifier();
		myLog = new ConsoleLog();
	}
	
	public AspectClassifier(int kernelType, int svmType, Classifier outerParameterClassifier) {
		this();
		this.kernelType= kernelType;
		this.svmType = svmType;
		this.outerParameterClassifier = outerParameterClassifier;
	}
	
	public Classifier getClassifier() {
		return classifier;
	}

	public void setClassifier(FilteredClassifier classifier) {
		this.classifier = classifier;
	}

	public AspectClassifier(String sourcePath) {
		this();
		this.sourcePath = sourcePath;
	}
	
	//Trains Classifier for given training Data
	public Instances buildClassifier(Instances train) throws Exception{ 
		   // further processing, classification, etc.
			LibSVM svm = new LibSVM();
			
			List<Integer> stringIndices=new ArrayList<Integer>(); //random length
			for(int i=0;i<train.numAttributes();i++) {
				if(train.attribute(i).isString()) {
					//System.out.println("String found in '" +train.attribute(i).name() + "'.");
					//-1 because we remove the 0-index after all
					stringIndices.add(i-1);
				}
			}
			
			StringToWordVector s2wFilter = createS2WVector(convertIntegers(stringIndices));
			//s2wFilter.setDoNotOperateOnPerClassBasis(true);
			myLog.log(stringIndices + " Kernel:" + kernelType);
			
			svm.setKernelType(new SelectedTag(kernelType, LibSVM.TAGS_KERNELTYPE));
			svm.setSVMType(new SelectedTag(svmType, LibSVM.TAGS_SVMTYPE));
			svm.setProbabilityEstimates(true);
			svm.setNormalize(true);
			svm.setShrinking(true);
			
			//weights?
			double weightClassA=0;
			double weightClassB=0;
			double weightClassC=1;
			
			for(Instance singleInstance : train) {
				if(singleInstance.classValue()==0) {
					weightClassA++;
				} else if(singleInstance.classValue()==1){
					weightClassB++;
				} else {
					weightClassC++;
				}
			}
			
			if(!regression) {
				//svm.setWeights((weightClassB/weightClassC) + " " + (weightClassA/weightClassC));
				myLog.log("Weights set: " + svm.getWeights());
			}

			NominalToBinary ntb = new NominalToBinary();
			ntb.setAttributeIndices("first-last");
			ntb.setTransformAllValues(true);
			
			Remove removeFilter = new Remove();
			//remove ID-Feature
			removeFilter.setAttributeIndices("1");
				
			svm.setDegree(2);
			//for constrainedS1:
			//svm.setCost(200);
			//svm.setGamma(0.002);
			//svm.setEps(0.0005);
			
			//for unconstrainedS1:
			//svm.setCost(210);
			//svm.setGamma(0.0015);
			//svm.setEps(0.0001);
			
			//for constrainedS3:
			//svm.setCost(500);
			//svm.setGamma(0.001);
			//svm.setEps(0.00005);
			
			//for unconstrainedS3:
			//svm.setCost(500);
			//svm.setGamma(0.001);
			//svm.setEps(0.00005);
			
			
			cps = new CVParameterSelection();
			cps.setClassifier(svm);
			cps.setNumFolds(5);
			cps.setDebug(true);
			String[] params = new String[4];
			params[0] = "D 2 2 1";
			params[1] = "C 500 500 1";
			params[2] = "G 0.0005 0.0015 3";
			params[3] = "E 0.00005 0.0002 4";
			cps.setCVParameters(params);
				
			ClassifierAttributeEval cae = new ClassifierAttributeEval();
			cae.setClassifier(classifier);
			cae.setEvaluationMeasure(new SelectedTag(ClassifierSubsetEval.EVAL_FMEASURE, ClassifierSubsetEval.TAGS_EVALUATION));
			cae.setLeaveOneAttributeOut(true);
			cae.setFolds(5);
			
			SGD sgd = new SGD();
			sgd.setLossFunction(new SelectedTag(SGD.LOGLOSS, SGD.TAGS_SELECTION));
			//sgd.setLearningRate(0.41);
			//sgd.setEpsilon(0.40);
			//sgd.setDoNotCheckCapabilities(true);

			MultilayerPerceptron mp = new MultilayerPerceptron();
			mp.setHiddenLayers("64,16");
			mp.setAutoBuild(true);
			mp.setTrainingTime(2);
			
			MultiFilter mf = new MultiFilter();
			mf.setInputFormat(train);
			mf.setFilters(new Filter[] {s2wFilter});
			//TODO: CHANGE
			mf.setFilters(new Filter[] {removeFilter, s2wFilter});
			
			//classifier.setClassifier(svm);
			if(outerParameterClassifier!=null) {
				myLog.log("Found classifier from outside...");
				classifier.setClassifier(outerParameterClassifier);
				
				if(outerParameterClassifier.getClass().equals(CVParameterSelection.class)) {
					cps = (CVParameterSelection) outerParameterClassifier;
				}
			} else {
				classifier.setClassifier(svm);
			}
			
			classifier.setFilter(mf);
			classifier.buildClassifier(train);
			
			return train;
	}
	
	public ArrayList<Evaluation> learn(Instances train, Instances test) throws Exception {
		ArrayList<Evaluation> returnList = new ArrayList<Evaluation>();

		double avgMeanError=0, avgMeanErrorMapped=0, avgMeanErrorMappedCumulated=0;
			   
		   //test.deleteAttributeAt(0);
		   //build-Classifier loescht automatisch 0
		   buildClassifier(train);
	
		   Evaluation currentEvaluation;
		   currentEvaluation = evalModel(classifier, test, folds, new Random());
		   
		   if(regression) {
			   avgMeanErrorMapped=0;
			   
			   double minimalAcceptanceValue=0;
			   HashMap<Double, Integer> histogram = new HashMap<Double, Integer>();
			   double minimalValue=1000, maximalValue=0, spanValue=0;
			   if(mapToHistogram) {
				   //create histogram
				   for(Instance singleInstance : train) {   
					   if(singleInstance.classValue()>maximalValue) {
						   maximalValue = singleInstance.classValue();
					   }
					   if(singleInstance.classValue()<minimalValue) {
						   minimalValue = singleInstance.classValue();
					   }
					   
					   if(!histogram.containsKey(singleInstance.classValue())) {
						   histogram.put(singleInstance.classValue(),1);
					   } else {
						   histogram.put(singleInstance.classValue(), histogram.get(singleInstance.classValue())+1);
					   }
				   }
				   
				   //TODO: make the *10 dependant on the data set
				   spanValue = Math.abs(maximalValue - minimalValue)*10;
				   
				   myLog.log("span value: " + spanValue);
				   minimalAcceptanceValue = (train.size()/spanValue) * parameterHistogram;
				   
				   for(Double key : histogram.keySet()) {
					   myLog.log(key + "-" + histogram.get(key) + " ? " + minimalAcceptanceValue);
					   if(histogram.get(key) < minimalAcceptanceValue) {
						   histogram.put(key, 0);
					   }
				   }
			   }
			   
			   for(Instance testInstance : test) {
				   double initialScore = classifier.classifyInstance(testInstance);
				   double dblMappedScore = initialScore;
	
				   if(mapToHistogram) {		   
					   double minimalDistance = 1000;
					   
					   for(Double key : histogram.keySet()) {
						   double currentDistance = Math.abs(initialScore - key);
						   
						   if(currentDistance < minimalDistance && histogram.get(key)>0) {
							   minimalDistance = currentDistance;
							   dblMappedScore = key;
						   }
					   }
				   }
				   
				   double difference = Math.abs(testInstance.classValue()-dblMappedScore);
				   difference = difference * difference;
				   
				   System.out.println(testInstance.classValue() + "-(" + dblMappedScore + "<" + initialScore + ") --> " + difference);
				   avgMeanErrorMapped += difference;
				   
			   }
			   
			   System.out.println(classifier.toString());
			   
			   avgMeanErrorMappedCumulated += (avgMeanErrorMapped/test.size());
			   
			   avgMeanError += currentEvaluation.rootMeanSquaredError();
			   myLog.log("absolute squared error: " + currentEvaluation.rootMeanSquaredError());
		   } else {
			   System.out.println("Matrix: " + currentEvaluation.toMatrixString());
			   System.out.println(currentEvaluation.toSummaryString());
		   }
		   
		   returnList.add(currentEvaluation);
		   
		   /*if(cps!=null && cps.getBestClassifierOptions()!=null) {
			   myLog.log("Best found parameters for this fold: ");
			   for(String parameter : cps.getBestClassifierOptions()) {
				   myLog.log(parameter);
			   }
		   }*/
		 
		 if(regression) {
			 myLog.log("AVG. SQUARED MEAN ERROR: " + (avgMeanError/folds));
			 myLog.log("AVG. SQUARED MAPPED ERROR: " + (avgMeanErrorMappedCumulated/(folds)));
		 }
		 
		 return returnList;
	}
	
	public ArrayList<Evaluation> learn(Instances data) throws Exception {
		ArrayList<Evaluation> returnList = new ArrayList<Evaluation>();
		
		Random rand = new Random(seed);
		Instances scrambledData = new Instances(data);

		scrambledData.randomize(rand);
		
	    if (scrambledData.classAttribute().isNominal()) {
	    	myLog.log("Stratified data set.");
	    	scrambledData.stratify(folds);
	    }
	    
	    
		 for (int n = 0; n < folds; n++) {
			   Instances train = scrambledData.trainCV(folds, n, rand);
			   Instances test = scrambledData.testCV(folds, n);
			   myLog.log("Train size: " + train.size());
			   myLog.log("test size: " + test.size());
			   
			   returnList.addAll(learn(train, test));
		 }
				
		 return returnList;
	}
	
	//import Instances -> learn and export model
	public void learnAndExport(String inputPath, String outputPath) throws IOException, URISyntaxException, Exception {
		learnAndExport(getData(inputPath, 1), outputPath);
	}
	
	public void learnAndExport(Instances data, String outputPath) throws IOException, URISyntaxException, Exception {
		Instances newData = buildClassifier(data);
		saveModel(outputPath, classifier);
	}
	
	private StringToWordVector createS2WVector(int[] attributeArray) {
		StringToWordVector s2wFilter;
		s2wFilter = new StringToWordVector(); 
		s2wFilter.setAttributeIndices("first-last");
		s2wFilter.setIDFTransform(idfTransformEnabled);
		s2wFilter.setLowerCaseTokens(true);	
		s2wFilter.setAttributeNamePrefix("s2w");
		
		//s2wFilter.setOutputWordCounts(true);
		//keep word number instead of booleanic existence
		myLog.log("Found " + attributeArray.length + " string attributes.");
		
		return s2wFilter;
		
	}
	
   private Evaluation evalModel(Classifier classifier, Instances data, Integer numberIterations, Random randData ) throws Exception {
       Evaluation eval = new Evaluation(data);
       eval.crossValidateModel(classifier, data, numberIterations, randData);
       return eval;
   }
   
   public void loadModels(String filePath) throws Exception {
	   sourcePath = new File(filePath).getName();
	   classifier = (FilteredClassifier) weka.core.SerializationHelper.read(filePath);
   }
   
   public Instances getData( String folderName, String fileType, boolean includeSubfolders, Integer posClass) throws IOException, URISyntaxException {
	   Instances returnInstances = null;
	   
	   ArrayList<String> fileList  = fu.getFilesInFolder(folderName, fileType, includeSubfolders);
	   
	   for(String filePath : fileList) {
		   if(returnInstances==null) {
			   returnInstances = getData(filePath, posClass);
		   } else {
			   returnInstances.addAll(getData(filePath, posClass));
		   }
	   }
	   
	   return returnInstances;
   }
   
   public void saveModel(String fileName, FilteredClassifier classifier) {
		try {
           ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
           out.writeObject(classifier);
           out.close();
			myLog.log("Exported Model to:" + fileName);
       } 
		catch (Exception e) {
			System.out.println("Problem found when writing: " + fileName);
		}
	}
   
   public static int[] convertIntegers(List<Integer> integers)
   {
       int[] returnArray = new int[integers.size()];
       for (int i=0; i < returnArray.length; i++)
       {
    	   returnArray[i] = integers.get(i).intValue();
       }
       return returnArray;
   }
   
   public Instances getData(String fileName, Integer posClass ) throws IOException, URISyntaxException {
       File file = new File(fileName);
       sourcePath = file.getName();
       myLog.log("Loading instances from " + fileName + "...");
       BufferedReader inputReader = new BufferedReader(new FileReader(file));
       Instances data = new Instances(inputReader);
       data.setClassIndex(data.numAttributes() - posClass);
       myLog.log("Found class attribute at '" + (data.numAttributes() - posClass) + "'.");

       return data;
   }
}
