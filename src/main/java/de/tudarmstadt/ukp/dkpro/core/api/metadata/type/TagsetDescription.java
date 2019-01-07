

/* First created by JCasGen Fri Dec 14 23:20:04 CET 2018 */
package de.tudarmstadt.ukp.dkpro.core.api.metadata.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Jan 07 22:47:37 CET 2019
 * XML source: C:/Users/Jonas/Downloads/de.unidue.langtech.bachelor.meise/de.unidue.langtech.bachelor.meise/typesystem.xml
 * @generated */
public class TagsetDescription extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(TagsetDescription.class);
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
  protected TagsetDescription() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public TagsetDescription(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public TagsetDescription(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public TagsetDescription(JCas jcas, int begin, int end) {
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
  //* Feature: layer

  /** getter for layer - gets 
   * @generated
   * @return value of the feature 
   */
  public String getLayer() {
    if (TagsetDescription_Type.featOkTst && ((TagsetDescription_Type)jcasType).casFeat_layer == null)
      jcasType.jcas.throwFeatMissing("layer", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagsetDescription");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TagsetDescription_Type)jcasType).casFeatCode_layer);}
    
  /** setter for layer - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLayer(String v) {
    if (TagsetDescription_Type.featOkTst && ((TagsetDescription_Type)jcasType).casFeat_layer == null)
      jcasType.jcas.throwFeatMissing("layer", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagsetDescription");
    jcasType.ll_cas.ll_setStringValue(addr, ((TagsetDescription_Type)jcasType).casFeatCode_layer, v);}    
   
    
  //*--------------*
  //* Feature: name

  /** getter for name - gets 
   * @generated
   * @return value of the feature 
   */
  public String getName() {
    if (TagsetDescription_Type.featOkTst && ((TagsetDescription_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagsetDescription");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TagsetDescription_Type)jcasType).casFeatCode_name);}
    
  /** setter for name - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setName(String v) {
    if (TagsetDescription_Type.featOkTst && ((TagsetDescription_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagsetDescription");
    jcasType.ll_cas.ll_setStringValue(addr, ((TagsetDescription_Type)jcasType).casFeatCode_name, v);}    
   
    
  //*--------------*
  //* Feature: tags

  /** getter for tags - gets 
   * @generated
   * @return value of the feature 
   */
  public FSArray getTags() {
    if (TagsetDescription_Type.featOkTst && ((TagsetDescription_Type)jcasType).casFeat_tags == null)
      jcasType.jcas.throwFeatMissing("tags", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagsetDescription");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((TagsetDescription_Type)jcasType).casFeatCode_tags)));}
    
  /** setter for tags - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTags(FSArray v) {
    if (TagsetDescription_Type.featOkTst && ((TagsetDescription_Type)jcasType).casFeat_tags == null)
      jcasType.jcas.throwFeatMissing("tags", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagsetDescription");
    jcasType.ll_cas.ll_setRefValue(addr, ((TagsetDescription_Type)jcasType).casFeatCode_tags, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for tags - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public TagDescription getTags(int i) {
    if (TagsetDescription_Type.featOkTst && ((TagsetDescription_Type)jcasType).casFeat_tags == null)
      jcasType.jcas.throwFeatMissing("tags", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagsetDescription");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((TagsetDescription_Type)jcasType).casFeatCode_tags), i);
    return (TagDescription)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((TagsetDescription_Type)jcasType).casFeatCode_tags), i)));}

  /** indexed setter for tags - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setTags(int i, TagDescription v) { 
    if (TagsetDescription_Type.featOkTst && ((TagsetDescription_Type)jcasType).casFeat_tags == null)
      jcasType.jcas.throwFeatMissing("tags", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagsetDescription");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((TagsetDescription_Type)jcasType).casFeatCode_tags), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((TagsetDescription_Type)jcasType).casFeatCode_tags), i, jcasType.ll_cas.ll_getFSRef(v));}
  }

    