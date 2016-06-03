package org.alfresco.model;

import org.alfresco.service.namespace.QName;


/**
 * QuickShare Model Constants
 * 
 * @author janv
 */
public interface QuickShareModel
{
    // Namespaces
    static final String QSHARE_MODEL_1_0_URI = "http://www.alfresco.org/model/qshare/1.0";
    
    // Aspects
    static final QName ASPECT_QSHARE = QName.createQName(QSHARE_MODEL_1_0_URI, "shared");
    
    // Properties
    static final QName PROP_QSHARE_SHAREDID = QName.createQName(QSHARE_MODEL_1_0_URI, "sharedId");
    static final QName PROP_QSHARE_SHAREDBY = QName.createQName(QSHARE_MODEL_1_0_URI, "sharedBy");
}
