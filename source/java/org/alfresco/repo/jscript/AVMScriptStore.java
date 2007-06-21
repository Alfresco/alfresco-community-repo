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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.jscript;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.template.AVM;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * @author Kevin Roast
 */
public class AVMScriptStore
{
    private ServiceRegistry services;
    private AVMStoreDescriptor descriptor;
    private Scriptable scope;
    
    /**
     * Constructor
     * 
     * @param services
     * @param store         Store descriptor this object represents
     * @param scope
     */
    public AVMScriptStore(ServiceRegistry services, AVMStoreDescriptor store, Scriptable scope)
    {
        this.descriptor = store;
        this.services = services;
        this.scope = scope;
    }
    
    /**
     * @return Store name
     */
    public String getName()
    {
        return this.descriptor.getName();
    }
    
    public String jsGet_name()
    {
        return getName();
    }
    
    /**
     * @return Store name
     */
    public String getId()
    {
        return this.descriptor.getName();
    }
    
    public String jsGet_id()
    {
        return getId();
    }
    
    /**
     * @return User who created the store
     */
    public String getCreator()
    {
        return this.descriptor.getCreator();
    }
    
    public String jsGet_creator()
    {
        return getCreator();
    }
    
    /**
     * @return Creation date of the store
     */
    public Date getCreatedDate()
    {
        return new Date(this.descriptor.getCreateDate());
    }
    
    public Serializable jsGet_createdDate()
    {
        return new ValueConverter().convertValueForScript(
                this.services, this.scope, null, getCreatedDate());
    }
    
    /**
     * @return the root node of all webapps in the store
     */
    public AVMNode lookupRoot()
    {
        AVMNode rootNode = null;
        String rootPath = this.descriptor.getName() + ':' + AVM.getWebappsFolderPath();
        AVMNodeDescriptor nodeDesc = this.services.getAVMService().lookup(-1, rootPath);
        if (nodeDesc != null)
        {
            rootNode = new AVMNode(AVMNodeConverter.ToNodeRef(-1, rootPath), this.services, this.scope);
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
    public AVMNode lookupNode(String path)
    {
        AVMNode node = null;
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
                node = new AVMNode(path, -1, this.services, this.scope);
            }
        }
        return node;
    }
    
    /**
     * Perform a lucene query against this store.
     * 
     * @param query     Lucene
     * 
     * @return array of AVM node object results - empty array if no results found
     */
    public Scriptable luceneSearch(String query)
    {
        Object[] nodes = null;
        
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
                nodes = new Object[results.length()];
                for (int i=0; i<results.length(); i++)
                {
                    ResultSetRow row = results.getRow(i);
                    nodes[i] = new AVMNode(row.getNodeRef(), this.services, this.scope);
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
        
        if (nodes != null)
        {
            return Context.getCurrentContext().newArray(this.scope, nodes);
        }
        else
        {
            return Context.getCurrentContext().newArray(this.scope, 0);
        }
    }
}
