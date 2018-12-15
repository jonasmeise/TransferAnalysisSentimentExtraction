

/* First created by JCasGen Fri Dec 14 23:20:04 CET 2018 */
package de.tudarmstadt.ukp.dkpro.core.api.semantics.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.TOP;


/** 
 * Updated by JCasGen Sat Dec 15 00:23:13 CET 2018
 * XML source: C:/Users/Jonas/Downloads/de.unidue.langtech.bachelor.meise/de.unidue.langtech.bachelor.meise/typesystem.xml
 * @generated */
public class SemArgLink extends TOP {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(SemArgLink.class);
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
  protected SemArgLink() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public SemArgLink(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public SemArgLink(JCas jcas) {
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
    if (SemArgLink_Type.featOkTst && ((SemArgLink_Type)jcasType).casFeat_role == null)
      jcasType.jcas.throwFeatMissing("role", "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemArgLink");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SemArgLink_Type)jcasType).casFeatCode_role);}
    
  /** setter for role - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setRole(String v) {
    if (SemArgLink_Type.featOkTst && ((SemArgLink_Type)jcasType).casFeat_role == null)
      jcasType.jcas.throwFeatMissing("role", "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemArgLink");
    jcasType.ll_cas.ll_setStringValue(addr, ((SemArgLink_Type)jcasType).casFeatCode_role, v);}    
   
    
  //*--------------*
  //* Feature: target

  /** getter for target - gets 
   * @generated
   * @return value of the feature 
   */
  public SemArg getTarget() {
    if (SemArgLink_Type.featOkTst && ((SemArgLink_Type)jcasType).casFeat_target == null)
      jcasType.jcas.throwFeatMissing("target", "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemArgLink");
    return (SemArg)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((SemArgLink_Type)jcasType).casFeatCode_target)));}
    
  /** setter for target - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTarget(SemArg v) {
    if (SemArgLink_Type.featOkTst && ((SemArgLink_Type)jcasType).casFeat_target == null)
      jcasType.jcas.throwFeatMissing("target", "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemArgLink");
    jcasType.ll_cas.ll_setRefValue(addr, ((SemArgLink_Type)jcasType).casFeatCode_target, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    