package de.unidue.langtech.bachelor.meise.evaluation;

import de.unidue.langtech.bachelor.meise.classifier.ClassifierHandler;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SGD;
import weka.classifiers.meta.CVParameterSelection;
import weka.core.SelectedTag;

public class GTI_Evaluator extends ClassifierHandler{
	
	public GTI_Evaluator(boolean oldData) {
		this();
		useCFV = true;
	}
	
	public GTI_Evaluator() {
		super();
	}
	
	public void execute(int slot, int[] removeArray) {	
		if(slot==3) {
			//compare the final console output of class GTI_ClassifierGenerator3
			//learning is not here included, because the system is evaluated within its class due to its rule-based structure
			myLog.log("No evaluation available. Check the corresponding class output of GTI_ClassifierGenerator3.");
		} else if(slot==1){
			setSlot1(true);
			
			if(removeArray==null) {
				setOutputPath(sourcePath + "analysis.txt");
			}
			
			LibSVM svm = new LibSVM();
			svm.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_LINEAR, LibSVM.TAGS_KERNELTYPE));
			
			generateFoldsAndLearn(fetchFiles(), 10, 1, false, svm);
		} else {
			myLog.log("Wrong slot: " + slot);
		}
	}

	@Override
	public void setSourcePath() {
		if(useCFV) {
			sourcePath = "src\\main\\resources\\learningtest_GTI\\subtask1\\unconstrained\\";
		} else {
			sourcePath = "src\\main\\resources\\learningtest_GTI\\subtask1\\old\\unconstrained\\";
		}
	}
}
