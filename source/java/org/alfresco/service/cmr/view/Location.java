/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.service.cmr.view;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * Importer / Exporter Location
 * 
 * @author David Caruana
 */
public class Location
{
    private StoreRef storeRef = null;
    private NodeRef nodeRef = null;
    private String path = null;
    private QName childAssocType = null;
    

    /**
     * Construct
     * 
     * @param nodeRef
     */
    public Location(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("Node Ref", nodeRef);
        this.storeRef = nodeRef.getStoreRef();
        this.nodeRef = nodeRef;
    }

    /**
     * Construct
     * 
     * @param storeRef
     */
    public Location(StoreRef storeRef)
    {
        ParameterCheck.mandatory("Store Ref", storeRef);
        this.storeRef = storeRef;
    }

    /**
     * @return  the store ref
     */
    public StoreRef getStoreRef()
    {
        return storeRef;
    }
    
    /**
     * @return  the node ref
     */
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }
    
    /**
     * Sets the location to the specified path
     *  
     * @param path  path relative to store or node reference
     */
    public void setPath(String path)
    {
        this.path = path;
    }
    
    /**
     * @return  the location
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Sets the child association type
     * 
     * @param childAssocType  child association type
     */
    public void setChildAssocType(QName childAssocType)
    {
        this.childAssocType = childAssocType;
    }
    
    /**
     * @return  the child association type
     */
    public QName getChildAssocType()
    {
        return childAssocType;
    }
}
