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
package org.alfresco.jcr.item;

import java.util.List;

import javax.jcr.PathNotFoundException;

import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;


/**
 * Responsible for finding JCR Items (Nodes, Properties) from Alfresco equivalents 
 * 
 * @author David Caruana
 *
 */
public class ItemResolver
{

    /**
     * Create an Item from a JCR Path
     * 
     * @param context  session context
     * @param from  starting node for path
     * @param path  the path
     * @return  the Item (Node or Property)
     * @throws PathNotFoundException
     */
    public static ItemImpl findItem(SessionImpl context, NodeRef from, String path)
        throws PathNotFoundException
    {
        ItemImpl item = null;
        
        NodeRef nodeRef = getNodeRef(context, from, path);
        if (nodeRef != null)
        {
            item = new NodeImpl(context, nodeRef);
        }
        else
        {
            // TODO: create property
        }
        
        if (item == null)
        {
            throw new PathNotFoundException("Path " + path + " not found.");
        }

        return item;
    }

    /**
     * Create an Node from a JCR Path
     * 
     * @param context  session context
     * @param from  starting node for path
     * @param path  the path
     * @return  the Item (Node or Property)
     * @throws PathNotFoundException
     */
    public static NodeImpl findNode(SessionImpl context, NodeRef from, String path)
        throws PathNotFoundException
    {
        NodeRef nodeRef = getNodeRef(context, from, path);
        if (nodeRef == null)
        {
            throw new PathNotFoundException("A node does not exist at path " + path + " relative to node " + from);
        }
        return new NodeImpl(context, nodeRef);
    }
    
    /**
     * Determine if Item exists
     * 
     * @param context  session context
     * @param from  starting node for path
     * @param path  the path
     * @return  true => exists, false => no it doesn't
     */
    public static boolean itemExists(SessionImpl context, NodeRef from, String path)
    {
        boolean exists = nodeExists(context, from, path);
        if (!exists)
        {
            // TODO: Check for property
        }
        return exists;
    }

    /**
     * Determine if Node exists
     * 
     * @param context  session context
     * @param from  starting node for path
     * @param path  the path
     * @return  true => exists, false => no it doesn't
     */
    public static boolean nodeExists(SessionImpl context, NodeRef from, String path)
    {
        NodeRef nodeRef = getNodeRef(context, from, path);
        return nodeRef != null;
    }
    
    /**
     * Gets the Node Reference for the node at the specified path
     * 
     * @param context  session context
     * @param from  the starting node for the path
     * @param path  the path
     * @return  the node reference (or null if not found)
     */
    public static NodeRef getNodeRef(SessionImpl context, NodeRef from, String path)
    {
        NodeRef nodeRef = null;
        
        // TODO: Support JCR Path
        // TODO: Catch malformed path and return false (per Specification)
        SearchService search = context.getRepositoryImpl().getServiceRegistry().getSearchService(); 
        List<NodeRef> nodeRefs = search.selectNodes(from, path, null, context.getNamespaceResolver(), false);
        if (nodeRefs != null && nodeRefs.size() > 0)
        {
            nodeRef = nodeRefs.get(0);
        }
            
        return nodeRef;
    }
    
}
