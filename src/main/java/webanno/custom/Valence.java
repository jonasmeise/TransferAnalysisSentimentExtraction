

/* First created by JCasGen Fri Dec 14 23:25:09 CET 2018 */
package webanno.custom;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Sat Dec 15 00:23:16 CET 2018
 * XML source: C:/Users/Jonas/Downloads/de.unidue.langtech.bachelor.meise/de.unidue.langtech.bachelor.meise/typesystem.xml
 * @generated */
public class Valence extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Valence.class);
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
  protected Valence() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Valence(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Valence(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Valence(JCas jcas, int begin, int end) {
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
  //* Feature: Dependent

  /** getter for Dependent - gets 
   * @generated
   * @return value of the feature 
   */
  public AspectRating getDependent() {
    if (Valence_Type.featOkTst && ((Valence_Type)jcasType).casFeat_Dependent == null)
      jcasType.jcas.throwFeatMissing("Dependent", "webanno.custom.Valence");
    return (AspectRating)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Valence_Type)jcasType).casFeatCode_Dependent)));}
    
  /** setter for Dependent - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDependent(AspectRating v) {
    if (Valence_Type.featOkTst && ((Valence_Type)jcasType).casFeat_Dependent == null)
      jcasType.jcas.throwFeatMissing("Dependent", "webanno.custom.Valence");
    jcasType.ll_cas.ll_setRefValue(addr, ((Valence_Type)jcasType).casFeatCode_Dependent, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: Governor

  /** getter for Governor - gets 
   * @generated
   * @return value of the feature 
   */
  public AspectRating getGovernor() {
    if (Valence_Type.featOkTst && ((Valence_Type)jcasType).casFeat_Governor == null)
      jcasType.jcas.throwFeatMissing("Governor", "webanno.custom.Valence");
    return (AspectRating)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Valence_Type)jcasType).casFeatCode_Governor)));}
    
  /** setter for Governor - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setGovernor(AspectRating v) {
    if (Valence_Type.featOkTst && ((Valence_Type)jcasType).casFeat_Governor == null)
      jcasType.jcas.throwFeatMissing("Governor", "webanno.custom.Valence");
    jcasType.ll_cas.ll_setRefValue(addr, ((Valence_Type)jcasType).casFeatCode_Governor, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: ValenceRating

  /** getter for ValenceRating - gets 
   * @generated
   * @return value of the feature 
   */
  public String getValenceRating() {
    if (Valence_Type.featOkTst && ((Valence_Type)jcasType).casFeat_ValenceRating == null)
      jcasType.jcas.throwFeatMissing("ValenceRating", "webanno.custom.Valence");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Valence_Type)jcasType).casFeatCode_ValenceRating);}
    
  /** setter for ValenceRating - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setValenceRating(String v) {
    if (Valence_Type.featOkTst && ((Valence_Type)jcasType).casFeat_ValenceRating == null)
      jcasType.jcas.throwFeatMissing("ValenceRating", "webanno.custom.Valence");
    jcasType.ll_cas.ll_setStringValue(addr, ((Valence_Type)jcasType).casFeatCode_ValenceRating, v);}    
  }

    