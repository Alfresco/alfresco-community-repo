/**
 * ClassificationService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.classification;

public interface ClassificationService extends javax.xml.rpc.Service {

/**
 * Provides support for classifying content resources.
 */
    public java.lang.String getClassificationServiceAddress();

    public org.alfresco.example.webservice.classification.ClassificationServiceSoapPort getClassificationService() throws javax.xml.rpc.ServiceException;

    public org.alfresco.example.webservice.classification.ClassificationServiceSoapPort getClassificationService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
