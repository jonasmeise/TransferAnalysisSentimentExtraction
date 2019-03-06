package de.unidue.langtech.bachelor.meise.evaluation;

import de.unidue.langtech.bachelor.meise.classifier.ClassifierHandler;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SGD;
import weka.classifiers.meta.CVParameterSelection;
import weka.core.SelectedTag;
import weka.packages.ThresholdSelector;

public class Baseline2_Evaluator extends ClassifierHandler{
	
	public Baseline2_Evaluator(boolean oldData) {
		this();
		useCFV = true;
	}
	
	public Baseline2_Evaluator() {
		super();
	}
	
	public void execute(int slot, int[] removeArray) {	
		LibSVM svm = new LibSVM();
		svm.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_LINEAR, LibSVM.TAGS_KERNELTYPE));
		
		
		if(slot==3) {
			if(removeArray==null) {
				setOutputPath(sourcePath + "analysis.txt");
			}

			generateFoldsAndLearn(fetchFiles(), 10, 1, false, svm);
		} else if(slot==1){
			setSourcePath_Slot1();
			setSlot1(true);
			
			if(removeArray==null) {
				setOutputPath(sourcePath + "analysis.txt");
			}
			
			svm.setProbabilityEstimates(true);
			ThresholdSelector ts = new ThresholdSelector();

			try {
				ts.setManualThresholdValue(0.4);
			} catch (Exception e) {
				e.printStackTrace();
			}

			ts.setClassifier(svm);

			generateFoldsAndLearn(fetchFiles(), 10, 1, false, ts);
		} else {
			myLog.log("Wrong slot: " + slot);
		}
	}

	@Override
	public void setSourcePath() {
		if(useCFV) {
			sourcePath = "src\\main\\resources\\learningtest_baseline2\\subtask3\\constrained\\";
		} else {
			sourcePath = "src\\main\\resources\\learningtest_baseline2\\subtask3\\old\\constrained\\";
		}
	}
	
	public void setSourcePath_Slot1() {
		if(useCFV) {
			sourcePath = "src\\main\\resources\\learningtest_baseline2\\subtask1\\constrained\\";
		} else {
			sourcePath = "src\\main\\resources\\learningtest_baseline2\\subtask1\\old\\constrained\\";
		}
	}
}
