

/* First created by JCasGen Fri Dec 14 23:25:09 CET 2018 */
package webanno.custom;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Sat Dec 15 00:23:16 CET 2018
 * XML source: C:/Users/Jonas/Downloads/de.unidue.langtech.bachelor.meise/de.unidue.langtech.bachelor.meise/typesystem.xml
 * @generated */
public class AspectRating extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(AspectRating.class);
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
  protected AspectRating() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public AspectRating(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public AspectRating(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public AspectRating(JCas jcas, int begin, int end) {
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
  //* Feature: Aspect

  /** getter for Aspect - gets 
   * @generated
   * @return value of the feature 
   */
  public String getAspect() {
    if (AspectRating_Type.featOkTst && ((AspectRating_Type)jcasType).casFeat_Aspect == null)
      jcasType.jcas.throwFeatMissing("Aspect", "webanno.custom.AspectRating");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AspectRating_Type)jcasType).casFeatCode_Aspect);}
    
  /** setter for Aspect - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setAspect(String v) {
    if (AspectRating_Type.featOkTst && ((AspectRating_Type)jcasType).casFeat_Aspect == null)
      jcasType.jcas.throwFeatMissing("Aspect", "webanno.custom.AspectRating");
    jcasType.ll_cas.ll_setStringValue(addr, ((AspectRating_Type)jcasType).casFeatCode_Aspect, v);}    
   
    
  //*--------------*
  //* Feature: OwnAspect

  /** getter for OwnAspect - gets 
   * @generated
   * @return value of the feature 
   */
  public FSArray getOwnAspect() {
    if (AspectRating_Type.featOkTst && ((AspectRating_Type)jcasType).casFeat_OwnAspect == null)
      jcasType.jcas.throwFeatMissing("OwnAspect", "webanno.custom.AspectRating");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((AspectRating_Type)jcasType).casFeatCode_OwnAspect)));}
    
  /** setter for OwnAspect - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setOwnAspect(FSArray v) {
    if (AspectRating_Type.featOkTst && ((AspectRating_Type)jcasType).casFeat_OwnAspect == null)
      jcasType.jcas.throwFeatMissing("OwnAspect", "webanno.custom.AspectRating");
    jcasType.ll_cas.ll_setRefValue(addr, ((AspectRating_Type)jcasType).casFeatCode_OwnAspect, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for OwnAspect - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public AspectRatingOwnAspectLink getOwnAspect(int i) {
    if (AspectRating_Type.featOkTst && ((AspectRating_Type)jcasType).casFeat_OwnAspect == null)
      jcasType.jcas.throwFeatMissing("OwnAspect", "webanno.custom.AspectRating");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((AspectRating_Type)jcasType).casFeatCode_OwnAspect), i);
    return (AspectRatingOwnAspectLink)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((AspectRating_Type)jcasType).casFeatCode_OwnAspect), i)));}

  /** indexed setter for OwnAspect - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setOwnAspect(int i, AspectRatingOwnAspectLink v) { 
    if (AspectRating_Type.featOkTst && ((AspectRating_Type)jcasType).casFeat_OwnAspect == null)
      jcasType.jcas.throwFeatMissing("OwnAspect", "webanno.custom.AspectRating");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((AspectRating_Type)jcasType).casFeatCode_OwnAspect), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((AspectRating_Type)jcasType).casFeatCode_OwnAspect), i, jcasType.ll_cas.ll_getFSRef(v));}
  }

    