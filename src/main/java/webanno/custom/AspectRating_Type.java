
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
public class AspectRating_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = AspectRating.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("webanno.custom.AspectRating");
 
  /** @generated */
  final Feature casFeat_Aspect;
  /** @generated */
  final int     casFeatCode_Aspect;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getAspect(int addr) {
        if (featOkTst && casFeat_Aspect == null)
      jcas.throwFeatMissing("Aspect", "webanno.custom.AspectRating");
    return ll_cas.ll_getStringValue(addr, casFeatCode_Aspect);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setAspect(int addr, String v) {
        if (featOkTst && casFeat_Aspect == null)
      jcas.throwFeatMissing("Aspect", "webanno.custom.AspectRating");
    ll_cas.ll_setStringValue(addr, casFeatCode_Aspect, v);}
    
  
 
  /** @generated */
  final Feature casFeat_OwnAspect;
  /** @generated */
  final int     casFeatCode_OwnAspect;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getOwnAspect(int addr) {
        if (featOkTst && casFeat_OwnAspect == null)
      jcas.throwFeatMissing("OwnAspect", "webanno.custom.AspectRating");
    return ll_cas.ll_getRefValue(addr, casFeatCode_OwnAspect);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setOwnAspect(int addr, int v) {
        if (featOkTst && casFeat_OwnAspect == null)
      jcas.throwFeatMissing("OwnAspect", "webanno.custom.AspectRating");
    ll_cas.ll_setRefValue(addr, casFeatCode_OwnAspect, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public int getOwnAspect(int addr, int i) {
        if (featOkTst && casFeat_OwnAspect == null)
      jcas.throwFeatMissing("OwnAspect", "webanno.custom.AspectRating");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_OwnAspect), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_OwnAspect), i);
  return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_OwnAspect), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setOwnAspect(int addr, int i, int v) {
        if (featOkTst && casFeat_OwnAspect == null)
      jcas.throwFeatMissing("OwnAspect", "webanno.custom.AspectRating");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_OwnAspect), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_OwnAspect), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_OwnAspect), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public AspectRating_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_Aspect = jcas.getRequiredFeatureDE(casType, "Aspect", "uima.cas.String", featOkTst);
    casFeatCode_Aspect  = (null == casFeat_Aspect) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Aspect).getCode();

 
    casFeat_OwnAspect = jcas.getRequiredFeatureDE(casType, "OwnAspect", "uima.cas.FSArray", featOkTst);
    casFeatCode_OwnAspect  = (null == casFeat_OwnAspect) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_OwnAspect).getCode();

  }
}



    