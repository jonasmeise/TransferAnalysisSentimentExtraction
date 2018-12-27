package de.unidue.langtech.bachelor.meise.classifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.uima.fit.factory.JCasFactory;
import de.unidue.langtech.bachelor.meise.files.FileUtils;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.stemmers.LovinsStemmer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class AspectClassifier {
	//a single Aspect Classifier for a set of Instances
	
	String sourcePath;
	FilteredClassifier classifier;
	Instances instances;
	Evaluation evaluation;
	FileUtils fu;
	String fileType = ".arff";
	int seed;
	int folds = 10;
	
	public AspectClassifier() {
		fu = new FileUtils();
		sourcePath = "";
		instances = null;
	}
	
	public Classifier getClassifier() {
		return classifier;
	}

	public void setClassifier(FilteredClassifier classifier) {
		this.classifier = classifier;
	}

	public AspectClassifier(String sourcePath) {
		this();
		this.sourcePath = sourcePath;
	}
	
	public static void main(String[] args) {
		AspectClassifier ac = new AspectClassifier();
		ac.run();
	}
	
	public void learn(Instances data) throws Exception {
		 Random rand = new Random(seed);   // create seeded number generator
		 Instances randData = new Instances(data);   // create copy of original data
		 randData.randomize(rand);         // randomize data with number generator
		 
		 for (int n = 0; n < folds; n++) {
			   Instances train = randData.trainCV(folds, n, rand);
			   Instances test = randData.testCV(folds, n);

			   // further processing, classification, etc.
				LibSVM svm = new LibSVM();
				
				StringToWordVector s2wFilter;
				Remove removeFilter = new Remove();
				removeFilter.setAttributeIndicesArray(new int[]{1, 3});
				
				Instances newData = null;
				try {
					removeFilter.setInputFormat(train);
					newData = Filter.useFilter(train, removeFilter);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				s2wFilter = new StringToWordVector(); 
				s2wFilter.setAttributeIndices("1");
				s2wFilter.setIDFTransform(true);
				s2wFilter.setLowerCaseTokens(true);

				svm.setKernelType(new SelectedTag(0, LibSVM.TAGS_KERNELTYPE));
				svm.setSVMType(new SelectedTag(0, LibSVM.TAGS_SVMTYPE));
				svm.setProbabilityEstimates(true);
				
				classifier.setFilter(s2wFilter); 
				classifier.setClassifier(svm);
				classifier.buildClassifier(newData);
				
				evaluation = evalModel(classifier, test, 10, new Random());
				
				System.out.println(evaluation.toSummaryString());

			 }
	}
	
	public void run() {
		try {
			instances = getData(sourcePath, fileType, true, 1);
			learn(instances);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	   /** Erzeugt ein Evaluation Objekt, mit dem der Klassifikator auf die gegebenen Daten angewendet wird.
    *
    * @param classifier Der Klassifikator
    * @param data Die Daten
    * @param numberIterations Die Anzahl der Unterteilungen des Datensatzes waehrend der Kreuzvalidierung
    * @param randData Ein Zufallszahlengenerator
    * @return Das Evaluation Objekt
    */
   private Evaluation evalModel(Classifier classifier, Instances data, Integer numberIterations, Random randData ) throws Exception {
       Evaluation eval = new Evaluation(data);
       eval.crossValidateModel(classifier, data, numberIterations, randData);
       return eval;
   }

   /** Liest aus einer ARFF Datei Daten mit Attribut- und Datenbeschreibungen
    *
    * @param filename Pfad und Name der Datei
    * @param posClass 1-basierter Index der Klassendefinition vom Ende der Attributliste aus gesehen.
    * @return Ein Instances Objekt
 * @throws URISyntaxException 
 * @throws IOException 
    */
   
   private Instances getData( String folderName, String fileType, boolean includeSubfolders, Integer posClass) throws IOException, URISyntaxException {
	   Instances returnInstances = null;
	   
	   ArrayList<String> fileList  = fu.getFilesInFolder(folderName, fileType, includeSubfolders);
	   
	   for(String filePath : fileList) {
		   if(returnInstances==null) {
			   returnInstances = getData(filePath, posClass);
		   } else {
			   returnInstances.addAll(getData(filePath, posClass));
		   }
	   }
	   
	   return returnInstances;
   }
   
   private Instances getData(String fileName, Integer posClass ) throws IOException, URISyntaxException {
       File file = new File(fileName);
       BufferedReader inputReader = new BufferedReader(new FileReader(file));
       Instances data = new Instances(inputReader);
       data.setClassIndex(data.numAttributes() - posClass);

       return data;
   }
}
