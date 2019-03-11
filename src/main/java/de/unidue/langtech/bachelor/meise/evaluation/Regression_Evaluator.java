package de.unidue.langtech.bachelor.meise.evaluation;

import de.unidue.langtech.bachelor.meise.classifier.ClassifierHandler;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.SimpleLinearRegression;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CVParameterSelection;
import weka.core.SelectedTag;

public class Regression_Evaluator extends ClassifierHandler{
	
	boolean constrained = true;
	
	public Regression_Evaluator(boolean oldData) {
		this();
		useOldData(oldData);
	}
	
	public Regression_Evaluator() {
		super();
		constrained = true;
		useOldData(false);
	}
	
	//slot equals the category:
	//0 = constrained
	//1 = unconstrained
	//2 = unconstrained (no text)
	public void execute(int slot) {
		myLog.log("Please set a classifier type.");
	}
	
	@Override
	public void execute(int slot, int[] removeArray) {
		execute(slot);
	}
	
	public void execute(int slot, String classifierType) {	
		if(slot==1) {
			constrained = true;
			removeArray = new int[10];
			removeArray[0] = 0;
			removeArray[1] = 4;
			removeArray[2] = 5;
			removeArray[3] = 6;
			removeArray[4] = 7;
			removeArray[5] = 8;
			removeArray[6] = 9;
			removeArray[7] = 10;
			removeArray[8] = 11;
			removeArray[9] = 12;
		} else {
			constrained = false;
			
			if(slot == 3) {
				removeArray = new int[4];
				removeArray[0] = 0;
				removeArray[1] = 1;
				removeArray[2] = 2;
				removeArray[3] = 3;
			}
		}
		
		execute(slot, null, classifierType);
	}
	
	public void execute(int slot, int[] removeArray, String classifierType) {
		//if(removeArray==null) {
			setSourcePath();
			setOutputPath(sourcePath + "analysis_" + slot + "_" + classifierType + ".txt");
		//}

		if(classifierType.equals("esvr")) {
			LibSVM svm = new LibSVM();
			svm.setSVMType(new SelectedTag(LibSVM.SVMTYPE_EPSILON_SVR, LibSVM.TAGS_SVMTYPE));
			
			CVParameterSelection cvp = new CVParameterSelection();
			String[] parameters = new String[2];
			parameters[0] = "C 0.01 100 5";
			parameters[1] = "P 0.01 0.5 5";
			
			try {
				cvp.setOptions(parameters);
				cvp.setNumFolds(5);
				cvp.setClassifier(svm);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			generateFoldsAndLearn(fetchFiles(), 10, 1, false, svm);

		} else if(classifierType.equals("slr")){
			SimpleLinearRegression slr = new SimpleLinearRegression();
			slr.setOutputAdditionalStats(true);
			
			generateFoldsAndLearn(fetchFiles(), 10, 1, false, slr);
		} else if(classifierType.equals("knn")){ 
			 IBk ibk = new IBk();	
			 ibk.setKNN(3);
			 ibk.setDebug(true);
			 ibk.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_SIMILARITY, IBk.TAGS_WEIGHTING));
			 ibk.setMeanSquared(true);
			 
			 generateFoldsAndLearn(fetchFiles(), 10, 1, false, ibk);
		} else if(classifierType.equals("lr")) {
			 LinearRegression lr = new LinearRegression(); 
			 lr.setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_M5, LinearRegression.TAGS_SELECTION));
			 lr.setRidge(0.0000001);
			 lr.setDebug(true);
			 
			 generateFoldsAndLearn(fetchFiles(), 10, 1, false, lr);
		} else {
			myLog.log("Wrong classifier type: " + classifierType);
		}
	}

	@Override
	public void setSourcePath() {
		if(constrained) {
			sourcePath = "src\\main\\resources\\RQ2_learningtest\\constrained\\";
		} else {
			sourcePath = "src\\main\\resources\\RQ2_learningtest\\unconstrained\\";
		}
	}
}