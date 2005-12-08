/**
 * AuthoringService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.authoring;

public interface AuthoringService extends javax.xml.rpc.Service {

/**
 * Provides support for collaborative editing of content.
 */
    public java.lang.String getAuthoringServiceAddress();

    public org.alfresco.example.webservice.authoring.AuthoringServiceSoapPort getAuthoringService() throws javax.xml.rpc.ServiceException;

    public org.alfresco.example.webservice.authoring.AuthoringServiceSoapPort getAuthoringService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
