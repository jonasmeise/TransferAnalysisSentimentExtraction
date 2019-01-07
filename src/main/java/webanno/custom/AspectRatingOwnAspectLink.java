

/* First created by JCasGen Fri Dec 14 23:25:09 CET 2018 */
package webanno.custom;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.TOP;


/** 
 * Updated by JCasGen Mon Jan 07 17:09:17 CET 2019
 * XML source: C:/Users/Jonas/Downloads/de.unidue.langtech.bachelor.meise/de.unidue.langtech.bachelor.meise/src/main/resources/typesystem.xml
 * @generated */
public class AspectRatingOwnAspectLink extends TOP {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(AspectRatingOwnAspectLink.class);
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
  protected AspectRatingOwnAspectLink() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public AspectRatingOwnAspectLink(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public AspectRatingOwnAspectLink(JCas jcas) {
    super(jcas);
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
  //* Feature: role

  /** getter for role - gets 
   * @generated
   * @return value of the feature 
   */
  public String getRole() {
    if (AspectRatingOwnAspectLink_Type.featOkTst && ((AspectRatingOwnAspectLink_Type)jcasType).casFeat_role == null)
      jcasType.jcas.throwFeatMissing("role", "webanno.custom.AspectRatingOwnAspectLink");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AspectRatingOwnAspectLink_Type)jcasType).casFeatCode_role);}
    
  /** setter for role - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setRole(String v) {
    if (AspectRatingOwnAspectLink_Type.featOkTst && ((AspectRatingOwnAspectLink_Type)jcasType).casFeat_role == null)
      jcasType.jcas.throwFeatMissing("role", "webanno.custom.AspectRatingOwnAspectLink");
    jcasType.ll_cas.ll_setStringValue(addr, ((AspectRatingOwnAspectLink_Type)jcasType).casFeatCode_role, v);}    
   
    
  //*--------------*
  //* Feature: target

  /** getter for target - gets 
   * @generated
   * @return value of the feature 
   */
  public AspectRating getTarget() {
    if (AspectRatingOwnAspectLink_Type.featOkTst && ((AspectRatingOwnAspectLink_Type)jcasType).casFeat_target == null)
      jcasType.jcas.throwFeatMissing("target", "webanno.custom.AspectRatingOwnAspectLink");
    return (AspectRating)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((AspectRatingOwnAspectLink_Type)jcasType).casFeatCode_target)));}
    
  /** setter for target - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTarget(AspectRating v) {
    if (AspectRatingOwnAspectLink_Type.featOkTst && ((AspectRatingOwnAspectLink_Type)jcasType).casFeat_target == null)
      jcasType.jcas.throwFeatMissing("target", "webanno.custom.AspectRatingOwnAspectLink");
    jcasType.ll_cas.ll_setRefValue(addr, ((AspectRatingOwnAspectLink_Type)jcasType).casFeatCode_target, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    