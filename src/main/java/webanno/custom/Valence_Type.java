
/* First created by JCasGen Fri Dec 14 23:25:09 CET 2018 */
package webanno.custom;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Tue Dec 18 17:19:10 CET 2018
 * @generated */
public class Valence_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Valence.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("webanno.custom.Valence");
 
  /** @generated */
  final Feature casFeat_Dependent;
  /** @generated */
  final int     casFeatCode_Dependent;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getDependent(int addr) {
        if (featOkTst && casFeat_Dependent == null)
      jcas.throwFeatMissing("Dependent", "webanno.custom.Valence");
    return ll_cas.ll_getRefValue(addr, casFeatCode_Dependent);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setDependent(int addr, int v) {
        if (featOkTst && casFeat_Dependent == null)
      jcas.throwFeatMissing("Dependent", "webanno.custom.Valence");
    ll_cas.ll_setRefValue(addr, casFeatCode_Dependent, v);}
    
  
 
  /** @generated */
  final Feature casFeat_Governor;
  /** @generated */
  final int     casFeatCode_Governor;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getGovernor(int addr) {
        if (featOkTst && casFeat_Governor == null)
      jcas.throwFeatMissing("Governor", "webanno.custom.Valence");
    return ll_cas.ll_getRefValue(addr, casFeatCode_Governor);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setGovernor(int addr, int v) {
        if (featOkTst && casFeat_Governor == null)
      jcas.throwFeatMissing("Governor", "webanno.custom.Valence");
    ll_cas.ll_setRefValue(addr, casFeatCode_Governor, v);}
    
  
 
  /** @generated */
  final Feature casFeat_ValenceRating;
  /** @generated */
  final int     casFeatCode_ValenceRating;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getValenceRating(int addr) {
        if (featOkTst && casFeat_ValenceRating == null)
      jcas.throwFeatMissing("ValenceRating", "webanno.custom.Valence");
    return ll_cas.ll_getStringValue(addr, casFeatCode_ValenceRating);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setValenceRating(int addr, String v) {
        if (featOkTst && casFeat_ValenceRating == null)
      jcas.throwFeatMissing("ValenceRating", "webanno.custom.Valence");
    ll_cas.ll_setStringValue(addr, casFeatCode_ValenceRating, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Valence_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_Dependent = jcas.getRequiredFeatureDE(casType, "Dependent", "webanno.custom.AspectRating", featOkTst);
    casFeatCode_Dependent  = (null == casFeat_Dependent) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Dependent).getCode();

 
    casFeat_Governor = jcas.getRequiredFeatureDE(casType, "Governor", "webanno.custom.AspectRating", featOkTst);
    casFeatCode_Governor  = (null == casFeat_Governor) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Governor).getCode();

 
    casFeat_ValenceRating = jcas.getRequiredFeatureDE(casType, "ValenceRating", "uima.cas.String", featOkTst);
    casFeatCode_ValenceRating  = (null == casFeat_ValenceRating) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_ValenceRating).getCode();

  }
}



    