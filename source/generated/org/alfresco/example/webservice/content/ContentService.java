/**
 * ContentService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.content;

public interface ContentService extends javax.xml.rpc.Service {

/**
 * Provides read and write access to content streams.
 */
    public java.lang.String getContentServiceAddress();

    public org.alfresco.example.webservice.content.ContentServiceSoapPort getContentService() throws javax.xml.rpc.ServiceException;

    public org.alfresco.example.webservice.content.ContentServiceSoapPort getContentService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
