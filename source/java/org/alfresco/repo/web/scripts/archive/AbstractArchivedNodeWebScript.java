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
package org.alfresco.repo.web.scripts.archive;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is an abstract base class for the various webscript controllers in the
 * NodeArchiveService.
 * 
 * @author Neil McErlean
 * @since 3.5
 */
public abstract class AbstractArchivedNodeWebScript extends DeclarativeWebScript
{
    public static final String NAME          = "name";
    public static final String TITLE         = "title";
    public static final String DESCRIPTION   = "description";
    public static final String NODEREF       = "nodeRef";
    public static final String ARCHIVED_BY   = "archivedBy";
    public static final String ARCHIVED_DATE = "archivedDate";
    public static final String DISPLAY_PATH  = "displayPath";
    public static final String USER_NAME     = "userName";
    public static final String FIRST_NAME    = "firstName";
    public static final String LAST_NAME     = "lastName";
    public static final String NODE_TYPE     = "nodeType";
    public static final String DELETED_NODES = "deletedNodes";
    
    // Injected services
    protected ServiceRegistry serviceRegistry;
    protected NodeArchiveService nodeArchiveService;

    /**
     * Sets the serviceRegistry instance
     * 
     * @param serviceRegistry the serviceRegistry to set
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Sets the nodeArchiveService instance
     * 
     * @param nodeArchiveService the nodeArchiveService to set
     */
    public void setNodeArchiveService(NodeArchiveService nodeArchiveService)
    {
        this.nodeArchiveService = nodeArchiveService;
    }

    protected StoreRef parseRequestForStoreRef(WebScriptRequest req)
    {
        // get the parameters that represent the StoreRef, we know they are present
        // otherwise this webscript would not have matched
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String storeType = templateVars.get("store_type");
        String storeId = templateVars.get("store_id");

        // create the StoreRef and ensure it is valid
        StoreRef storeRef = new StoreRef(storeType, storeId);

        return storeRef;
    }

    protected NodeRef parseRequestForNodeRef(WebScriptRequest req)
    {
        // get the parameters that represent the NodeRef. They may not all be there
        // for all deletednodes webscripts.
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String storeType = templateVars.get("store_type");
        String storeId = templateVars.get("store_id");
        String id = templateVars.get("id");

        if (id == null || id.trim().length() == 0)
        {
            return null;
        }
        else
        {
            return new NodeRef(storeType, storeId, id);
        }
    }
    
    /**
     * Retrieves the named parameter as an integer, if the parameter is not present the default value is returned
     * 
     * @param req The WebScript request
     * @param paramName The name of parameter to look for
     * @param defaultValue The default value that should be returned if parameter is not present in request or if it is not positive
     * @return The request parameter or default value
     */
    protected int getIntParameter(WebScriptRequest req, String paramName, int defaultValue)
    {
        String paramString = req.getParameter(paramName);
        
        if (paramString != null)
        {
            try
            {
                int param = Integer.valueOf(paramString);
                
                if (param >= 0)
                {
                    return param;
                }
            }
            catch (NumberFormatException e) 
            {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }
        }
        
        return defaultValue;
    }
    
    /**
     * This method gets all nodes from the archive which were originally contained
     * within the specified StoreRef.
     */
    protected SortedSet<ChildAssociationRef> getArchivedNodesFrom(StoreRef storeRef)
    {
        NodeService nodeService = serviceRegistry.getNodeService();
        NodeRef archiveRootNode = nodeService.getStoreArchiveNode(storeRef);

        List<ChildAssociationRef> children = nodeService.getChildAssocs(archiveRootNode);
    
        // We must get the sys:archived children in order of their archiving.
        Comparator<ChildAssociationRef> archivedNodeSorter = new ArchivedDateComparator();
        SortedSet<ChildAssociationRef> orderedChildren = new TreeSet<ChildAssociationRef>(archivedNodeSorter);
        for (ChildAssociationRef chAssRef : children)
        {
            if (nodeService.hasAspect(chAssRef.getChildRef(), ContentModel.ASPECT_ARCHIVED))
            {
                orderedChildren.add(chAssRef);
            }
        }
        return orderedChildren;
    }

    /**
     * This class is used to sort ChildAssociationRefs into the correct order based on archivedDate.
     * 
     * @author Neil Mc Erlean.
     */
    protected class ArchivedDateComparator implements Comparator<ChildAssociationRef>
    {
        
        @Override
        public int compare(ChildAssociationRef chAssRef1, ChildAssociationRef chAssRef2)
        {
            NodeService nodeService = serviceRegistry.getNodeService();
            Date archivedDate1 = (Date)nodeService.getProperty(chAssRef1.getChildRef(), ContentModel.PROP_ARCHIVED_DATE);
            Date archivedDate2 = (Date)nodeService.getProperty(chAssRef2.getChildRef(), ContentModel.PROP_ARCHIVED_DATE);
            
            // We want the dates to be in reverse order i.e. most recent first. Hence the reversal of 2 and 1 below.
            return archivedDate2.compareTo(archivedDate1);
        }
    }
}