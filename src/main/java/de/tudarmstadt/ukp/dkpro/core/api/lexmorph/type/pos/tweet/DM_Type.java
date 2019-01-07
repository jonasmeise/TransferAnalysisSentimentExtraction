
/* First created by JCasGen Fri Dec 14 23:20:03 CET 2018 */
package de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.tweet;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.O_Type;

/** 
 * Updated by JCasGen Mon Jan 07 22:47:37 CET 2019
 * @generated */
public class DM_Type extends O_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = DM.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.tweet.DM");



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public DM_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    