package de.unidue.langtech.bachelor.meise.type.classifiers;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.unidue.langtech.bachelor.meise.sentimentlexicon.*;
import de.unidue.langtech.bachelor.meise.type.ArffGenerator;
import de.unidue.langtech.bachelor.meise.type.SentimentLexicon;
import de.unidue.langtech.bachelor.meise.type.StopwordHandler;
import de.unidue.langtech.bachelor.meise.type.Tree;
import webanno.custom.AspectRating;
import webanno.custom.Valence;

public class OwnClassifier_ClassifierGenerator3 extends ArffGenerator{
	
	int cutoff = 200; //Maximale Saetze/Datensatz
	int dataCutoff = 0; //Maximale Datenentries pro Datensatz
	int valueId=0;
	
	String regexIgnore = "[\'\"]";
	ArrayList<String> negationWords;
	ArrayList<SentimentLexicon> sentimentLexicons;
	StopwordHandler myStopwordHandler;
	
	ArrayList<ArrayList<String>> sortedLines = new ArrayList<ArrayList<String>>();
	
	@Override
	public Collection<ArrayList<String>> generateFeaturesFromCas(Sentence sentence) {
		ArrayList<ArrayList<String>> returnList = new ArrayList<ArrayList<String>>();
		Collection<ArrayList<Token>> subSentences = new ArrayList<ArrayList<Token>>();
		
		if(valueId < dataCutoff || dataCutoff==0) { //check if max amount of data is enabled

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
	        	
        	
        	subSentences = divideIntoSubSentences(sentence, treeCollection);
        	//generate all outputs, then overwrite specific ones
			for(Valence valence : selectCovered(Valence.class, sentence)) {
        		AspectRating t1 = valence.getDependent();
        		AspectRating t2 = valence.getGovernor();
        		
				int dependencyDistance = -1;
				
				for(Tree<Token> tree : treeCollection) {
    				int newDistance = tree.tokenDistanceInTree(selectCovered(Token.class, t1).get(0),selectCovered(Token.class, t2).get(0));
    				
    				if(newDistance > dependencyDistance) {
    					dependencyDistance = newDistance;
    				}
    			}
				
				if(dependencyDistance>=0) {
					String wordA = selectCovered(Token.class, t1).get(0).getLemma().getValue();
					String wordB = selectCovered(Token.class, t2).get(0).getLemma().getValue();
					double lexicons_min=0, lexicons_avg=0, lexicons_max=0;
					int word_distance=0, context_negation=0, context_lexicon_avg=0, sentence_length=0;
					
					ArrayList<Token> currentSentence = new ArrayList<Token>();
	        		for(ArrayList<Token> subSentence : subSentences) {
	        			for(Token singleToken : subSentence) {
	        				if(singleToken.equals(selectCovered(Token.class, t1).get(0)) || singleToken.equals(selectCovered(Token.class, t2).get(0))) {
	        					currentSentence = subSentence;
	        				}
	        			}
	        		}
	        		
	        		String currentSentenceString = "";
	        		for(Token singleToken : currentSentence) {
	        			currentSentenceString = currentSentenceString + singleToken.getCoveredText() + " ";
	        		}
	        		
	        		ArrayList<Token> searchTokens = new ArrayList<Token>();
	        		searchTokens.add(selectCovered(Token.class, t1).get(0));
	        		searchTokens.add(selectCovered(Token.class, t2).get(0));
	        		
	        		ArrayList<Token> contextTokens = getContext(sentence, treeCollection, 3, 1000, searchTokens);
	        		
	        		for(SentimentLexicon singleLexi : sentimentLexicons) {
	        			double currentValue = singleLexi.fetchPolarity(wordA, new String[] {"positive"})-singleLexi.fetchPolarity(wordA, new String[] {"negative"});
	        			double currentValue2 = singleLexi.fetchPolarity(wordB, new String[] {"positive"})-singleLexi.fetchPolarity(wordB, new String[] {"negative"});
	        			
	        			if(currentValue>lexicons_max) {
	        				lexicons_max = currentValue;
	        			}
	        			if(currentValue2>lexicons_max) {
	        				lexicons_max = currentValue2;
	        			}
	        			if(currentValue<lexicons_min) {
	        				lexicons_min = currentValue;
	        			}
	        			if(currentValue2<lexicons_min) {
	        				lexicons_min = currentValue2;
	        			}
      			
	        			lexicons_avg += currentValue + currentValue2;
	        		}
	        		
	        		lexicons_avg = (lexicons_avg/(sentimentLexicons.size()*2));
	        		word_distance = Math.abs(t1.getBegin() - t2.getBegin());
	        		
	        		boolean containsNegation=false;
	        		for(Token singleToken : contextTokens) {
	        			for(String negationWord : negationWords) {
	        				if(singleToken.getLemma().getValue().contains(negationWord)) {
	        					containsNegation=true;
	        				}
	        			}
	        			
	        			for(SentimentLexicon singleLexi : sentimentLexicons) {
	        				context_lexicon_avg += singleLexi.fetchPolarity(singleToken.getLemma().getValue(), new String[] {"positive"})-singleLexi.fetchPolarity(singleToken.getLemma().getValue(), new String[] {"negative"});
	        			}
	        		}
	        		
	        		context_negation = containsNegation ? 1 : 0;
	        		context_lexicon_avg = context_lexicon_avg/(sentimentLexicons.size()*contextTokens.size());
	        		sentence_length = currentSentenceString.length()-1;
	        			
					String identifier;
					
					if(t1.getAspect()!=null && t2.getAspect()!=null) {
						if(t1.getAspect().toLowerCase().compareTo("ratingofaspect")==0) {
	    					//non-negated
							identifier = t2.getAspect().replaceAll("[^\\x00-\\x7F]", "");
	    				} else {
	    					//negated
	    					identifier = t1.getAspect().replaceAll("[^\\x00-\\x7F]", "");
	    				}
						
						//find the value in returnList and replace it with the new one
						
						ArrayList<String> returnInstance = new ArrayList<String>();
						returnInstance.add("" + valueId);
						valueId++;
						
						returnInstance.add(wordA);
						returnInstance.add(wordB);
						returnInstance.add(identifier);
						returnInstance.add("" + word_distance);
						
						if(!constrained) {
							returnInstance.add("" + lexicons_min);
							returnInstance.add("" + lexicons_avg);
							returnInstance.add("" + lexicons_max);
						} else {
							returnInstance.add("0");
							returnInstance.add("0");
							returnInstance.add("0");
						}
						returnInstance.add("" + context_negation);
						
						if(!constrained) {
							returnInstance.add("" + context_lexicon_avg);
						} else {
							returnInstance.add("0");
						}

						returnInstance.add("" + sentence_length);
						
						returnInstance.add("valence");
						
						String currentValence=valence.getValenceRating();
						if(valence.getValenceRating()==null || (!valence.getValenceRating().equals("positive") && !valence.getValenceRating().equals("negative"))) {
							currentValence="negative";
						}
						
						returnInstance.add(currentValence);
						sortedLines.add(returnInstance);
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
		myStopwordHandler = new StopwordHandler("src\\main\\resources\\stopwords.txt");	
		
		String[] types = new String[8];
		types[0] = "Ausstattung";
		types[1] = "Hotelpersonal";
		types[2] = "Lage";
		types[3] = "OTHER";
		types[4] = "Komfort";
		types[5] = "Preis-Leistungs-Verhltnis";
		types[6] = "WLAN";
		types[7] = "Sauberkeit";
		
		String[] types2 = new String[1];
		types2[0] = "valence";
		
		negationWords = new ArrayList<String>();
		negationWords.add("no");
		negationWords.add("not");
		negationWords.add("nt");
		negationWords.add("n't");
		negationWords.add("need");
		negationWords.add("must");
		
		sentimentLexicons = new ArrayList<SentimentLexicon>();
		sentimentLexicons.add(new AFINN());
		sentimentLexicons.add(new BingLiu());
		sentimentLexicons.add(new EmoLex());
		
		for(int i=0;i<types2.length;i++) {
			ArrayList<String> relations = new ArrayList<String>();
			
			relations.add("id numeric");
			
			//of NN+, JJ+, VB+, RB
			relations.add("wordA string");
			relations.add("wordB string");
			relations.add("dependency_type string");
			relations.add("word_distance numeric");
			relations.add("lexicons_min numeric");
			relations.add("lexicons_avg numeric");
			relations.add("lexicons_max numeric");
			relations.add("context_negation numeric");
			relations.add("context_lexicon_avg numeric");
			relations.add("sentence_length numeric");
			
			relations.add("String type");
			
			relations.add("aspecttype " + generateTupel(new String[] {"positive", "negative"}));	
			
			allClassAttributes.add(types2[i]);
			
			this.relations.add(relations);
			
			
			ignoreFeatures = new int[1];
			identifierAttributeAt = relations.size()-2;
			ignoreFeatures[0] = identifierAttributeAt;
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
