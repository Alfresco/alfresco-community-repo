/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.dictionary;

import org.alfresco.repo.search.SearcherException;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;

/**
 * Repository location object, defines a location in the repository from which dictionary models/resources should be loaded
 * for inclusion in the data dictionary.
 *
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
     * Set the queru language
     *
     * @param path  the search language
     */
    public void setQueryLanguage(String queryLanguage)
    {
        if (queryLanguage.equals(SearchService.LANGUAGE_LUCENE) || queryLanguage.equals(SearchService.LANGUAGE_XPATH))   
        {
            this.queryLanguage = queryLanguage;
        }
        else
        {
            throw new SearcherException("Unknown query language: " + queryLanguage);
        }
    }

    /**
     * Get the store reference
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
        
        if (this.path != null)
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
        
        if (this.path != null)
        {
            result = this.path + result;
        }

        return result;
    }
}