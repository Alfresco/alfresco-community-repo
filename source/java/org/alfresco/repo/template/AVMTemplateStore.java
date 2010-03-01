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
package org.alfresco.repo.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;

/**
 * Representation of an AVM Store for the template model. Accessed via the AVM helper object
 * and is responsible for returning AVMTemplateNode objects via various mechanisms.
 * 
 * @author Kevin Roast
 */
public class AVMTemplateStore
{
    private ServiceRegistry services;
    private AVMStoreDescriptor descriptor;
    private TemplateImageResolver resolver;
    
    /**
     * Constructor
     * 
     * @param services
     * @param resolver
     * @param store         Store descriptor this object represents
     */
    public AVMTemplateStore(ServiceRegistry services, TemplateImageResolver resolver, AVMStoreDescriptor store)
    {
        this.descriptor = store;
        this.services = services;
        this.resolver = resolver;
    }
    
    /**
     * @return Store name
     */
    public String getName()
    {
        return this.descriptor.getName();
    }
    
    /**
     * @return Store name
     */
    public String getId()
    {
        return this.descriptor.getName();
    }
    
    /**
     * @return User who created the store
     */
    public String getCreator()
    {
        return this.descriptor.getCreator();
    }
    
    /**
     * @return Creation date of the store
     */
    public Date getCreatedDate()
    {
        return new Date(this.descriptor.getCreateDate());
    }
    
    /**
     * @return the root node of all webapps in the store
     */
    public AVMTemplateNode getLookupRoot()
    {
        AVMTemplateNode rootNode = null;
        String rootPath = this.descriptor.getName() + ':' + AVM.getWebappsFolderPath();
        AVMNodeDescriptor nodeDesc = this.services.getAVMService().lookup(-1, rootPath);
        if (nodeDesc != null)
        {
            rootNode = new AVMTemplateNode(rootPath, -1, this.services, this.resolver);
        }
        return rootNode;
    }
    
    /**
     * Lookup a node in the store, the path is assumed to be related to the webapps folder root.
     * Therefore a valid path would be "/ROOT/WEB-INF/lib/web.xml".
     * 
     * @param path      Relative to the webapps folder root path for this store.
     * 
     * @return node if found, null otherwise.
     */
    public AVMTemplateNode lookupNode(String path)
    {
        AVMTemplateNode node = null;
        if (path != null && path.length() != 0)
        {
            if (path.charAt(0) != '/')
            {
                path = '/' + path;
            }
            path = this.descriptor.getName() + ':' + AVM.getWebappsFolderPath() + path;
            AVMNodeDescriptor nodeDesc = this.services.getAVMService().lookup(-1, path);
            if (nodeDesc != null)
            {
                node = new AVMTemplateNode(path, -1, this.services, this.resolver);
            }
        }
        return node;
    }
    
    /**
     * Perform a lucene query against this store.
     * 
     * @param query     Lucene
     * 
     * @return list of AVM node objects as results - empty list if no results found
     */
    public List<AVMTemplateNode> luceneSearch(String query)
    {
        List<AVMTemplateNode> nodes = null;
        
        // perform the search against the repo
        ResultSet results = null;
        try
        {
            results = this.services.getSearchService().query(
                    new StoreRef(StoreRef.PROTOCOL_AVM, this.descriptor.getName()),
                    SearchService.LANGUAGE_LUCENE,
                    query);
            
            if (results.length() != 0)
            {
                nodes = new ArrayList<AVMTemplateNode>(results.length());
                for (ResultSetRow row : results)
                {
                    NodeRef nodeRef = row.getNodeRef();
                    nodes.add(new AVMTemplateNode(nodeRef, this.services, this.resolver));
                }
            }
        }
        catch (Throwable err)
        {
            throw new AlfrescoRuntimeException("Failed to execute search: " + query, err);
        }
        finally
        {
            if (results != null)
            {
                results.close();
            }
        }
        
        return (nodes != null ? nodes : (List)Collections.emptyList());
    }
}
