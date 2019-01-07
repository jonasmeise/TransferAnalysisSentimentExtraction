

/* First created by JCasGen Fri Dec 14 23:20:04 CET 2018 */
package de.tudarmstadt.ukp.dkpro.core.api.metadata.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.DocumentAnnotation;


/** 
 * Updated by JCasGen Mon Jan 07 22:47:37 CET 2019
 * XML source: C:/Users/Jonas/Downloads/de.unidue.langtech.bachelor.meise/de.unidue.langtech.bachelor.meise/typesystem.xml
 * @generated */
public class DocumentMetaData extends DocumentAnnotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(DocumentMetaData.class);
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
  protected DocumentMetaData() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public DocumentMetaData(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public DocumentMetaData(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public DocumentMetaData(JCas jcas, int begin, int end) {
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
  //* Feature: documentTitle

  /** getter for documentTitle - gets 
   * @generated
   * @return value of the feature 
   */
  public String getDocumentTitle() {
    if (DocumentMetaData_Type.featOkTst && ((DocumentMetaData_Type)jcasType).casFeat_documentTitle == null)
      jcasType.jcas.throwFeatMissing("documentTitle", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DocumentMetaData_Type)jcasType).casFeatCode_documentTitle);}
    
  /** setter for documentTitle - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDocumentTitle(String v) {
    if (DocumentMetaData_Type.featOkTst && ((DocumentMetaData_Type)jcasType).casFeat_documentTitle == null)
      jcasType.jcas.throwFeatMissing("documentTitle", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    jcasType.ll_cas.ll_setStringValue(addr, ((DocumentMetaData_Type)jcasType).casFeatCode_documentTitle, v);}    
   
    
  //*--------------*
  //* Feature: documentId

  /** getter for documentId - gets 
   * @generated
   * @return value of the feature 
   */
  public String getDocumentId() {
    if (DocumentMetaData_Type.featOkTst && ((DocumentMetaData_Type)jcasType).casFeat_documentId == null)
      jcasType.jcas.throwFeatMissing("documentId", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DocumentMetaData_Type)jcasType).casFeatCode_documentId);}
    
  /** setter for documentId - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDocumentId(String v) {
    if (DocumentMetaData_Type.featOkTst && ((DocumentMetaData_Type)jcasType).casFeat_documentId == null)
      jcasType.jcas.throwFeatMissing("documentId", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    jcasType.ll_cas.ll_setStringValue(addr, ((DocumentMetaData_Type)jcasType).casFeatCode_documentId, v);}    
   
    
  //*--------------*
  //* Feature: documentUri

  /** getter for documentUri - gets 
   * @generated
   * @return value of the feature 
   */
  public String getDocumentUri() {
    if (DocumentMetaData_Type.featOkTst && ((DocumentMetaData_Type)jcasType).casFeat_documentUri == null)
      jcasType.jcas.throwFeatMissing("documentUri", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DocumentMetaData_Type)jcasType).casFeatCode_documentUri);}
    
  /** setter for documentUri - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDocumentUri(String v) {
    if (DocumentMetaData_Type.featOkTst && ((DocumentMetaData_Type)jcasType).casFeat_documentUri == null)
      jcasType.jcas.throwFeatMissing("documentUri", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    jcasType.ll_cas.ll_setStringValue(addr, ((DocumentMetaData_Type)jcasType).casFeatCode_documentUri, v);}    
   
    
  //*--------------*
  //* Feature: collectionId

  /** getter for collectionId - gets 
   * @generated
   * @return value of the feature 
   */
  public String getCollectionId() {
    if (DocumentMetaData_Type.featOkTst && ((DocumentMetaData_Type)jcasType).casFeat_collectionId == null)
      jcasType.jcas.throwFeatMissing("collectionId", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DocumentMetaData_Type)jcasType).casFeatCode_collectionId);}
    
  /** setter for collectionId - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setCollectionId(String v) {
    if (DocumentMetaData_Type.featOkTst && ((DocumentMetaData_Type)jcasType).casFeat_collectionId == null)
      jcasType.jcas.throwFeatMissing("collectionId", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    jcasType.ll_cas.ll_setStringValue(addr, ((DocumentMetaData_Type)jcasType).casFeatCode_collectionId, v);}    
   
    
  //*--------------*
  //* Feature: documentBaseUri

  /** getter for documentBaseUri - gets 
   * @generated
   * @return value of the feature 
   */
  public String getDocumentBaseUri() {
    if (DocumentMetaData_Type.featOkTst && ((DocumentMetaData_Type)jcasType).casFeat_documentBaseUri == null)
      jcasType.jcas.throwFeatMissing("documentBaseUri", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DocumentMetaData_Type)jcasType).casFeatCode_documentBaseUri);}
    
  /** setter for documentBaseUri - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDocumentBaseUri(String v) {
    if (DocumentMetaData_Type.featOkTst && ((DocumentMetaData_Type)jcasType).casFeat_documentBaseUri == null)
      jcasType.jcas.throwFeatMissing("documentBaseUri", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    jcasType.ll_cas.ll_setStringValue(addr, ((DocumentMetaData_Type)jcasType).casFeatCode_documentBaseUri, v);}    
   
    
  //*--------------*
  //* Feature: isLastSegment

  /** getter for isLastSegment - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getIsLastSegment() {
    if (DocumentMetaData_Type.featOkTst && ((DocumentMetaData_Type)jcasType).casFeat_isLastSegment == null)
      jcasType.jcas.throwFeatMissing("isLastSegment", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((DocumentMetaData_Type)jcasType).casFeatCode_isLastSegment);}
    
  /** setter for isLastSegment - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setIsLastSegment(boolean v) {
    if (DocumentMetaData_Type.featOkTst && ((DocumentMetaData_Type)jcasType).casFeat_isLastSegment == null)
      jcasType.jcas.throwFeatMissing("isLastSegment", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((DocumentMetaData_Type)jcasType).casFeatCode_isLastSegment, v);}    
  }

    