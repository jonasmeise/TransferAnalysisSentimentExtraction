

/* First created by JCasGen Fri Dec 14 23:25:09 CET 2018 */
package de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Tue Dec 18 17:19:09 CET 2018
 * XML source: C:/Users/Jonas/Downloads/de.unidue.langtech.bachelor.meise/de.unidue.langtech.bachelor.meise/typesystem.xml
 * @generated */
public class Dependency extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Dependency.class);
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
  protected Dependency() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Dependency(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Dependency(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Dependency(JCas jcas, int begin, int end) {
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
  //* Feature: Governor

  /** getter for Governor - gets 
   * @generated
   * @return value of the feature 
   */
  public Token getGovernor() {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_Governor == null)
      jcasType.jcas.throwFeatMissing("Governor", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    return (Token)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Dependency_Type)jcasType).casFeatCode_Governor)));}
    
  /** setter for Governor - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setGovernor(Token v) {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_Governor == null)
      jcasType.jcas.throwFeatMissing("Governor", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    jcasType.ll_cas.ll_setRefValue(addr, ((Dependency_Type)jcasType).casFeatCode_Governor, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: Dependent

  /** getter for Dependent - gets 
   * @generated
   * @return value of the feature 
   */
  public Token getDependent() {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_Dependent == null)
      jcasType.jcas.throwFeatMissing("Dependent", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    return (Token)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Dependency_Type)jcasType).casFeatCode_Dependent)));}
    
  /** setter for Dependent - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDependent(Token v) {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_Dependent == null)
      jcasType.jcas.throwFeatMissing("Dependent", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    jcasType.ll_cas.ll_setRefValue(addr, ((Dependency_Type)jcasType).casFeatCode_Dependent, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: DependencyType

  /** getter for DependencyType - gets 
   * @generated
   * @return value of the feature 
   */
  public String getDependencyType() {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_DependencyType == null)
      jcasType.jcas.throwFeatMissing("DependencyType", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Dependency_Type)jcasType).casFeatCode_DependencyType);}
    
  /** setter for DependencyType - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDependencyType(String v) {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_DependencyType == null)
      jcasType.jcas.throwFeatMissing("DependencyType", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    jcasType.ll_cas.ll_setStringValue(addr, ((Dependency_Type)jcasType).casFeatCode_DependencyType, v);}    
   
    
  //*--------------*
  //* Feature: flavor

  /** getter for flavor - gets 
   * @generated
   * @return value of the feature 
   */
  public String getFlavor() {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_flavor == null)
      jcasType.jcas.throwFeatMissing("flavor", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Dependency_Type)jcasType).casFeatCode_flavor);}
    
  /** setter for flavor - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setFlavor(String v) {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_flavor == null)
      jcasType.jcas.throwFeatMissing("flavor", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    jcasType.ll_cas.ll_setStringValue(addr, ((Dependency_Type)jcasType).casFeatCode_flavor, v);}    
  }

    