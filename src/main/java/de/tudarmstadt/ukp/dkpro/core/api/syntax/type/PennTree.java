

/* First created by JCasGen Fri Dec 14 23:20:04 CET 2018 */
package de.tudarmstadt.ukp.dkpro.core.api.syntax.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Sat Dec 15 00:23:13 CET 2018
 * XML source: C:/Users/Jonas/Downloads/de.unidue.langtech.bachelor.meise/de.unidue.langtech.bachelor.meise/typesystem.xml
 * @generated */
public class PennTree extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(PennTree.class);
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
  protected PennTree() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public PennTree(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public PennTree(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public PennTree(JCas jcas, int begin, int end) {
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
  //* Feature: PennTree

  /** getter for PennTree - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPennTree() {
    if (PennTree_Type.featOkTst && ((PennTree_Type)jcasType).casFeat_PennTree == null)
      jcasType.jcas.throwFeatMissing("PennTree", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree");
    return jcasType.ll_cas.ll_getStringValue(addr, ((PennTree_Type)jcasType).casFeatCode_PennTree);}
    
  /** setter for PennTree - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPennTree(String v) {
    if (PennTree_Type.featOkTst && ((PennTree_Type)jcasType).casFeat_PennTree == null)
      jcasType.jcas.throwFeatMissing("PennTree", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree");
    jcasType.ll_cas.ll_setStringValue(addr, ((PennTree_Type)jcasType).casFeatCode_PennTree, v);}    
   
    
  //*--------------*
  //* Feature: TransformationNames

  /** getter for TransformationNames - gets 
   * @generated
   * @return value of the feature 
   */
  public String getTransformationNames() {
    if (PennTree_Type.featOkTst && ((PennTree_Type)jcasType).casFeat_TransformationNames == null)
      jcasType.jcas.throwFeatMissing("TransformationNames", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree");
    return jcasType.ll_cas.ll_getStringValue(addr, ((PennTree_Type)jcasType).casFeatCode_TransformationNames);}
    
  /** setter for TransformationNames - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTransformationNames(String v) {
    if (PennTree_Type.featOkTst && ((PennTree_Type)jcasType).casFeat_TransformationNames == null)
      jcasType.jcas.throwFeatMissing("TransformationNames", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree");
    jcasType.ll_cas.ll_setStringValue(addr, ((PennTree_Type)jcasType).casFeatCode_TransformationNames, v);}    
  }

    