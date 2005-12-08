/**
 * RepositoryServiceSoapPort.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.repository;

public interface RepositoryServiceSoapPort extends java.rmi.Remote {

    /**
     * Retrieves a list of stores where content resources are held.
     */
    public org.alfresco.example.webservice.types.Store[] getStores() throws java.rmi.RemoteException, org.alfresco.example.webservice.repository.RepositoryFault;

    /**
     * Executes a query against a store.
     */
    public org.alfresco.example.webservice.repository.QueryResult query(org.alfresco.example.webservice.types.Store store, org.alfresco.example.webservice.types.Query query, boolean includeMetaData) throws java.rmi.RemoteException, org.alfresco.example.webservice.repository.RepositoryFault;

    /**
     * Executes a query to retrieve the children of the specified
     * resource.
     */
    public org.alfresco.example.webservice.repository.QueryResult queryChildren(org.alfresco.example.webservice.types.Reference node) throws java.rmi.RemoteException, org.alfresco.example.webservice.repository.RepositoryFault;

    /**
     * Executes a query to retrieve the parents of the specified resource.
     */
    public org.alfresco.example.webservice.repository.QueryResult queryParents(org.alfresco.example.webservice.types.Reference node) throws java.rmi.RemoteException, org.alfresco.example.webservice.repository.RepositoryFault;

    /**
     * Executes a query to retrieve associated resources of the specified
     * resource.
     */
    public org.alfresco.example.webservice.repository.QueryResult queryAssociated(org.alfresco.example.webservice.types.Reference node, org.alfresco.example.webservice.repository.Association[] association) throws java.rmi.RemoteException, org.alfresco.example.webservice.repository.RepositoryFault;

    /**
     * Fetches the next batch of query results.
     */
    public org.alfresco.example.webservice.repository.QueryResult fetchMore(java.lang.String querySession) throws java.rmi.RemoteException, org.alfresco.example.webservice.repository.RepositoryFault;

    /**
     * Executes a CML script to manipulate the contents of a Repository
     * store.
     */
    public org.alfresco.example.webservice.repository.UpdateResult[] update(org.alfresco.example.webservice.types.CML statements) throws java.rmi.RemoteException, org.alfresco.example.webservice.repository.RepositoryFault;

    /**
     * Describes a content resource.
     */
    public org.alfresco.example.webservice.types.NodeDefinition[] describe(org.alfresco.example.webservice.types.Predicate items) throws java.rmi.RemoteException, org.alfresco.example.webservice.repository.RepositoryFault;

    /**
     * Gets a resource from the repository.
     */
    public org.alfresco.example.webservice.types.Node[] get(org.alfresco.example.webservice.types.Predicate where) throws java.rmi.RemoteException, org.alfresco.example.webservice.repository.RepositoryFault;
}
