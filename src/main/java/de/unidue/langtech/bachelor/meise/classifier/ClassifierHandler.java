package de.unidue.langtech.bachelor.meise.classifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;

import de.unidue.langtech.bachelor.meise.extra.ConsoleLog;
import de.unidue.langtech.bachelor.meise.files.FileUtils;
import de.unidue.langtech.bachelor.meise.type.ArffGenerator;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public abstract class ClassifierHandler{
	public AspectClassifier aspectClassifier;
	public ArffGenerator arffGenerator;
	public FileUtils fu;
	public ConsoleLog myLog;
	public Instances instanceScheme;

	public String sourcePath;
	public String outputPath;
	
	public boolean slot1=false;
	
	public boolean useCFV;

    private String allData;
    public int[] removeArray;	
  	    
	 public void execute(int slot) {
		 execute(slot, null);
	 }
	 
	 public abstract void setSourcePath();
	 
	 public ClassifierHandler() {
		 myLog = new ConsoleLog(); 
		 fu = new FileUtils();
		 setSourcePath();
		 setOutputPath(sourcePath);
	 }
	  
	 public void generateFoldsAndLearn(Collection<String> arffFileInputs, int numFolds, int classAttributeAt, boolean idfTransformEnabled, Classifier outerParameterClassifier) {
		 ArrayList<String> analysisString = new ArrayList<String>();
		 allData="";
		 
		 myLog.log("This class is called from outside. Manually setting attributes...");
		 
		 myLog.log("WARNING: This current method will not generate any data, but only will analyse already existing .arff files with crossfold validation");	 
		 
		for(String arffFileInput : arffFileInputs) {
			try {
				//if learning for folds: continue from her
				ArrayList<Evaluation> allEvaluations;
				AspectClassifier foldClassifier = new AspectClassifier(outerParameterClassifier);;
				foldClassifier.idfTransformEnabled=idfTransformEnabled;
				
				if(removeArray!=null) {
					foldClassifier.removeArray = removeArray;
					
					myLog.log("Set remove array from outside.");
					myLog.log(ArrayUtils.toString(removeArray));
				}
				
				myLog.log("Use CFV=" + useCFV);
				
				if(useCFV) {
					Instances data = foldClassifier.getData(arffFileInput, classAttributeAt);
					System.out.println(classAttributeAt);
					
					foldClassifier.folds = numFolds;
					allEvaluations = foldClassifier.learn(data);
				} else {
					foldClassifier.folds = 0;
					
					Instances train = foldClassifier.getData(arffFileInput, classAttributeAt);
					myLog.log(arffFileInput + ".gold");
					Instances test = foldClassifier.getData(arffFileInput + ".gold", classAttributeAt);
					
					myLog.log("Found training data (" + train.size() + ") and test data (" + test.size() + ").");
					
					allEvaluations = foldClassifier.learn(train, test);
					numFolds = 1;
				}
				
				double precision=0, recall=0, fMeasure=0, accuracy=0;

				double tp=0;
				double tn=0;
				double fp=0;
				double fn=0;
				int counter=0;
				
				for(Evaluation singleEval : allEvaluations) {
			
					myLog.log("fold " + (++counter) + "/" + allEvaluations.size());
					for(int singleClass=0;singleClass<((!useCFV&&!slot1)?3:2);singleClass++) {
						myLog.log("Class " + singleClass + ": " + (singleEval.getClassPriors()[singleClass] / singleEval.numInstances()));		
					}
					
					tp += singleEval.numTruePositives(0);
					tn += singleEval.numTrueNegatives(0);
					fp += singleEval.numFalsePositives(0);
					fn += singleEval.numFalseNegatives(0);	
					
					accuracy += singleEval.pctCorrect();
				}
				
				tp = tp / numFolds;
				tn = tn / numFolds;
				fp = fp / numFolds;
				fn = fn / numFolds;
				
				recall = tp / (tp + fn);
				precision = tp / (tp + fp);
				accuracy = accuracy / numFolds;
				
				fMeasure = 2*recall*precision / (precision + recall);
					
				System.out.println("Precision\t" + precision);
				System.out.println("Recall\t" + recall);
				System.out.println("fMeasure" + fMeasure);
				System.out.println("Accuracy\t" + accuracy);
				
				analysisString.add(foldClassifier.sourcePath);
				analysisString.add("Precision\t" + precision);
				analysisString.add("Recall\t" + recall);
				analysisString.add("fMeasure" + fMeasure);
				analysisString.add("Accuracy\t" + accuracy);
				
				double balancedAccuracy = (tp / (tp + fn) + tn / (tn + fp))/2;
				
				analysisString.add("TP\t" + tp);
				analysisString.add("FP\t" + fp);
				analysisString.add("FN\t" + fn);
				analysisString.add("TN\t" + tn);
				analysisString.add("balanced accuracy\t" + balancedAccuracy);
				
				analysisString.add("");
				myLog.log("Completed " + numFolds +"-folded learning for '" + arffFileInput + "'.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		for(String line : analysisString) {
			allData = allData + line + "\n";
		}
		
		try {
			fu.createWriter(outputPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		fu.write(allData);
		fu.close();
	 }


	public abstract void execute(int slot, int[] removeArray);
	
	public void useOldData(boolean useCFV) {
		this.useCFV = !useCFV;
		
		//update source-path
		setSourcePath();
	}
	
	public ArrayList<String> fetchFiles() {
		return fu.getFilesInFolder(sourcePath, ".arff", false);
	}
	
	public void setUpAblation(String indexRemove, int maxIteration, int slot) {
		String[] split = indexRemove.split(",");
		removeArray = new int[split.length+1];
		
		for(int n=0;n<split.length;n++) {
			removeArray[n] = Integer.valueOf(split[n]);
		}
		
		for(int i=1;i<maxIteration;i++) {
			//check if it isn't already included
			String alreadyIncluded="";
			boolean allGood=true;
			
			for(int check=0;check<removeArray.length-1;check++) {
				alreadyIncluded += removeArray[check];
				if(removeArray[check]==i) {
					allGood=false;
				}
			}
			
			if(allGood) {
				removeArray[removeArray.length-1] = i;
				System.out.println("Ablation test: Remove features '" + alreadyIncluded + "' + '" + i + "'.");
				
				if(slot == 1) {
					setSourcePath_Slot1();
					setSlot1(true);
				}
				
				myLog.log("REMOVE ARRAY: " + removeArray);
				setOutputPath(sourcePath + "_" + alreadyIncluded + "_" + i + ".txt");
				execute(slot, removeArray);
			}
		}
	}
	
	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
		myLog.log("Set output path to '" + outputPath + "'.");
	}
	
	public void setSlot1(boolean slot1) {
		this.slot1 = slot1;
	}
	
	public void setSourcePath_Slot1() {
		//filler
	}
}
