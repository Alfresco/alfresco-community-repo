/**
 * ContentServiceSoapPort.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.content;

public interface ContentServiceSoapPort extends java.rmi.Remote {

    /**
     * Retrieves content from the repository.
     */
    public org.alfresco.example.webservice.content.Content[] read(org.alfresco.example.webservice.types.Predicate items, java.lang.String property) throws java.rmi.RemoteException, org.alfresco.example.webservice.content.ContentFault;

    /**
     * Writes content to the repository.
     */
    public org.alfresco.example.webservice.content.Content write(org.alfresco.example.webservice.types.Reference node, java.lang.String property, byte[] content, org.alfresco.example.webservice.types.ContentFormat format) throws java.rmi.RemoteException, org.alfresco.example.webservice.content.ContentFault;

    /**
     * Clears content from the repository.
     */
    public org.alfresco.example.webservice.content.Content[] clear(org.alfresco.example.webservice.types.Predicate items, java.lang.String property) throws java.rmi.RemoteException, org.alfresco.example.webservice.content.ContentFault;
}
