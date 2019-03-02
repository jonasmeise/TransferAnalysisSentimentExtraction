package de.unidue.langtech.bachelor.meise.evaluation;

import de.unidue.langtech.bachelor.meise.classifier.ClassifierHandler;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.functions.LibSVM;
import weka.core.SelectedTag;

public class AKTSKI_Evaluator extends ClassifierHandler{
	
	public AKTSKI_Evaluator(boolean oldData) {
		this();
		useCFV = true;
	}
	
	public AKTSKI_Evaluator() {
		super();
		numFolds = 10;
	}
	
	public void execute(int slot, int[] removeArray) {
		if(removeArray==null) {
			setOutputPath(sourcePath + "analysis.txt");
		} else {
			numFolds = 5;
		}
		
		if(slot==3) {
			LibSVM svm = new LibSVM();
			svm.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_RBF, LibSVM.TAGS_KERNELTYPE));
			svm.setCost(100);
			svm.setGamma(0.001);
			
			generateFoldsAndLearn(fetchFiles(), 10, 1, true, svm);
		} else {
			myLog.log("Wrong slot: " + slot);
		}
	}

	@Override
	public void setSourcePath() {
		if(useCFV) {
			sourcePath = "src\\main\\resources\\learningtest_AKTSKI\\subtask3\\unconstrained\\";
		} else {
			sourcePath = "src\\main\\resources\\learningtest_AKTSKI\\subtask3\\old\\unconstrained\\";
		}
	}
}
