package de.unidue.langtech.bachelor.meise.classifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.uima.fit.factory.JCasFactory;

import de.unidue.langtech.bachelor.meise.extra.ConsoleLog;
import de.unidue.langtech.bachelor.meise.files.FileUtils;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.stemmers.LovinsStemmer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class AspectClassifier {
	//a single Aspect Classifier for a set of Instances
	
	public String sourcePath;
	private FilteredClassifier classifier;
	public Instances instances;
	public Evaluation evaluation;
	private FileUtils fu;
	private ConsoleLog myLog;
	
	String fileType = "data400.arff";
	int seed;
	int folds = 5;
	
	public AspectClassifier() {
		fu = new FileUtils();
		sourcePath = "";
		instances = null;
		classifier = new FilteredClassifier();
		myLog = new ConsoleLog();
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
	
	public static void main(String[] args) throws Exception {
		AspectClassifier ac = new AspectClassifier();
		ac.sourcePath = "C:\\Users\\Jonas\\Downloads\\de.unidue.langtech.bachelor.meise\\de.unidue.langtech.bachelor.meise\\src\\main\\resources";
		ac.run("C:\\Users\\\\Jonas\\Downloads\\de.unidue.langtech.bachelor.meise\\de.unidue.langtech.bachelor.meise\\src\\main\\resources\\data400.model");
	}
	
	
	//Trains Classifier for given training Data
	public Instances buildClassifier(Instances train) throws Exception{ 
		   // further processing, classification, etc.
			LibSVM svm = new LibSVM();
			
			List<Integer> stringIndices=new ArrayList<Integer>(); //random length
			for(int i=0;i<train.numAttributes();i++) {
				if(train.attribute(i).isString()) {
					System.out.println(train.attribute(i).name());
					stringIndices.add(i-1);
				}
			}
			
			Remove removeFilter = new Remove();
			//remove ID-Feature
			removeFilter.setAttributeIndicesArray(new int[]{0});
			
			Instances newData = null;
			try {
				removeFilter.setInputFormat(train);
				newData = Filter.useFilter(train, removeFilter);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			//remove the ID-Feature again

			StringToWordVector s2wFilter = createS2WVector(convertIntegers(stringIndices));

			svm.setKernelType(new SelectedTag(0, LibSVM.TAGS_KERNELTYPE));
			svm.setSVMType(new SelectedTag(0, LibSVM.TAGS_SVMTYPE));
			svm.setProbabilityEstimates(true);
			
			classifier.setFilter(s2wFilter); 
			classifier.setClassifier(svm);	
			
			return newData;
	}
	
	public void learn(Instances data) throws Exception {
		Random rand = new Random(seed);   // create seeded number generator
		 Instances randData = new Instances(data);   // create copy of original data
		 randData.randomize(rand);         // randomize data with number generator
		 
		 for (int n = 0; n < folds; n++) {
			   Instances train = randData.trainCV(folds, n, rand);
			   Instances test = randData.testCV(folds, n);

			   buildClassifier(train);
			   
			   evaluation = evalModel(classifier, test, folds, new Random());
				
			   System.out.println(evaluation.toSummaryString());
		 }
	}
	
	//import Instances -> learn and export model
	public void learnAndExport(String inputPath, String outputPath) throws IOException, URISyntaxException, Exception {
		learnAndExport(getData(inputPath, 1), outputPath);
	}
	
	public void learnAndExport(Instances data, String outputPath) throws IOException, URISyntaxException, Exception {
		Instances newData = buildClassifier(data);
		classifier.buildClassifier(newData);
		saveModel(outputPath, classifier);
	}
	
	public void run(String outputPath) throws Exception {
		try {
			instances = getData(sourcePath, fileType, true, 1);
			learnAndExport(instances, outputPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		loadModels(outputPath);
		
		Instances sourceInstances = getData(sourcePath, fileType, false, 1);
		sourceInstances.deleteAttributeAt(0);
		Instance testInstance = new DenseInstance(5);

		//testInstance.setValue(sourceInstances.attribute(0), 0);
		testInstance.setValue(sourceInstances.attribute(0), "helpful staff");
		testInstance.setValue(sourceInstances.attribute(1), 0);
		testInstance.setValue(sourceInstances.attribute(2), 2);
		testInstance.setValue(sourceInstances.attribute(3), "NN JJ");
		//testInstance.setValue(sourceInstances.attribute(4), "?");
		testInstance.setDataset(sourceInstances);
		
		System.out.println("Instance: " + testInstance);
		
	   double clsLabel = classifier.classifyInstance(testInstance);
	   testInstance.setClassValue(clsLabel);
	   System.out.println(testInstance.toString(testInstance.classAttribute()));
	   
	   double[] prediction=classifier.distributionForInstance(testInstance);

       //output predictions
       for(int i=0; i<prediction.length; i=i+1)
       {
    	   if(prediction[i]>0.1) {
    		   System.out.println("Probability of class "+testInstance.classAttribute().value(i)+" : "+Double.toString(prediction[i]));
    	   }
       }
	   
	}
	
	private StringToWordVector createS2WVector(int[] ignoreAttributeArray) {
		StringToWordVector s2wFilter;
		s2wFilter = new StringToWordVector(); 
		s2wFilter.setAttributeIndicesArray(ignoreAttributeArray);
		s2wFilter.setIDFTransform(true);
		s2wFilter.setLowerCaseTokens(true);
		
		return s2wFilter;
		
	}
	
   private Evaluation evalModel(Classifier classifier, Instances data, Integer numberIterations, Random randData ) throws Exception {
       Evaluation eval = new Evaluation(data);
       eval.crossValidateModel(classifier, data, numberIterations, randData);
       return eval;
   }
   
   public void loadModels(String filePath) throws Exception {
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
       myLog.log("Loading instances from " + fileName);
       BufferedReader inputReader = new BufferedReader(new FileReader(file));
       Instances data = new Instances(inputReader);
       data.setClassIndex(data.numAttributes() - posClass);

       return data;
   }
}
