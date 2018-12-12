package de.unidue.langtech.bachelor.meise.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
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
    private String outputPath;
    private FileUtils fu;
	
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
    	super.initialize(aContext);
    	fu = new FileUtils();
    }
    
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		generateTVS(aJCas);
	}

	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		fu.close();
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
	
	public int getNumberOfToken(int[] list, int begin) {
		for(int i=0;i<list.length;i++) {
			if(list[i]==begin) {
				return i+1;
			}
		}
		
		return 0;
	}
	
	private void printTokens(JCas aJCas)
    {
        Collection<Token> tokens = JCasUtil.select(aJCas, Token.class);
        for(Token token : tokens) {
            System.out.print(token.getCoveredText() + "(" + token.getPos().getPosValue() + ") ");
        }
        
        Collection<Dependency> dependencies = JCasUtil.select(aJCas, Dependency.class);
        for(Dependency dependency : dependencies) {
        	System.out.println(dependency.getBegin() + "-" + dependency.getEnd() + " " + dependency.getCoveredText() + ";" + dependency.getGovernor().getCoveredText() + "->" + dependency.getDependent().getCoveredText() + " (" + dependency.getDependencyType() + ")");
        }
    }
	
}
