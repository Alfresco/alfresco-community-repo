/**
 * 
 */
package org.alfresco.model;

import org.alfresco.service.namespace.QName;

/**
 * QName definitions for WCM.
 * @author britt
 */
public interface WCMModel 
{
    public static final String WCM_MODEL_URI = "http://www.alfresco.org/model/wcmmodel/1.0";
    public static final String WCM_MODEL_PREFIX = "wcm";
    
    // The XForms data capture template aspect.
    public static final QName ASPECT_TEMPLATE = QName.createQName(WCM_MODEL_URI, "template");
    public static final QName PROP_SCHEMA_ROOT_TAG_NAME = QName.createQName(WCM_MODEL_URI, "schemaroottagname");
    public static final QName ASSOC_TEMPLATE_OUTPUT_METHODS = QName.createQName(WCM_MODEL_URI, "templateoutputmethods");
    
    // An XML to something else tranformer aspect.
    public static final QName ASPECT_TEMPLATE_OUTPUT_METHOD = QName.createQName(WCM_MODEL_URI, "templateoutputmethod");
    public static final QName PROP_TEMPLATE_OUTPUT_METHOD_TYPE = QName.createQName(WCM_MODEL_URI, "templateoutputmethodtype");
    public static final QName PROP_TEMPLATE_SOURCE = QName.createQName(WCM_MODEL_URI, "templatesource");
}
