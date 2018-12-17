package de.unidue.langtech.bachelor.meise.pipeline;

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class TestReader extends JCasAnnotator_ImplBase{
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		printTokens(aJCas);
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
