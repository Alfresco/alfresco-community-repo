/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.dictionary;

import org.alfresco.repo.search.SearcherException;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;

/**
 * Repository location object - defines a location in the repository (can also be used for classpath location)
 */
public class RepositoryLocation
{
    /** Store protocol */
    private String storeProtocol = StoreRef.PROTOCOL_WORKSPACE; // default

    /** Store identifier */
    private String storeId = "SpacesStore"; // default

    /** Path */
    private String path = ""; // default
    
    /** Search Language */
    private String queryLanguage = "xpath"; // default
    
    public static final String LANGUAGE_PATH = "path"; // lookup directly using node (prefix qname) path
    public static final String LANGUAGE_CLASSPATH = "classpath";
    
    
    /**
     */
    public RepositoryLocation()
    {
    }
    
    /**
     * Constructor
     * 
     * @param storeRef       the store reference (e.g. 'workspace://SpacesStore' )
     * @param path           the path (e.g. '/app:company_home/app:dictionary/app:models' )
     * @param queryLanguage  the query language (e.g. 'xpath' or 'lucence')
     */
    public RepositoryLocation(StoreRef storeRef, String path, String queryLanguage)
    {
        this.storeProtocol = storeRef.getProtocol();
        this.storeId = storeRef.getIdentifier();
        this.path = path;
        
        setQueryLanguage(queryLanguage);
    }

    /**
     * Set the store protocol
     *
     * @param storeProtocol     the store protocol
     */
    public void setStoreProtocol(String storeProtocol)
    {
        this.storeProtocol = storeProtocol;
    }

    /**
     * Set the store identifier
     *
     * @param storeId       the store identifier
     */
    public void setStoreId(String storeId)
    {
        this.storeId = storeId;
    }

    /**
     * Set the path
     * 
     * Example path: /app:company_home/app:dictionary/app:models
     *
     * @param path  the path
     */
    public void setPath(String path)
    {
        this.path = path;
    }
    
    /**
     * Set the query language
     *
     * @param path  the search language
     */
    public void setQueryLanguage(String queryLanguage)
    {
        if (queryLanguage.equals(SearchService.LANGUAGE_LUCENE) || 
            queryLanguage.equals(SearchService.LANGUAGE_XPATH) || 
            queryLanguage.equals(LANGUAGE_PATH) ||
            queryLanguage.equals(LANGUAGE_CLASSPATH))
        {
            this.queryLanguage = queryLanguage;
        }
        else
        {
            throw new SearcherException("Unknown query language: " + queryLanguage);
        }
    }

    /**
     * Get the store reference (note: should be ignored for classpath location)
     *
     * @return  the store reference
     */
    public StoreRef getStoreRef()
    {
        return new StoreRef(this.storeProtocol, this.storeId);
    }
    
    /**
     * Get the path
     *
     * @return  the path
     */
    public String getPath()
    {
        return this.path;
    }
    
    /**
     * Get the query language
     * 
     * @return the query language
     */
    public String getQueryLanguage()
    {
        return this.queryLanguage;
    }

    /**
     * Get the Lucene query statement for models, based on the path
     *
     * @return  the Lucene query statement
     */
    public String getLuceneQueryStatement(QName contentModelType)
    {
        String result = "+TYPE:\"" + contentModelType.toString() + "\"";
        
        if ((this.path != null) && (! this.path.equals("")))
        {
            result += " +PATH:\"" + this.path + "\"";
        }
       
        return result;
    }
    
    /**
     * Get the XPath query statement for models, based on the path
     *
     * @return  the XPath query statement
     */
    public String getXPathQueryStatement(QName prefixResolvedContentModelType)
    {
        String result = "/*[subtypeOf('" + prefixResolvedContentModelType.toPrefixString() + "')]"; // immediate children only
        
        if ((this.path != null) && (! this.path.equals("")))
        {
            result = this.path + result;
        }
        
        return result;
    }
    
    public String[] getPathElements()
    {
        if ((this.path != null) && (! this.path.equals("")))
        {
            String pathToSplit = this.path;
            
            while (pathToSplit.startsWith("/"))
            {
                pathToSplit = pathToSplit.substring(1);
            }
            while (pathToSplit.endsWith("/"))
            {
                pathToSplit = pathToSplit.substring(0, pathToSplit.length() - 1);
            }
            String[] pathElements = pathToSplit.split("/");
            return pathElements;
        }
        else
        {
            return new String[0];
        }
    }
}