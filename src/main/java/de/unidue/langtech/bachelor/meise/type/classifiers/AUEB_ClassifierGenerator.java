package de.unidue.langtech.bachelor.meise.type.classifiers;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.unidue.langtech.bachelor.meise.files.RawJsonReviewReader;
import de.unidue.langtech.bachelor.meise.type.ArffGenerator;
import de.unidue.langtech.bachelor.meise.type.ReviewData;
import de.unidue.langtech.bachelor.meise.type.SentimentLexicon;
import de.unidue.langtech.bachelor.meise.type.Tree;
import webanno.custom.AspectRating;
import webanno.custom.Valence;

//BASED ON THIS MODEL (ASPECT CATEGORY DETECTION, 2.1) DESCRIBED IN THIS PAPER:
//http://aclweb.org/anthology/S/S16/S16-1050.pdf
//AUEB-ABSA at SemEval-2016 Task 5: Ensembles of Classifiers and
//Embeddings for Aspect Based Sentiment Analysis
//Dionysios Xenos, Panagiotis Theodorakakos, John Pavlopoulos,
//Prodromos Malakasiotis and Ion Androutsopoulos
//Classification type: SVM
//GridSearch was not performed, Meta-learning was realised with CVParameterSelection instead
//Stemming -> Lemmatizing
//	-> TF-IDF transformation while learning
//unconstrained version is not included


public class AUEB_ClassifierGenerator extends ArffGenerator{

	int cutoff = 200; //Maximale Saetze/Datensatz
	int dataCutoff = 0; //Maximale Datenentries pro Datensatz
	int valueId=0;
	
	String regexIgnore = "[\'\"]";
	ArrayList<String> tagWordsForAspects;
	
	ArrayList<ArrayList<String>> sortedLines = new ArrayList<ArrayList<String>>();
	
