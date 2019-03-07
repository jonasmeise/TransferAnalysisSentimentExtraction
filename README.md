# General
This github repository contains the programming project which accompanies the bachelor thesis *Analyzing the transfer of aspect-based sentiment extraction to hotel reviews*.

**Abstract**



# Setup
The program represents the framework we used for implementing annotation procession, model construction and evaluation. This is a Maven project, so downloading and importing the ``pom.xml`` in the main folder is the first step. Make sure that all classes referenced in the ``pom.xml`` are properly imported and available after your IDE downloaded its data. If ClassNotFound compiling errors occur, please check if Maven found the correct version of each dependency.

The main pipeline is the main program, and is executed by running the code within the class [MainPipeline.java](https://github.com/jonasmeise/AutomaticAspectExtraction/blob/master/src/main/java/de/unidue/langtech/bachelor/meise/pipeline/MainPipeline.java).
In the current iteration, the code does not execute any of the tasks represented in the thesis. But, all methods that are needed for constructing our found results are implemented:

``buildFilesOldDomain(String inputFile, String outputFolder)``

``buildFilesNewDomain(String inputFile, String outputFolder)``

``executeAnnotationStudy()``

``foldLearning()``

To show how we generated our data and results, we will showcase the procedure and exact steps, so you can replicate them.

## Example Run: Data Generation
If you want to build the Baseline2 model (Simple Classifier) for Slot 3 of the new data, enable the following method calls in MainPipeline. If you chose to execute the method, make sure to delete the preexisting files in the output folder first, since otherwise they are overwritten, and the resulting data would be unusable.

```java
public static void main(String[] args) throws Exception {
		MainPipeline myPipeline = new MainPipeline();

		myPipeline.buildFilesNewDomain("src\\main\\resources", "src\\main\\resources\\learningtest_Baseline2\\subtask3\\constrained");
	}
```
The first parameter refers to the folder containing the .xmi files with the annotated data set (sub-folders are ignored). This patch should usually not be changed, since ``output_1.xmi`` to ``output_5.xmi`` contain the annotated data for the five hotels.
The second parameter refers to the output folder, where the generated ``.arff`` files that contain the data for the model are saved in. This patch should be changed to the designated folders, as shown in the example.

To correctly choose the right models, you have to specify the model classifier within the ``writer`` object in ``buildFilesNewDomain``:
```java
AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(Baseline2_ClassifierGenerator3.class, 
	        		Baseline2_ClassifierGenerator3.PARAM_OUTPUT_PATH, outputFolder, 
	        		Baseline2_ClassifierGenerator3.PARAM_RELATION_NAME, "Baseline2",
	        		Baseline2_ClassifierGenerator3.PARAM_CONSTRAINED, "true",
	        		Baseline2_ClassifierGenerator3.PARAM_USE_OLD_DATA, "false");
```
``Baseline2_ClassifierGenerator3.class`` can be interchanged with any of the generator models that are in [\classifiers\\](https://github.com/jonasmeise/TransferAnalysisSentimentExtraction/tree/master/src/main/java/de/unidue/langtech/bachelor/meise/type/classifiers). The suffix "3" indicates the Slot 3 model, no suffix indicates Slot 1.

``PARAM_OUTPUT_PATH`` sets the output folder for the .arff files. This parameter is already set due to the method call.
``PARAM_RELATION_NAME`` sets the relation name for the .arff files. This is an optional parameter, it does not change the data itself.
``PARAM_CONSTRAINED`` sets the constrain type of the model. If the model only supports a single type, then setting this parameter has no impact.
``PARAM_USE_OLD_DATA`` sets the input type: ``true`` generates ``.arff`` files (training + test data) based on the data from SE-ABSA16, while ``false`` generates them for the new domain (only training). You should not change this value: if you want to build the same model for the old data, call ``buildFilesOldDomain("src\\main\\resources\\SEABSA16_data", "src\\main\\resources\\learningtest_Baseline2\\subtask3\\constrained")`` instead. Be sure to set the correct generator model as described in _both_ writer pipelines (one pipeline generates training, the other test data).

An exemplary call for each main method is included in the comment section of the ``main()`` method. 
To properly execute the method, make sure to correctly address the folder paths with the respective data - a short guideline for handling the data is presented in **Structure**.
Be careful with executing any methods, since any new generated data will be appended to older files. To circumvent this scenario, redirect the output into a preferably empty folder by setting the according file path.

## Example Run: Evaluation

If you want to run the evaluation process for a model, execute the method ``myPipeline.foldLearning()``.

```java
public void foldLearning() throws Exception {	 
		AKTSKI_Evaluator myEvaluator = new AKTSKI_Evaluator();
		myEvaluator.useOldData(false);
		//myEvaluator.setUpAblation("0,1,2", 23, 3);
		myEvaluator.execute(3);
	}
```
The evaluator class for each model is contained in the folder [\evaluation\\](https://github.com/jonasmeise/TransferAnalysisSentimentExtraction/tree/master/src/main/java/de/unidue/langtech/bachelor/meise/evaluation). Following commands can be executed on an ``\_Evaluator`` class:

``useOldData(boolean)`` enables the evaluation on either the new data (``false``) or the old data (``true``). Old data is evaluated on a fixed training/test data split, while the new domain is evaluated with 10-folded cross validation. 

``execute(slot)`` executes evaluation for a specific slot. If the slot does not exist for the model, an error prompt will be shown. The ``analysis.txt`` file is autmoatically generated in the corresponding folder as presented in **[Structure](#structure)**. This source path can not be changed, unless you change the source code within the ``\_Evaluator`` class (``getSourcePath()`` and ``getSourcePath_Slot1()``).

``setUpAblation(alreadyRemoved, maxFeatures, slot)`` executes an iterative execution slot on a reduced set of features. The string ``alreadyRemoved`` indicates the features that are _already_ removed. "0" should be always included, since for every model, the first feature (ID) is always removed. Additional numbers are added by separating them with a comma. The integer ``maxFeatures`` refers to the maximum number of features in a model (counting from 1, including the final class label). For each feature that is not already contained in ``alreadyRemoved`` and its index is smaller than ``maxFeatures``, it will be removed once in the ablation test. The final integer ``slot`` refers to the used slot for the test - it equals the ``slot`` parameter of the ``execute(slot)`` method. The method produces an analysis file for each variation of the feature analysis (with the naming syntax ``ablation_alreadyRemoved_currentlyRemoved.txt``) in the base folder of the respective model. To run multiple iterative ablation tests, the ``alreadyRemoved`` has to manually updated by the user and the method needs to be called again.

## How are the models evaluated? How do you get the performance values?

For each model, the same procedure is done. 

```java
	buildFilesOldDomain("src//main//resources//SEABSA16_data", "src//main//resources//learningtest_modelXXX//subtaskX//old//constrained//");
	foldLearning();
``` 
for generating the training files (``.arff``), test files (``.gold``) and the evaluation file ``analysis.txt`` regarding the **old** data.

_or_

```java
	buildFilesNewDomain("src//main//resources", "src//main//resources//learningtest_modelXXX//subtaskX//constrained//");
	foldLearning();
``` 
for generating the training files (``.arff``) and the evaluation file ``analysis.txt`` regarding the **new** data.

The objects, methods and parameters are set with the same methods as described in **[Setup](#setup)**.

We manually combined the individual performance results of the binary classifiers with the labels, as described in the thesis, and calculated the final score of a model. The singular performances of each classifier are in the ``analysis.txt`` file of each model folder. The weights for the hotel domain are presented in table 3.2 of the thesis (percentage share), the distribution of labels in the test data of the restaurant domain are included in the file [stats.txt](https://github.com/jonasmeise/TransferAnalysisSentimentExtraction/blob/master/src/main/resources/SEABSA16_data/stats.txt). We used a small assistance table for calculating the respective values.

# Structure

## Folders

The main data for this program is included in [resources](https://github.com/jonasmeise/AutomaticAspectExtraction/tree/master/src/main/resources). The sub-folders include...

* **folders with "_\_learningtest_"** --> The model-specific instances of each reconstructed and implemented model, as well as their analysis in .txt format. If possible, do not edit these folders, since they contain the evaluation data of this thesis.

  * **constrained** --> The constrained data for the reconstructed task.
  
  * **unconstrained** --> The unconstrained data for the reconstructed task.
  
  Constrained and unconstrained folders are further split into the slots that the model was tested on. Each slot folder contains the learning instances (``.arff``), a raw performance of each classifier (``analysis.txt`` or ``analysis_old.txt``) and a calculated final performance score (``eval.txt``). The paths to these final sub-folders are used when generating the output data for the classifier models. We recommend to exchange this file path with a custom folder, otherwise the files are overwritten and corrupted.
  
  * **old** --> The comparison data for the old SE-ABSA16 task.
  
  * save --> Outdated data. The files in it can be ignored.

* **folders with "datasetX"** --> The annotation data of each hotel X (1 to 5), in plain .txt form, parsed .xml form, converted .tsv form (for importing them into WebAnno) and the final .xmi JCas annotation file. The .xmi files are equal with the ones present in the root [resources](https://github.com/jonasmeise/AutomaticAspectExtraction/tree/master/src/main/resources) folder. For [dataset1](https://github.com/jonasmeise/AutomaticAspectExtraction/tree/master/src/main/resources/dataset1) and [dataset2](https://github.com/jonasmeise/AutomaticAspectExtraction/tree/master/src/main/resources/dataset2), the different annotations of both the curator and the respective annotator are included.
 
* **"sentimentlexicon"** --> The data files for the three sentiment lexicons that are used in the thesis.

**"SEABSA16_data"** --> The original training and test data that was available in the SemEval 2016 ABSA task.

**"stopwords.txt"** --> The file with NLKT stopword list.

All other files can mostly be ignored.

## Class Descriptions

The final section includes a description of all classes, to give you a general idea about what exactly they do. Our implemented classes can be found in the folder [/meise/](https://github.com/jonasmeise/TransferAnalysisSentimentExtraction/tree/master/src/main/java/de/unidue/langtech/bachelor/meise). The other classes are either auto-generated (WebAnno-classes) or external classes, which are used as a surrogate for methods that are not yet implemented in Maven-libraries (Weka).


