package de.unidue.langtech.bachelor.meise.type.classifiers;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unidue.langtech.bachelor.meise.type.ArffGenerator;

public class IHRSD_ClassifierGenerator extends ArffGenerator{

	int cutoff = 200; //Maximale Saetze/Datensatz
	int dataCutoff = 0; //Maximale Datenentries pro Datensatz
	int valueId=0;
	
	String regexIgnore = "[\'\"]";
	
	ArrayList<ArrayList<String>> sortedLines = new ArrayList<ArrayList<String>>();
	
	
	@Override
	public Collection<ArrayList<String>> generateFeaturesFromCas(Sentence sentence) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<ArrayList<String>> generateRelations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
		
	}
	
	public int[] generateHeatmap(ArrayList<Token> sentence) {
		int[] returnArray = new int[81];
		
		return returnArray;
	}

}
