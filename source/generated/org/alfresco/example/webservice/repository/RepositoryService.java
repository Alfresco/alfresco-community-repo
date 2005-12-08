/**
 * RepositoryService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.repository;

public interface RepositoryService extends javax.xml.rpc.Service {

/**
 * Provides read and write operations against a repository.
 */
    public java.lang.String getRepositoryServiceAddress();

    public org.alfresco.example.webservice.repository.RepositoryServiceSoapPort getRepositoryService() throws javax.xml.rpc.ServiceException;

    public org.alfresco.example.webservice.repository.RepositoryServiceSoapPort getRepositoryService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
