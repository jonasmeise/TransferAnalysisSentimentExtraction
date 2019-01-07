

/* First created by JCasGen Fri Dec 14 23:20:04 CET 2018 */
package de.tudarmstadt.ukp.dkpro.core.api.segmentation.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Jan 07 22:47:38 CET 2019
 * XML source: C:/Users/Jonas/Downloads/de.unidue.langtech.bachelor.meise/de.unidue.langtech.bachelor.meise/typesystem.xml
 * @generated */
public class Token extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Token.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Token() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Token(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Token(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Token(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: parent

  /** getter for parent - gets 
   * @generated
   * @return value of the feature 
   */
  public Annotation getParent() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_parent == null)
      jcasType.jcas.throwFeatMissing("parent", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    return (Annotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_parent)));}
    
  /** setter for parent - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setParent(Annotation v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_parent == null)
      jcasType.jcas.throwFeatMissing("parent", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_parent, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: lemma

  /** getter for lemma - gets 
   * @generated
   * @return value of the feature 
   */
  public Lemma getLemma() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_lemma == null)
      jcasType.jcas.throwFeatMissing("lemma", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    return (Lemma)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_lemma)));}
    
  /** setter for lemma - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLemma(Lemma v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_lemma == null)
      jcasType.jcas.throwFeatMissing("lemma", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_lemma, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: stem

  /** getter for stem - gets 
   * @generated
   * @return value of the feature 
   */
  public Stem getStem() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_stem == null)
      jcasType.jcas.throwFeatMissing("stem", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    return (Stem)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_stem)));}
    
  /** setter for stem - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setStem(Stem v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_stem == null)
      jcasType.jcas.throwFeatMissing("stem", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_stem, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: pos

  /** getter for pos - gets 
   * @generated
   * @return value of the feature 
   */
  public POS getPos() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_pos == null)
      jcasType.jcas.throwFeatMissing("pos", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    return (POS)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_pos)));}
    
  /** setter for pos - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPos(POS v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_pos == null)
      jcasType.jcas.throwFeatMissing("pos", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_pos, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: morph

  /** getter for morph - gets 
   * @generated
   * @return value of the feature 
   */
  public MorphologicalFeatures getMorph() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_morph == null)
      jcasType.jcas.throwFeatMissing("morph", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    return (MorphologicalFeatures)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_morph)));}
    
  /** setter for morph - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMorph(MorphologicalFeatures v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_morph == null)
      jcasType.jcas.throwFeatMissing("morph", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_morph, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    