package de.unidue.langtech.bachelor.meise.pipeline;

import java.util.ArrayList;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import org.dkpro.statistics.agreement.coding.CohenKappaAgreement;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import webanno.custom.Valence;

public class AnnotationStudy extends JCasAnnotator_ImplBase{

	public ArrayList<String> arrayListA;
	public ArrayList<String> arrayListB;
	public int counter = 0;
	
	boolean annotatorA = true;
	
	 public void initialize(UimaContext aContext) throws ResourceInitializationException {
	    	super.initialize(aContext);
	    	
	    	arrayListA = new ArrayList<String>();
	    	arrayListB = new ArrayList<String>();
	 }
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		for(Sentence sentence : JCasUtil.select(aJCas, Sentence.class)) {
			String valences = "";
			
			for(Valence valence :  JCasUtil.selectCovered(Valence.class, sentence)) {
				if(valence.getGovernor()!=null && valence.getDependent()!=null && valence.getValenceRating()!=null) {					
					String toAdd =  valence.getValenceRating() + "_" + valence.getGovernor().getAspect();
					
					if(!valences.contains(toAdd)) {
						valences = valences + toAdd + "   ";
					}
 				}
			}
			
			if(annotatorA) {			
				arrayListA.add(valences);
			} else {
				arrayListB.add(valences);
			}
			
			counter++;
		}
	}
	
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		
		
		System.out.println("total:" + counter);
		counter = counter / 2;
		
		for(int i=0;i<counter;i++) {
			arrayListB.add(arrayListA.remove(counter));
		}
		
		System.out.println("List A: " + arrayListA.size() + " entries.");
		System.out.println("List B: " + arrayListB.size() + " entries.");
		
		CodingAnnotationStudy study = new CodingAnnotationStudy(2);

		for(int i=0;i<200;i++) {
			System.out.println(arrayListA.get(i) + "\n" + arrayListB.get(i));
			String[] splitA = arrayListA.get(i).split("   ");
			String[] splitB = arrayListB.get(i).split("   ");
			
			for(String splitString : splitA) {
				boolean contains=false;
				
				if(!splitString.equals("")) {
					for(int n=0;n<splitB.length;n++) {
						String subSplit = splitB[n];
						if(splitString.equals(subSplit)) {
							contains=true;
							splitB[n] = "???";
						}
					}
					
					if(contains) {
						study.addItem(splitString, splitString);
						System.out.println(splitString + "-" + splitString);
					} else {
						study.addItem(splitString, "");
						System.out.println(splitString + "-");
					}
				}
			}
			
			for(String splitString : splitB) {
				if(!splitString.equals("???") && !splitString.equals("") && splitString.length()>10) {
					study.addItem("", splitString);
					System.out.println("-" + splitString);
				}
			}
		}
		
		CohenKappaAgreement kappa = new CohenKappaAgreement(study);
		System.out.println(kappa.calculateAgreement());
		System.out.println(kappa.calculateExpectedAgreement());
		System.out.println(kappa.calculateMaximumAgreement());
	}	
}
