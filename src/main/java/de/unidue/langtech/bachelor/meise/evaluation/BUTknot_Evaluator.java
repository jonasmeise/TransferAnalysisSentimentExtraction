package de.unidue.langtech.bachelor.meise.evaluation;

import de.unidue.langtech.bachelor.meise.classifier.ClassifierHandler;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SGD;
import weka.classifiers.meta.AdditiveRegression;
import weka.classifiers.meta.CVParameterSelection;
import weka.core.SelectedTag;
import weka.packages.ThresholdSelector;

public class BUTknot_Evaluator extends ClassifierHandler{
	
	public BUTknot_Evaluator(boolean oldData) {
		this();
		useCFV = !oldData;
	}
	
	public BUTknot_Evaluator() {
		super();
	}
	
	public void execute(int slot, int[] removeArray) {	
		if(slot==1){
			setSlot1(true);
			
			if(removeArray==null) {
				setOutputPath(sourcePath + "analysis.txt");
			}
			
			SGD sgd = new SGD();
			sgd.setLossFunction(new SelectedTag(SGD.LOGLOSS, SGD.TAGS_SELECTION));
			sgd.setLearningRate(0.41);
			sgd.setEpsilon(0.01);
			sgd.setEpochs(20);
			
			ThresholdSelector ts = new ThresholdSelector();
			ts.setClassifier(sgd);
			ts.setDesignatedClass(new SelectedTag(ThresholdSelector.OPTIMIZE_0, ThresholdSelector.TAGS_OPTIMIZE));
			ts.setNumXValFolds(4);
			
			try {
				//ts.setManualThresholdValue(0.40);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			generateFoldsAndLearn(fetchFiles(), 5, 1, false, ts);
		} else {
			myLog.log("Wrong slot: " + slot);
		}
	}

	@Override
	public void setSourcePath() {
		if(useCFV) {
			sourcePath = "src\\main\\resources\\learningtest_BUTknot\\subtask1\\constrained\\";
		} else {
			sourcePath = "src\\main\\resources\\learningtest_BUTknot\\old\\constrained\\";
		}
	}
}
