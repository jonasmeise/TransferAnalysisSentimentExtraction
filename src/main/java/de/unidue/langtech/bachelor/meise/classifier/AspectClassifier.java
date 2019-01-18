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
import java.util.Collection;
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
	
	int seed;
	int folds = 10;
	
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
	
	public ArrayList<String> learn(Instances data) throws Exception {
		ArrayList<String> returnList = new ArrayList<String>();
		Random rand = new Random(seed);
		 Instances randData = new Instances(data);
		 randData.randomize(rand);
		 
		 for (int n = 0; n < folds; n++) {
			   Instances train = randData.trainCV(folds, n, rand);
			   Instances test = randData.testCV(folds, n);
 
			   test.deleteAttributeAt(0);
			   //build-Classifier loescht automatisch 0
			   train = buildClassifier(train);
			   
			   evaluation = evalModel(classifier, test, folds, new Random());
			   returnList.add(data.relationName() + "\tFold#" + (n+1) + "\n" 
			   + "FP: " +evaluation.numFalsePositives(train.numAttributes()-1) + "\n"
			   + "FN: " +evaluation.numFalseNegatives(train.numAttributes()-1) + "\n"
			   + "TN: " +evaluation.numTrueNegatives(train.numAttributes()-1) + "\n"
			   + "TP: " +evaluation.numTruePositives(train.numAttributes()-1)
			   + evaluation.toSummaryString()); 
		 }
		 
		   return returnList;
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
       myLog.log("Loading instances from " + fileName + "...");
       BufferedReader inputReader = new BufferedReader(new FileReader(file));
       Instances data = new Instances(inputReader);
       data.setClassIndex(data.numAttributes() - posClass);

       return data;
   }
}
