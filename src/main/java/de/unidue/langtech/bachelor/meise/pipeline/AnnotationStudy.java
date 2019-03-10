package de.unidue.langtech.bachelor.meise.pipeline;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import org.dkpro.statistics.agreement.coding.CohenKappaAgreement;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.unidue.langtech.bachelor.meise.type.Tree;
import webanno.custom.AspectRating;
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
			
			Collection<ArrayList<Token>> subSentences = new ArrayList<ArrayList<Token>>();
			Collection<Dependency> dependencies =  selectCovered(Dependency.class, sentence);
        	ArrayList<Tree<Token>> treeCollection = new ArrayList<Tree<Token>>();
				
        	ArrayList<Token> roots = new ArrayList<Token>();
        	for(Dependency dpElement : dependencies) {
				if(dpElement.getDependencyType().toLowerCase().compareTo("root") == 0) {
					roots.add(dpElement.getGovernor());
				}
			}
        	
        	for(Token root : roots) {
	        	Tree<Token> tree = new Tree<Token>(root);
	        	tree.generateTreeOfDependency(dependencies, root);
	        	tree.setParentDependencyType("ROOT");
	        	
	        	treeCollection.add(tree);
	        	subSentences.add(tree.getAllObjects());	
        	}
        	
        	int currentCounter=0;
        	
        	for(ArrayList<Token> subSentence : subSentences) {		
        		valences = "";
				for(Valence valence :  JCasUtil.selectCovered(Valence.class, sentence)) {
					if(valence.getGovernor()!=null && valence.getDependent()!=null && valence.getValenceRating()!=null) {		
						AspectRating t1 = valence.getDependent();
		        		AspectRating t2 = valence.getGovernor();
		        		
		    			int dependencyDistance = treeCollection.get(currentCounter).tokenDistanceInTree(selectCovered(Token.class, t1).get(0),selectCovered(Token.class, t2).get(0));
								
						if(dependencyDistance>=0) {
							AspectRating identifier=null;
							
							if(t1.getAspect()!=null && t2.getAspect()!=null) {
								if(t1.getAspect().toLowerCase().compareTo("ratingofaspect")==0) {
			    					//non-negated
									identifier = t2;
			    				} else {
			    					//negated
			    					identifier = t1;
			    				}
							
								if(identifier!=null) {
									String toAdd =  valence.getValenceRating() + "_" + identifier.getAspect() + "_" + identifier.getCoveredText();
									
									if(!valences.contains(toAdd)) {
										valences = valences + toAdd + "   ";
									}
								}
							}
						}
	 				}
				}	
				
			arrayListA.add(valences);
			currentCounter++;
			counter++;
				
        	}
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
		
		/*for(int i=0;i<200;i++) {
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
						System.out.println(splitString + "-null");
					}
				}
			}
			
			for(String splitString : splitB) {
				if(!splitString.equals("???") && !splitString.equals("") && splitString.length()>10) {
					study.addItem("", splitString);
					System.out.println("null-" + splitString);
				}
			}*/
			
		int i=0;
		while(i < arrayListA.size() && i < arrayListB.size()) {
			System.out.println("Annotator 1:" + arrayListA.get(i) + "\n" + "Annotator 2:" + arrayListB.get(i) + "\n -----------------");
			study.addItem(arrayListA.get(i), arrayListB.get(i));
			i++;
		}
		
		CohenKappaAgreement kappa = new CohenKappaAgreement(study);
		System.out.println("Cohen's Kappa: " + kappa.calculateAgreement());
	}	
}