	@Override
	public Collection<ArrayList<String>> generateFeaturesFromCas(Sentence sentence) {
		ArrayList<ArrayList<String>> returnList = new ArrayList<ArrayList<String>>();
		Collection<ArrayList<Token>> subSentences = new ArrayList<ArrayList<Token>>();
		
		if(valueId < dataCutoff || dataCutoff==0) { //check if max amount of data is enabled

			if(useOldData) {
				Collection<Dependency> dependencies =  selectCovered(Dependency.class, sentence);
				
	        	ArrayList<Token> roots = new ArrayList<Token>();
	        	for(Dependency dpElement : dependencies) {
					if(dpElement.getDependencyType().toLowerCase().compareTo("root") == 0) {
						roots.add(dpElement.getGovernor());
					}
				}
	        	
	        	int currentDataId = myReader.getReviewId(sentence.getCoveredText());
	        	ReviewData currentData = myReader.getReview(currentDataId);
	        	
	        	myLog.log(sentence.getCoveredText() + " -> (" + currentDataId + "): " + currentData.getTextContent());
	        	
	        	ArrayList<String> categories = currentData.getOpinionCategory();

	        	for(String singleClass : allClassAttributes) {
	        		ArrayList<String> singleLine = new ArrayList<String>();
	        		
	        		singleLine.add("" + valueId);
	        		
	        		String uni = "";
	        		String bi = "";
	        		String uni_lemma = "";
	        		String bi_lemma = "";
	        		String bi_pos = "";

	        		Collection<Token> currentSentence =  selectCovered(Token.class, sentence);
	        		Token oldToken = null;
	        		
	        		for(Token singleToken : currentSentence) {
	        			uni = uni + singleToken.getCoveredText() + " ";
	        			uni_lemma = uni_lemma + singleToken.getLemma().getValue() + " ";
	        			
	        			if(oldToken!=null) {
	        				bi = bi + oldToken.getCoveredText() + "_" + singleToken.getCoveredText() + " "; 
	        				bi_lemma = bi_lemma + oldToken.getLemma().getValue() + "_" + singleToken.getLemma().getValue() + " "; 
	        				bi_pos = bi_pos + oldToken.getPos().getPosValue() + "_" + singleToken.getPos().getPosValue() + " ";
	        				
	        				oldToken = singleToken;
	        			}
	        			
	        			if(oldToken==null) {
	        				oldToken = singleToken;
	        			}
	        		}

	        		singleLine.add("'" + uni.replaceAll(regexIgnore, "") + "'");
	        		singleLine.add("'" + uni_lemma.replaceAll(regexIgnore, "") +"'");
	        		singleLine.add("'" + bi.replaceAll(regexIgnore, "") +"'");
	        		singleLine.add("'" + bi_lemma.replaceAll(regexIgnore, "") +"'");
	        		singleLine.add("'" + bi_pos.replaceAll(regexIgnore, "") +"'");
	        		
	        		singleLine.add(singleClass);
	        		
	        		boolean containsTarget=false;
	        		for(String target : categories) {
	        			if(target.toLowerCase().equals(singleClass)) {
	        				containsTarget=true;
	        			}
	        		}
	        		
	        		singleLine.add(containsTarget ? "true" : "false");
	        		
	        		sortedLines.add(singleLine);
	        		returnList.add(singleLine);
	        		
	        		valueId++;
	        	}

			} else {
	        	Collection<Dependency> dependencies =  selectCovered(Dependency.class, sentence);
	        	Collection<Tree<Token>> treeCollection = new ArrayList<Tree<Token>>();
				
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
		        	
				for(Valence singleValence : selectCovered(Valence.class, sentence)) {
	        		Token t1;
	        		Token t2;
	        		AspectRating ar;
	        		String identifier;
					
					if(singleValence.getDependent()!=null && singleValence.getDependent().getAspect()!=null && singleValence.getGovernor()!=null && singleValence.getGovernor().getAspect()!=null && singleValence.getValenceRating()!="null" && singleValence.getValenceRating()!=null) {
		        		if(singleValence.getDependent().getAspect().toLowerCase().equals("ratingofaspect")) {
	        				t1 = JCasUtil.selectCovered(Token.class, singleValence.getGovernor()).get(0);
	        				identifier = singleValence.getGovernor().getAspect().replaceAll("[^\\x00-\\x7F]", "");
	        				ar = singleValence.getGovernor();
		        		} else {
		        			t1 = JCasUtil.selectCovered(Token.class, singleValence.getDependent()).get(0);
		        			identifier = singleValence.getDependent().getAspect().replaceAll("[^\\x00-\\x7F]", "");
		        			ar = singleValence.getDependent();
		        		}
		        		
						for(String singleClass : allClassAttributes) {
			        		ArrayList<String> singleLine = new ArrayList<String>();
			        		
			        		singleLine.add("" + valueId);
			        		
			        		String uni = "";
			        		String bi = "";
			        		String uni_lemma = "";
			        		String bi_lemma = "";
			        		String bi_pos = "";

			        		Collection<Token> currentSentence =  selectCovered(Token.class, sentence);
			        		Token oldToken = null;
			        		
			        		for(Token singleToken : currentSentence) {
			        			uni = uni + singleToken.getCoveredText() + " ";
			        			uni_lemma = uni_lemma + singleToken.getLemma().getValue() + " ";
			        			
			        			if(oldToken!=null) {
			        				bi = bi + oldToken.getCoveredText() + "_" + singleToken.getCoveredText() + " "; 
			        				bi_lemma = bi_lemma + oldToken.getLemma().getValue() + "_" + singleToken.getLemma().getValue() + " "; 
			        				bi_pos = bi_pos + oldToken.getPos().getPosValue() + "_" + singleToken.getPos().getPosValue() + " ";
			        				
			        				oldToken = singleToken;
			        			}
			        			
			        			if(oldToken==null) {
			        				oldToken = singleToken;
			        			}
			        		}

			        		singleLine.add("'" + uni.replaceAll(regexIgnore, "") + "'");
			        		singleLine.add("'" + uni_lemma.replaceAll(regexIgnore, "") +"'");
			        		singleLine.add("'" + bi.replaceAll(regexIgnore, "") +"'");
			        		singleLine.add("'" + bi_lemma.replaceAll(regexIgnore, "") +"'");
			        		singleLine.add("'" + bi_pos.replaceAll(regexIgnore, "") +"'");
			        		
			        		singleLine.add(singleClass);
			        		
			        		boolean containsTarget=false;
			        		
		        			if(identifier.toLowerCase().equals(singleClass)) {
		        				containsTarget=true;
		        			}
			        		
			        		singleLine.add(containsTarget ? "true" : "false");
			        		
			        		sortedLines.add(singleLine);
			        		returnList.add(singleLine);
			        		
			        		valueId++;
			        	}
					}
				}
			}
		}       
		
		//since we don't need duplicates for every class...
		if(learningModeActivated) {
			int onlyTakeFirst = subSentences.size();
			ArrayList<ArrayList<String>> alternativeReturnList = new ArrayList<ArrayList<String>>();
			
			for(int i=0;i<onlyTakeFirst;i++) {
				alternativeReturnList.add(returnList.get(i));
			}
			
			return alternativeReturnList;
		}
		
		return returnList;
	}

	@Override
	public ArrayList<ArrayList<String>> generateRelations() {
		String[] types = new String[8];
		if(!useOldData) {
			types[0] = "Ausstattung";
			types[1] = "Hotelpersonal";
			types[2] = "Lage";
			types[3] = "OTHER";
			types[4] = "Komfort";
			types[5] = "Preis-Leistungs-Verhltnis";
			types[6] = "WLAN";
			types[7] = "Sauberkeit";
		} else {
			//load data separately for cross-checking
			myReader = new RawJsonReviewReader();
			myReader.folderPath = "src\\main\\resources\\SEABSA16_data";
			myReader.useOldData = true;
			
			if(!isTestData ) {
				myReader.fileExtension = ".xml";
			} else {
				myReader.fileExtension = ".gold";
			}
			myReader.external_Initialize(-1);
			
			types = getAspectLabelsOldData();
		}
		for(int i=0;i<types.length;i++) {
			ArrayList<String> relations = new ArrayList<String>();
			
			relations.add("id numeric");
			
			relations.add("unigram string");
			relations.add("unigram_lemma string");
			relations.add("bigram string");
			relations.add("bigram_lemma string");
			relations.add("pos_bigrams string");

			relations.add("type string");
			
			relations.add("aspecttype " + generateTupel(new String[] {"true", "false"}));	
			
			allClassAttributes.add(types[i]);
			
			this.relations.add(relations);
			
			
			ignoreFeatures = new int[1];
			identifierAttributeAt=relations.size()-2;
			ignoreFeatures[0]=identifierAttributeAt;
		}
		
		return this.relations;
	}

	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		writeOutput(sortedLines);
	}	
	
	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
		generateData(arg0, cutoff);
	}
}
