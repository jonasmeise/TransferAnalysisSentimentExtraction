package de.unidue.langtech.bachelor.meise.evaluation;

import de.unidue.langtech.bachelor.meise.classifier.ClassifierHandler;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SGD;
import weka.classifiers.meta.CVParameterSelection;
import weka.core.SelectedTag;

public class AUEB_Evaluator extends ClassifierHandler{
	
	public AUEB_Evaluator(boolean oldData) {
		this();
		useCFV = true;
	}
	
	public AUEB_Evaluator() {
		super();
	}
	
	public void execute(int slot, int[] removeArray) {	
		if(slot==3) {
			if(removeArray==null) {
				setOutputPath(sourcePath + "analysis.txt");
			}
			
			Logistic logistic = new Logistic();
			logistic.setRidge(0.1);
			
			generateFoldsAndLearn(fetchFiles(), 10, 1, false, logistic);
		} else if(slot==1){
			setSourcePath_Slot1();
			setSlot1(true);
			
			if(removeArray==null) {
				setOutputPath(sourcePath + "analysis.txt");
			}
			
			LibSVM svm = new LibSVM();
			if(useCFV) {
				svm.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_RBF, LibSVM.TAGS_KERNELTYPE));
				svm.setCost(125);
				svm.setGamma(0.0021);
			} else {
				svm.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_RBF, LibSVM.TAGS_KERNELTYPE));
				svm.setCost(100);
				svm.setGamma(0.001);
			}

			//CVP was already conducted, the optimal parameters are in svm
			CVParameterSelection cvp = new CVParameterSelection();
			cvp.setClassifier(svm);
			String[] options = new String[3];
			options[0] = "K 0 3 4";
			options[1] = "C 0.01 1000 10";
			options[2] = "G 0.0001 0.01 10";
			
			try {
				cvp.setNumFolds(5);
				cvp.setCVParameters(options);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			generateFoldsAndLearn(fetchFiles(), 10, 1, false, svm);
		} else {
			myLog.log("Wrong slot: " + slot);
		}
	}

	@Override
	public void setSourcePath() {
		if(useCFV) {
			sourcePath = "src\\main\\resources\\learningtest_AUEB\\subtask3\\unconstrained\\";
		} else {
			sourcePath = "src\\main\\resources\\learningtest_AUEB\\subtask3\\old\\unconstrained\\";
		}
	}
	
	public void setSourcePath_Slot1() {
		if(useCFV) {
			sourcePath = "src\\main\\resources\\learningtest_AUEB\\subtask1\\constrained\\";
		} else {
			sourcePath = "src\\main\\resources\\learningtest_AUEB\\subtask1\\old\\constrained\\";
		}
	}
}
