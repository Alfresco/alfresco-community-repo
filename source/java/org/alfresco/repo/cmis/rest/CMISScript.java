/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.rest;

import org.alfresco.repo.cmis.rest.CMISService.TypesFilter;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.web.scripts.Repository;
import org.alfresco.repo.web.util.paging.Cursor;
import org.alfresco.repo.web.util.paging.Page;
import org.alfresco.repo.web.util.paging.PagedResults;
import org.alfresco.repo.web.util.paging.Paging;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;


/**
 * CMIS Javascript API
 * 
 * @author davic
 */
public class CMISScript  extends BaseScopableProcessorExtension
{
    private static final TypesFilter defaultTypesFilter = TypesFilter.FoldersAndDocuments;
    
    private ServiceRegistry services;
    private Repository repository;
    private CMISService cmisService;
    private Paging paging;
    
    
    /**
     * Set the service registry
     * 
     * @param services  the service registry
     */
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
    }

    /**
     * Set the repository
     * 
     * @param repository
     */
    public void setRepository(Repository repository)
    {
        this.repository = repository;
    }
    
    /**
     * Set the paging helper
     * 
     * @param paging
     */
    public void setPaging(Paging paging)
    {
        this.paging = paging;
    }
    
    /**
     * Set the CMIS Navigation helper
     * 
     * @param cmisService
     */
    public void setCMISService(CMISService cmisService)
    {
        this.cmisService = cmisService;
    }
    
    /**
     * Gets the default root folder path
     * 
     * @return  default root folder path
     */
    public String getDefaultRootFolderPath()
    {
        return cmisService.getDefaultRootPath();
    }
    
    /**
     * Gets the default root folder
     * 
     * @return  default root folder
     */
    public ScriptNode getDefaultRootFolder()
    {
        return new ScriptNode(cmisService.getDefaultRootNodeRef(), services, getScope());
    }

    /**
     * Finds the arg value from the specified url argument and header
     * 
     * NOTE: Url argument takes precedence over header
     * 
     * @param argVal  url arg value
     * @param headerVal  header value
     * @return  value (or null)
     */
    public String findArg(String argVal, String headerVal)
    {
        return (argVal == null) ? headerVal : argVal;
    }

    /**
     * Finds the arg value from the specified url argument and header
     * 
     * NOTE: Url argument takes precedence over header
     * 
     * @param argVal  url arg value
     * @param headerVal  header value
     * @return  value (or null)
     */
    public String[] findArgM(String[] argVal, String[] headerVal)
    {
        return (argVal == null) ? headerVal : argVal;
    }

    /**
     * Gets the default Types filter
     * 
     * @return  default types filter
     */
    public String getDefaultTypesFilter()
    {
        return defaultTypesFilter.toString();
    }

    /**
     * Is specified Types filter valid?
     * 
     * @param typesFilter  types filter
     * @return  true => valid
     */
    public boolean isValidTypesFilter(String typesFilter)
    {
        try
        {
            TypesFilter.valueOf(typesFilter);
            return true;
        }
        catch(IllegalArgumentException e)
        {
            return false;
        }
        catch(NullPointerException e)
        {
            return false;
        }
    }
    
    /**
     * Finds a Node with the repository given a reference
     * 
     * @param referenceType  node, path
     * @param reference  node => id, path => path
     * @return  node (or null, if not found)
     */
    public ScriptNode findNode(String referenceType, String[] reference)
    {
        ScriptNode node = null;
        NodeRef nodeRef = repository.findNodeRef(referenceType, reference);
        if (nodeRef != null)
        {
            node = new ScriptNode(nodeRef, services, getScope());
        }
        return node;
    }
    
    /**
     * Query for node children
     * 
     * @param parent  node to query children for
     * @param typesFilter  types filter
     * @param page  page to query for
     * @return  paged result set of children
     */
    public PagedResults queryChildren(ScriptNode parent, String typesFilter, Page page)
    {
        TypesFilter filter = resolveTypesFilter(typesFilter);
        NodeRef[] children = cmisService.getChildren(parent.getNodeRef(), filter);
        
        Cursor cursor = paging.createCursor(children.length, page);
        ScriptNode[] nodes = new ScriptNode[cursor.getRowCount()];
        for (int i = cursor.getStartRow(); i <= cursor.getEndRow(); i++)
        {
            nodes[i - cursor.getStartRow()] = new ScriptNode(children[i], services, getScope());
        }
        
        PagedResults results = paging.createPagedResults(nodes, cursor);
        return results;
    }

    /**
     * Query for items checked-out to user
     * 
     * @param username  user
     * @param page
     * @return  paged result set of checked-out items
     */
    public PagedResults queryCheckedOut(String username, Page page)
    {
        return queryCheckedOut(username, null, false, page);
    }

    /**
     * Query for items checked-out to user within folder
     * 
     * @param username  user
     * @param folder  folder
     * @param page
     * @return  paged result set of checked-out items
     */
    public PagedResults queryCheckedOut(String username, ScriptNode folder, Page page)
    {
        return queryCheckedOut(username, folder, false, page);
    }

    /**
     * Query for items checked-out to user within folder (and possibly descendants)
     * 
     * @param username  user
     * @param folder  folder
     * @param includeDescendants  true = include descendants  
     * @param page
     * @return  paged result set of checked-out items
     */
    public PagedResults queryCheckedOut(String username, ScriptNode folder, boolean includeDescendants, Page page)
    {
        NodeRef[] checkedout = cmisService.getCheckedOut(username, (folder == null) ? null : folder.getNodeRef(), includeDescendants);
        Cursor cursor = paging.createCursor(checkedout.length, page);
        ScriptNode[] nodes = new ScriptNode[cursor.getRowCount()];
        for (int i = cursor.getStartRow(); i <= cursor.getEndRow(); i++)
        {
            nodes[i - cursor.getStartRow()] = new ScriptNode(checkedout[i], services, getScope());
        }
        
        PagedResults results = paging.createPagedResults(nodes, cursor);
        return results;
    }
    
    /**
     * Resolve to a Types Filter
     * 
     * NOTE: If specified types filter is not specified or invalid, the default types
     *       filter is returned
     *       
     * @param typesFilter  types filter
     * @return  resolved types filter
     */
    private TypesFilter resolveTypesFilter(String typesFilter)
    {
        if (isValidTypesFilter(typesFilter))
        {
            return TypesFilter.valueOf(typesFilter);
        }
        else
        {
            return defaultTypesFilter;
        }
    }
}
