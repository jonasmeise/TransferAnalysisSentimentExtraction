package de.unidue.langtech.bachelor.meise.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.ArrayUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.unidue.langtech.bachelor.meise.files.FileUtils;

public class TSVExporter extends JCasAnnotator_ImplBase{

	int lastSymbolAt = 0;
	int sentenceCounter = 1;
	String header = "#FORMAT=WebAnno TSV 3.2\r\n" + 
			"#T_SP=de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS|PosValue\r\n" + 
			"#T_SP=webanno.custom.AspectRating|Aspect|ROLE_webanno.custom.AspectRating:OwnAspect_webanno.custom.AspectRatingOwnAspectLink|webanno.custom.AspectRating\r\n" + 
			"#T_RL=de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency|DependencyType|flavor|BT_de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS\r\n" + 
			"#T_RL=webanno.custom.Valence|ValenceRating|BT_webanno.custom.AspectRating";
	
	public static final String PARAM_OUTPUT_PATH = "outputPath";
    @ConfigurationParameter(name = PARAM_OUTPUT_PATH, mandatory = true)
    public String outputPath;
    public FileUtils fu;
	
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
    	super.initialize(aContext);
    	fu = new FileUtils();
    }
    
    public TSVExporter() {
    	fu = new FileUtils();
    }
    
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		generateTVS(aJCas);
	}

	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		fu.close();
	}
	
	public void generateTSVwithDependencies(Sentence sentence, ArrayList<ArrayList<String>> dataDependencies) {
		//data-dependencies-Format:
		//1. sentence.getCoveredText()
		//2-n. Feature values
		//n+1 (last). Prediction Values [Name - Value]
		
		
		double minValue = 0.07;
		
		String outputString = "";
		String sentenceString = sentence.getCoveredText();
		int innerCounter=1;
		String[] dependencyAttributes = new String[5];
		int i=0;
		
		if(fu.fileWriter==null) {
			try {
				fu.createWriter(outputPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(sentenceString!=null) { //???
			
			Collection<Dependency> dependencies = JCasUtil.selectCovered(Dependency.class, sentence);
			int[] tokenLocations = new int[dependencies.size()+1];
			
			outputString = "\n#Text=" + sentenceString;
			
			for(Token token : JCasUtil.selectCovered(Token.class, sentence)) {
				tokenLocations[i] = token.getBegin();
				i++;
			}
			
			for(Dependency dependency : dependencies) {
				dependencyAttributes[0] = "";
				dependencyAttributes[1] = "";
				dependencyAttributes[2] = "";
				dependencyAttributes[3] = "";
				dependencyAttributes[4] = "";
				
				//look up dependencyAttributes
				for(ArrayList<String> dependencyDataLine : dataDependencies) {
					int startPredictionValues;
					
					if(dependencyDataLine.get(0).compareTo(sentenceString)==0) {
						
						//remove side string issues
						String[] splitLine = dependencyDataLine.get(1).split(" ");
						splitLine[0] = splitLine[0].substring(4);
						splitLine[1] = splitLine[1].substring(1,splitLine[1].length()-3);
						
						//String mainToken = splitLine[0].compareTo(dependency.getCoveredText())==0 ? splitLine[0] : splitLine[1];
						//String secondaryToken = splitLine[0].compareTo(dependency.getCoveredText())==0 ? splitLine[1] : splitLine[0];
						
						String mainToken = splitLine[0];
						String secondaryToken = splitLine[1];
						
						if(mainToken.compareTo(dependency.getCoveredText())==0) {
							//check for highest non-NONE prediction
							
							//find Prediction-values
							for(startPredictionValues=6;startPredictionValues<dependencyDataLine.size();startPredictionValues++) {
								if(dependencyDataLine.get(startPredictionValues).split(" ").length>1) {
									break;
								}
							}
							
							for(int x=startPredictionValues;x<dependencyDataLine.size();x++) {
								String[] predictionSplit = dependencyDataLine.get(x).split(" ");
								Double compareRatio = Double.valueOf(predictionSplit[1]);
								int secondaryTokenPos = 0;
								
								if(compareRatio > minValue && predictionSplit[0].compareTo("NONE")!=0) {
									
									System.out.println(predictionSplit[0] + ": " + compareRatio + " >>> " + minValue);
									
									//find secondary Token pos
									for(Token token : JCasUtil.selectCovered(Token.class, sentence)) {
										if(token.getCoveredText().compareTo(secondaryToken)==0) {
											secondaryTokenPos = getNumberOfToken(tokenLocations, token.getBegin());
											break;
										}
									}
									
									//case: same-token reference
									if(innerCounter==secondaryTokenPos) {
										dependencyAttributes[0] = predictionSplit[0].split("-")[0];
										dependencyAttributes[1] = predictionSplit[0].split("-")[1];
										dependencyAttributes[2] = sentenceCounter + "-" + secondaryTokenPos;
									} else {
										dependencyAttributes[0] = predictionSplit[0].split("-")[0];
										dependencyAttributes[3] = dependencyAttributes[3] + "|" + predictionSplit[0].split("-")[1];
										dependencyAttributes[4] = dependencyAttributes[4] + "|" + sentenceCounter + "-" + secondaryTokenPos;
									}
								}
							}
						}
					}
				}
				
				for(int x2=0;x2<dependencyAttributes.length;x2++) {
					if(dependencyAttributes[x2].length()==0 && x2<3) {
						dependencyAttributes[x2] = "_";
					} else if(dependencyAttributes[x2].length()==0) {
						dependencyAttributes[x2] = "__";
					}
				}	
				
	        	outputString = outputString + "\n" + sentenceCounter + "-" + innerCounter + "\t" +
	        			(lastSymbolAt + dependency.getBegin()) + "-" + (lastSymbolAt + dependency.getEnd()) + "\t" + 
	        			dependency.getCoveredText() + "\t" + 
	        			dependency.getDependent().getPos().getPosValue() + "\t" +		
	        			dependencyAttributes[0] + "\t" + dependencyAttributes[1] + "\t" + dependencyAttributes[2] + "\t" + 
	        			dependency.getDependencyType().toLowerCase() + "\t" + 
	        			"*\t" + sentenceCounter + "-" + getNumberOfToken(tokenLocations, dependency.getGovernor().getBegin()) + "\t" + 
	        			dependencyAttributes[3].substring(1) + "\t" + dependencyAttributes[4].substring(1);
	        	innerCounter++;
	        }
			
			if(sentenceCounter==1) {
				outputString = header + "\n\n" + outputString;
			}
			
			System.out.println(outputString);
			fu.write(outputString);
			
			lastSymbolAt += sentenceString.length() + 1;
			sentenceCounter++;		
		}
	}
	
	private void generateTVS(JCas aJCas) throws AnalysisEngineProcessException {
		//System.out.println(aJCas.getDocumentText());
		String outputString = "";
		String sentence = aJCas.getDocumentText();
		int innerCounter=1;
		int i=0;
		
		if(fu.fileWriter==null) {
			try {
				fu.createWriter(outputPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Collection<Dependency> dependencies = JCasUtil.select(aJCas, Dependency.class);
		int[] tokenLocations = new int[dependencies.size()+1];
		
		outputString = "\n#Text=" + sentence;
		
		for(Token token : JCasUtil.select(aJCas, Token.class)) {
			tokenLocations[i] = token.getBegin();
			i++;
		}
		
		for(Dependency dependency : dependencies) {
        	outputString = outputString + "\n" + sentenceCounter + "-" + innerCounter + "\t" +
        			(lastSymbolAt + dependency.getBegin()) + "-" + (lastSymbolAt + dependency.getEnd()) + "\t" + 
        			dependency.getCoveredText() + "\t" + 
        			dependency.getDependent().getPos().getPosValue() + "\t" +		
        			"_\t_\t_\t" + 
        			dependency.getDependencyType().toLowerCase() + "\t" + 
        			"_\t" + sentenceCounter + "-" + getNumberOfToken(tokenLocations, dependency.getGovernor().getBegin()) + "\t" + 
        			"_\t_\t";
        	innerCounter++;
        }
		
		if(sentenceCounter==1) {
			outputString = header + "\n\n" + outputString;
		}
		
		System.out.println(outputString);
		fu.write(outputString);
		
		lastSymbolAt += sentence.length() + 1;
		sentenceCounter++;
	}
	
	public int getNumberOfToken(ArrayList<Token> sentence, String toSearch) {
		int i=1;
		for(Token token : sentence) {
			if(token.getCoveredText().equals(toSearch)) {
				return i;
			}
			i++;
		}
		
		return -1;
	}
	
	public int getNumberOfToken(int[] list, int begin) {
		for(int i=0;i<list.length;i++) {
			if(list[i]==begin) {
				return i+1;
			}
		}
		
		return 0;
	}
}
