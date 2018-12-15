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
import weka.core.Instances;

public class AspectClassifier {
	
	String sourcePath = "";
	FileUtils fu;
	
	public AspectClassifier() {
		fu = new FileUtils();
	}
	
	public static void main(String[] args) {
		AspectClassifier ac = new AspectClassifier();
		ac.run();
	}
	
	public AspectClassifier(String sourcePath) {
		this();
		this.sourcePath = sourcePath;
	}
	
	public void run() {
		JCasFactory jcasFactory;
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
   
   //TODO: how to import multiple file sources?
   private Instances getData( String folderName, String fileType, boolean includeSubfolders, Integer posClass) throws IOException, URISyntaxException {
	   ArrayList<String> fileList  = fu.getFilesInFolder(folderName, fileType, includeSubfolders);
	   
	   for(String filePath : fileList) {
		   return getData(filePath, posClass);
	   }
	   
	   return null;
   }
   
   private Instances getData(String fileName, Integer posClass ) throws IOException, URISyntaxException {
       File file = new File(fileName);
       BufferedReader inputReader = new BufferedReader(new FileReader(file));
       Instances data = new Instances(inputReader);
       data.setClassIndex(data.numAttributes() - posClass);

       return data;
   }
}
