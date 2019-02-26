package de.unidue.langtech.bachelor.meise.evaluation;

import de.unidue.langtech.bachelor.meise.classifier.ClassifierHandler;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SGD;
import weka.classifiers.meta.CVParameterSelection;
import weka.core.SelectedTag;

public class OwnClass_Evaluator extends ClassifierHandler{
	
	public boolean constrained;
	
	public void setConstrained(boolean constrained) {
		this.constrained = constrained;
	}
	
	public OwnClass_Evaluator(boolean oldData) {
		this();
		useCFV = true;
	}
	
	public OwnClass_Evaluator() {
		super();
	}
	
	public void execute(int slot, int[] removeArray) {
		LibSVM svm = new LibSVM();
		int numFolds = 10;
		
		if(removeArray!=null) {
			//boosting up the speed of the ablation test
			numFolds = numFolds / 2;
		}
		
		if(slot==3) {
			setSourcePath();
			
			if(removeArray==null) {
				setOutputPath(sourcePath + "analysis.txt");
			}
			
			if(constrained) {
				svm.setCost(200);
				svm.setGamma(0.002);
				svm.setEps(0.0005);
			} else {
				svm.setCost(210);
				svm.setGamma(0.0015);
				svm.setEps(0.0001);
			}
			
			generateFoldsAndLearn(fetchFiles(), numFolds, 1, false, svm);
		} else if(slot==1){
			setSourcePath_Slot1();
			setSlot1(true);
			
			if(removeArray==null) {
				setOutputPath(sourcePath + "analysis.txt");
			} else {
				if(!constrained) {
					removeArray = new int[3];
					
					removeArray[0] = 0;
					removeArray[1] = 2;
					removeArray[2] = 3;
				}
			}
			
			if(constrained) {
				svm.setCost(500);
				svm.setGamma(0.001);
				svm.setEps(0.00005);
			} else {
				svm.setCost(500);
				svm.setGamma(0.001);
				svm.setEps(0.00005);
			}
			
			generateFoldsAndLearn(fetchFiles(), numFolds, 1, false, svm);
		} else {
			myLog.log("Wrong slot: " + slot);
		}
	}

	@Override
	public void setSourcePath() {
		if(useCFV) {
			if(constrained) {
				sourcePath = "src\\main\\resources\\learningtest_OwnClassifier\\subtask3\\constrained\\";
			} else {
				sourcePath = "src\\main\\resources\\learningtest_OwnClassifier\\subtask3\\unconstrained\\";
			}
		} else {
			myLog.log("Non-CFV approach does not exist!");
		}
	}
	
	public void setSourcePath_Slot1() {
		if(useCFV) {
			if(constrained) {
				sourcePath = "src\\main\\resources\\learningtest_OwnClassifier\\subtask1\\constrained\\";
			} else {
				sourcePath = "src\\main\\resources\\learningtest_OwnClassifier\\subtask1\\unconstrained\\";
			}
		} else {
			myLog.log("Non-CFV approach does not exist!");
		}
	}
}
