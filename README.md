# General
This github repository contains the programming project which accompanies the bachelor thesis *Analyzing the transfer of aspect-based sentiment extraction to hotel reviews*.

**Abstract**



# Setup
The program represents the framework we used for implementing annotation procession, model construction and evaluation. 
The main pipeline is by running the code within the class [MainPipeline.java](https://github.com/jonasmeise/AutomaticAspectExtraction/blob/master/src/main/java/de/unidue/langtech/bachelor/meise/pipeline/MainPipeline.java).
In the current iteration, the code does not execute any of the tasks represented in the thesis. In exchange, a couple of methods are already implemented:

``buildFilesOldDomain(String inputFile, String outputFile)``

``buildFilesNewDomain(String inputFile, String outputFile, String typeFile)``

``executeAnnotationStudy()``

``foldLearning()``

An exemplary call for each main method is included in the comment section of the ``main()`` method. 
To properly execute method, make sure to correctly address the folder paths with the respective data - a short guideline for handling the data is presented in **Structure**.
Be careful with executing any methods, since any new generated data will be appended to older files. To circumvent this scenario, redirect the output into a preferably empty folder by setting the according file path.

# Structure

The main data for this program is included in [resources](https://github.com/jonasmeise/AutomaticAspectExtraction/tree/master/src/main/resources). The sub-folders include...

* **folders with "_\_learningtest_"** --> The model-specific instances of each reconstructed and implemented model, as well as their analysis in .txt format. If possible, do not edit these folders, since they contain the evaluation data of this thesis.

  * **constrained** --> The constrained data for the reconstructed task.
  
  * **unconstrained** --> The unconstrained data for the reconstructed task.
  
  Constrained and unconstrained folders are further split into the slots that the model was tested on. Each slot folder contains the learning instances (.arff), a raw performance of each classifier (analysis.txt or analysis_old.txt) and a calculated final performance score (eval.txt).
  
  * **old** --> The comparison data for the old SE-ABSA16 task.
  
  * save --> Outdated data. The files in it can be ignored.

* **folders with "datasetX"** --> The annotation data of each hotel X (1 to 5), in plain .txt form, parsed .xml form, converted .tsv form (for importing them into WebAnno) and the final .xmi JCas annotation file. The .xmi files are equal with the ones present in the root [resources](https://github.com/jonasmeise/AutomaticAspectExtraction/tree/master/src/main/resources) folder. For [dataset1](https://github.com/jonasmeise/AutomaticAspectExtraction/tree/master/src/main/resources/dataset1) and [dataset2](https://github.com/jonasmeise/AutomaticAspectExtraction/tree/master/src/main/resources/dataset2), the different annotations of both the curator and the respective annotator are included.
 
* **"sentimentlexicon"** --> The data files for the three sentiment lexicons that are used in the thesis.

**"SEABSA16_data"** --> The original training and test data that was available in the SemEval 2016 ABSA task.
