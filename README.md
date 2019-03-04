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

The main data for this program is included in [resources](https://github.com/jonasmeise/AutomaticAspectExtraction/tree/master/src/main/resources). It includes

* **folders with _\_learningtest_** --> The model-specific instances of each reconstructed and implemented model, as well as their analysis.
