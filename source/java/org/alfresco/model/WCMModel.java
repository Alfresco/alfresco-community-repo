/**
 * 
 */
package org.alfresco.model;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * QName definitions for WCM.
 * @author britt
 */
public interface WCMModel 
{
    // content
    public static final QName TYPE_AVM_CONTENT = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmcontent");
    public static final QName TYPE_AVM_PLAIN_CONTENT = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmplaincontent");
    public static final QName TYPE_AVM_LAYERED_CONTENT = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmlayeredcontent");
    public static final QName PROP_AVM_FILE_INDIRECTION =  QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmfileindirection");
    
    // folders
    public static final QName TYPE_AVM_FOLDER = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmfolder");
    public static final QName TYPE_AVM_PLAIN_FOLDER = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmplainfolder");
    public static final QName TYPE_AVM_LAYERED_FOLDER = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmlayeredfolder");
    public static final QName PROP_AVM_DIR_INDIRECTION = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmdirindirection");
    
    // The XForms data capture template aspect.
    public static final QName ASPECT_TEMPLATE = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "template");
    public static final QName PROP_SCHEMA_ROOT_TAG_NAME = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "schemaroottagname");
    public static final QName ASSOC_TEMPLATE_OUTPUT_METHODS = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "templateoutputmethods");
    
    // An XML to something else tranformer aspect.
    public static final QName ASPECT_TEMPLATE_OUTPUT_METHOD = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "templateoutputmethod");
    public static final QName PROP_TEMPLATE_OUTPUT_METHOD_TYPE = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "templateoutputmethodtype");
    public static final QName PROP_TEMPLATE_OUTPUT_METHOD_DERIVED_FILE_EXTENSION = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "templateoutputmethodderivedfileextension");
    public static final QName PROP_TEMPLATE_SOURCE = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "templatesource");
}
