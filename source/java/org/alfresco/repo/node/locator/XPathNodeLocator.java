/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.node.locator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.ParameterCheck;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class XPathNodeLocator extends AbstractNodeLocator
{
    public static final String NAME = "XPathNodeLocator";
    public static final String QUERY_KEY = "query";
    public static final String STORE_TYPE_KEY = "store_type";
    public static final String STORE_ID_KEY = "store_id";

    private SearchService searchService;
    private StoreRef defaultStore;
    
    /**
    * {@inheritDoc}
    */
    @Override
    public NodeRef getNode(NodeRef source, Map<String, Serializable> params)
    {
        String query = (String) params.get(QUERY_KEY);
        ParameterCheck.mandatoryString("query", query);
        StoreRef store = null;
        if(source!=null)
        {
            store = source.getStoreRef();
        }
        else 
        {
            String storeType = (String) params.get(STORE_TYPE_KEY);
            String storeId = (String) params.get(STORE_ID_KEY);
            if(storeType !=null && storeId != null)
            {
                store = new StoreRef(storeType, storeId);
            }
            else store = defaultStore;
        }
        try
        {
            ResultSet results = searchService.query(store, SearchService.LANGUAGE_XPATH, query);
            List<NodeRef> nodes = results.getNodeRefs();
            if (nodes.size() > 0)
            {
                return nodes.get(0);
            }
        }
        catch(Exception e)
        {
            String msg = "Error while searching XPath. StoreRef: " + store + " Query: " + query;
            throw new AlfrescoRuntimeException(msg, e);
        }
        return null;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public List<ParameterDefinition> getParameterDefinitions()
    {
        List<ParameterDefinition> paramDefs = new ArrayList<ParameterDefinition>(2);
        paramDefs.add(new ParameterDefinitionImpl(QUERY_KEY, DataTypeDefinition.TEXT, true, "Query"));
        paramDefs.add(new ParameterDefinitionImpl(STORE_TYPE_KEY, DataTypeDefinition.TEXT, false, "Store Type"));
        paramDefs.add(new ParameterDefinitionImpl(STORE_ID_KEY, DataTypeDefinition.TEXT, false, "Store Id"));
        return paramDefs;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public String getName()
    {
        return NAME;
    }
 
    /**
     * @param searchService the searchService to set
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    /**
     * @param defaultStore the defaultStore to set
     */
    public void setDefaultStore(String defaultStoreStr)
    {
        this.defaultStore = new StoreRef(defaultStoreStr);
    }
}