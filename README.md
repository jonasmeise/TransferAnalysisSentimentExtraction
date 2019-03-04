# General
This github repository contains the programming project which accompanies the bachelor thesis *Analyzing the transfer of aspect-based sentiment extraction to hotel reviews*.

**Abstract**



# Setup
The program represents the framework we used for implementing annotation procession, model construction and evaluation. 
The main pipeline is by running the code within the class [MainPipeline.java](https://github.com/jonasmeise/AutomaticAspectExtraction/blob/master/src/main/java/de/unidue/langtech/bachelor/meise/pipeline/MainPipeline.java).
In the current iteration, the code does not execute any of the tasks represented in the thesis. In exchange, a couple of methods are already implemented:

``buildFilesOldDomain(String inputFile, String outputFolder)``

``buildFilesNewDomain(String inputFile, String outputFolder)``

``executeAnnotationStudy()``

``foldLearning()``

## Example Run
For example, if you want to build the Baseline2 model (Simple Classifier) for Slot 3 of the new data, enable the following method calls in MainPipeline. If you chose to execute the method, make sure to delete the preexisting files in the output folder first.

```java
public static void main(String[] args) throws Exception {
		MainPipeline myPipeline = new MainPipeline();

		myPipeline.buildFilesNewDomain("src\\main\\resources", "src\\main\\resources\\learningtest_Baseline2\\subtask3\\constrained");
		myPipeline.foldLearning();
	}
```
The first parameter refers to the folder containing the .xmi files with the annotated data set (sub-folders are ignored). 
The second parameter refers to the output folder, where the generated .arff files are saved in.

To correctly choose the right models, you have to specify the model classifier within the ``writer`` engine in ``buildFilesNewDomain``:
```java
AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(Baseline2_ClassifierGenerator3.class, 
	        		Baseline2_ClassifierGenerator3.PARAM_OUTPUT_PATH, outputFolder, 
	        		Baseline2_ClassifierGenerator3.PARAM_RELATION_NAME, "Baseline2",
	        		Baseline2_ClassifierGenerator3.PARAM_CONSTRAINED, "true",
	        		Baseline2_ClassifierGenerator3.PARAM_USE_OLD_DATA, "false");
```
``Baseline2_ClassifierGenerator3.class`` can be interchanged with any of the generator models that are in [\classifiers\\](https://github.com/jonasmeise/TransferAnalysisSentimentExtraction/tree/master/src/main/java/de/unidue/langtech/bachelor/meise/type/classifiers). The suffix "3" indicates the Slot 3 model, no suffix indicates Slot 1.

``PARAM_OUTPUT_PATH`` sets the output folder for the .arff files.
``PARAM_RELATION_NAME`` sets the relation name for the .arff files.
``PARAM_CONSTRAINED`` sets the constrain type of the model. If the model only supports a single type, then setting this parameter has no impact.
``PARAM_USE_OLD_DATA`` sets the input type: ``true`` generates .arff files based on the data from SE-ABSA16, while ``false`` generates them for the new domain. Changing this value is not necessary, since the methods already have the correct value by default.

An exemplary call for each main method is included in the comment section of the ``main()`` method. 
To properly execute method, make sure to correctly address the folder paths with the respective data - a short guideline for handling the data is presented in **Structure**.
Be careful with executing any methods, since any new generated data will be appended to older files. To circumvent this scenario, redirect the output into a preferably empty folder by setting the according file path.

# Structure

The main data for this program is included in [resources](https://github.com/jonasmeise/AutomaticAspectExtraction/tree/master/src/main/resources). The sub-folders include...

* **folders with "_\_learningtest_"** --> The model-specific instances of each reconstructed and implemented model, as well as their analysis in .txt format. If possible, do not edit these folders, since they contain the evaluation data of this thesis.

  * **constrained** --> The constrained data for the reconstructed task.
  
  * **unconstrained** --> The unconstrained data for the reconstructed task.
  
  Constrained and unconstrained folders are further split into the slots that the model was tested on. Each slot folder contains the learning instances (.arff), a raw performance of each classifier (analysis.txt or analysis_old.txt) and a calculated final performance score (eval.txt). The paths to these final sub-folders are used when generating the output data for the classifier models. We recommend to exchange this file path with a custom folder, otherwise the files are overwritten and corrupted.
  
  * **old** --> The comparison data for the old SE-ABSA16 task.
  
  * save --> Outdated data. The files in it can be ignored.

* **folders with "datasetX"** --> The annotation data of each hotel X (1 to 5), in plain .txt form, parsed .xml form, converted .tsv form (for importing them into WebAnno) and the final .xmi JCas annotation file. The .xmi files are equal with the ones present in the root [resources](https://github.com/jonasmeise/AutomaticAspectExtraction/tree/master/src/main/resources) folder. For [dataset1](https://github.com/jonasmeise/AutomaticAspectExtraction/tree/master/src/main/resources/dataset1) and [dataset2](https://github.com/jonasmeise/AutomaticAspectExtraction/tree/master/src/main/resources/dataset2), the different annotations of both the curator and the respective annotator are included.
 
* **"sentimentlexicon"** --> The data files for the three sentiment lexicons that are used in the thesis.

**"SEABSA16_data"** --> The original training and test data that was available in the SemEval 2016 ABSA task.

**"stopwords.txt"** --> The file with NLKT stopword list.

All other files can mostly be ignored.
