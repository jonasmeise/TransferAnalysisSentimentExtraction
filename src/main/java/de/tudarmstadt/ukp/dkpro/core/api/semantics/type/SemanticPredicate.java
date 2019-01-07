

/* First created by JCasGen Fri Dec 14 23:20:04 CET 2018 */
package de.tudarmstadt.ukp.dkpro.core.api.semantics.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Jan 07 22:47:38 CET 2019
 * XML source: C:/Users/Jonas/Downloads/de.unidue.langtech.bachelor.meise/de.unidue.langtech.bachelor.meise/typesystem.xml
 * @generated */
public class SemanticPredicate extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(SemanticPredicate.class);
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
  protected SemanticPredicate() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public SemanticPredicate(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public SemanticPredicate(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public SemanticPredicate(JCas jcas, int begin, int end) {
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
  //* Feature: category

  /** getter for category - gets 
   * @generated
   * @return value of the feature 
   */
  public String getCategory() {
    if (SemanticPredicate_Type.featOkTst && ((SemanticPredicate_Type)jcasType).casFeat_category == null)
      jcasType.jcas.throwFeatMissing("category", "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SemanticPredicate_Type)jcasType).casFeatCode_category);}
    
  /** setter for category - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setCategory(String v) {
    if (SemanticPredicate_Type.featOkTst && ((SemanticPredicate_Type)jcasType).casFeat_category == null)
      jcasType.jcas.throwFeatMissing("category", "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate");
    jcasType.ll_cas.ll_setStringValue(addr, ((SemanticPredicate_Type)jcasType).casFeatCode_category, v);}    
   
    
  //*--------------*
  //* Feature: arguments

  /** getter for arguments - gets 
   * @generated
   * @return value of the feature 
   */
  public FSArray getArguments() {
    if (SemanticPredicate_Type.featOkTst && ((SemanticPredicate_Type)jcasType).casFeat_arguments == null)
      jcasType.jcas.throwFeatMissing("arguments", "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((SemanticPredicate_Type)jcasType).casFeatCode_arguments)));}
    
  /** setter for arguments - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setArguments(FSArray v) {
    if (SemanticPredicate_Type.featOkTst && ((SemanticPredicate_Type)jcasType).casFeat_arguments == null)
      jcasType.jcas.throwFeatMissing("arguments", "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate");
    jcasType.ll_cas.ll_setRefValue(addr, ((SemanticPredicate_Type)jcasType).casFeatCode_arguments, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for arguments - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public SemanticArgument getArguments(int i) {
    if (SemanticPredicate_Type.featOkTst && ((SemanticPredicate_Type)jcasType).casFeat_arguments == null)
      jcasType.jcas.throwFeatMissing("arguments", "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((SemanticPredicate_Type)jcasType).casFeatCode_arguments), i);
    return (SemanticArgument)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((SemanticPredicate_Type)jcasType).casFeatCode_arguments), i)));}

  /** indexed setter for arguments - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setArguments(int i, SemanticArgument v) { 
    if (SemanticPredicate_Type.featOkTst && ((SemanticPredicate_Type)jcasType).casFeat_arguments == null)
      jcasType.jcas.throwFeatMissing("arguments", "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((SemanticPredicate_Type)jcasType).casFeatCode_arguments), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((SemanticPredicate_Type)jcasType).casFeatCode_arguments), i, jcasType.ll_cas.ll_getFSRef(v));}
  }

    