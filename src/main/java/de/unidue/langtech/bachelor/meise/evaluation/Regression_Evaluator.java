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
		} else {
			constrained = false;
		}
		
		execute(slot, null, classifierType);
	}
	
	public void execute(int slot, int[] removeArray, String classifierType) {
		if(removeArray==null) {
			setOutputPath(sourcePath + "analysis_" + classifierType + ".txt");
		}
		
		if(classifierType.equals("esvr")) {
			LibSVM svm = new LibSVM();
			svm.setSVMType(new SelectedTag(LibSVM.SVMTYPE_EPSILON_SVR, LibSVM.TAGS_SVMTYPE));
			
			CVParameterSelection cvp = new CVParameterSelection();
			String[] parameters = new String[3];
			parameters[0] = "K 0 3 4";
			parameters[1] = "C 0.01 100 5";
			parameters[2] = "P 0.01 0.5 3";
			
			try {
				cvp.setOptions(parameters);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			generateFoldsAndLearn(fetchFiles(), 10, 1, true, cvp);
		} else if(classifierType.equals("slr")){
			SimpleLinearRegression slr = new SimpleLinearRegression();
			slr.setOutputAdditionalStats(true);
			
			generateFoldsAndLearn(fetchFiles(), 10, 1, true, slr);
		} else if(classifierType.equals("knn")){ 
			 IBk ibk = new IBk();	
			 ibk.setKNN(3);
			 ibk.setDebug(true);
			 ibk.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_SIMILARITY, IBk.TAGS_WEIGHTING));
			 ibk.setMeanSquared(true);
			 
			 generateFoldsAndLearn(fetchFiles(), 10, 1, true, ibk);
		} else if(classifierType.equals("lr")) {
			 LinearRegression lr = new LinearRegression(); 
			 lr.setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_M5, LinearRegression.TAGS_SELECTION));
			 lr.setRidge(0.0000001);
			 lr.setDebug(true);
			 
			 generateFoldsAndLearn(fetchFiles(), 10, 1, true, lr);
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