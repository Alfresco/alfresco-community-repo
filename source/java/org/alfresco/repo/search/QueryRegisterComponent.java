package org.alfresco.repo.search;

import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.namespace.QName;

public interface QueryRegisterComponent
{
    /**
     * Get a query defintion by Qname
     * 
     * @param qName QName
     * @return CannedQueryDef
     */
    public CannedQueryDef getQueryDefinition(QName qName);
    
    /**
     * Get the name of the collection containing a query
     * 
     * @param qName QName
     * @return String
     */
    public String getCollectionNameforQueryDefinition(QName qName);
    
    /**
     * Get a parameter definition
     * 
     * @param qName QName
     * @return QueryParameterDefinition
     */
    public QueryParameterDefinition getParameterDefinition(QName qName);
    
    /**
     * Get the name of the collection containing a parameter definition
     * 
     * @param qName QName
     * @return String
     */
    public String getCollectionNameforParameterDefinition(QName qName);
    
    
    /**
     * Get a query collection by name
     * 
     * @param name String
     * @return QueryCollection
     */
    public QueryCollection getQueryCollection(String name);
    
    
    /**
     * Load a query collection
     *
     * @param location String
     */
    public void loadQueryCollection(String location);
}
