

/* First created by JCasGen Fri Dec 14 23:20:03 CET 2018 */
package de.tudarmstadt.ukp.dkpro.core.api.coref.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Sat Dec 15 00:23:11 CET 2018
 * XML source: C:/Users/Jonas/Downloads/de.unidue.langtech.bachelor.meise/de.unidue.langtech.bachelor.meise/typesystem.xml
 * @generated */
public class CoreferenceLink extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(CoreferenceLink.class);
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
  protected CoreferenceLink() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public CoreferenceLink(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public CoreferenceLink(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public CoreferenceLink(JCas jcas, int begin, int end) {
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
  //* Feature: next

  /** getter for next - gets 
   * @generated
   * @return value of the feature 
   */
  public CoreferenceLink getNext() {
    if (CoreferenceLink_Type.featOkTst && ((CoreferenceLink_Type)jcasType).casFeat_next == null)
      jcasType.jcas.throwFeatMissing("next", "de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink");
    return (CoreferenceLink)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((CoreferenceLink_Type)jcasType).casFeatCode_next)));}
    
  /** setter for next - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setNext(CoreferenceLink v) {
    if (CoreferenceLink_Type.featOkTst && ((CoreferenceLink_Type)jcasType).casFeat_next == null)
      jcasType.jcas.throwFeatMissing("next", "de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink");
    jcasType.ll_cas.ll_setRefValue(addr, ((CoreferenceLink_Type)jcasType).casFeatCode_next, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: referenceType

  /** getter for referenceType - gets 
   * @generated
   * @return value of the feature 
   */
  public String getReferenceType() {
    if (CoreferenceLink_Type.featOkTst && ((CoreferenceLink_Type)jcasType).casFeat_referenceType == null)
      jcasType.jcas.throwFeatMissing("referenceType", "de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink");
    return jcasType.ll_cas.ll_getStringValue(addr, ((CoreferenceLink_Type)jcasType).casFeatCode_referenceType);}
    
  /** setter for referenceType - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setReferenceType(String v) {
    if (CoreferenceLink_Type.featOkTst && ((CoreferenceLink_Type)jcasType).casFeat_referenceType == null)
      jcasType.jcas.throwFeatMissing("referenceType", "de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink");
    jcasType.ll_cas.ll_setStringValue(addr, ((CoreferenceLink_Type)jcasType).casFeatCode_referenceType, v);}    
   
    
  //*--------------*
  //* Feature: referenceRelation

  /** getter for referenceRelation - gets 
   * @generated
   * @return value of the feature 
   */
  public String getReferenceRelation() {
    if (CoreferenceLink_Type.featOkTst && ((CoreferenceLink_Type)jcasType).casFeat_referenceRelation == null)
      jcasType.jcas.throwFeatMissing("referenceRelation", "de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink");
    return jcasType.ll_cas.ll_getStringValue(addr, ((CoreferenceLink_Type)jcasType).casFeatCode_referenceRelation);}
    
  /** setter for referenceRelation - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setReferenceRelation(String v) {
    if (CoreferenceLink_Type.featOkTst && ((CoreferenceLink_Type)jcasType).casFeat_referenceRelation == null)
      jcasType.jcas.throwFeatMissing("referenceRelation", "de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink");
    jcasType.ll_cas.ll_setStringValue(addr, ((CoreferenceLink_Type)jcasType).casFeatCode_referenceRelation, v);}    
  }

    