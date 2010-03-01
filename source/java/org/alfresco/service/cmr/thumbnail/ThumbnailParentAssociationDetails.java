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
package org.alfresco.service.cmr.thumbnail;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Encapsulates the details of a thumbnails parent association
 * 
 * @author Roy Wetherall
 */
public class ThumbnailParentAssociationDetails
{
    /** The parent node reference */
    private NodeRef parent;
    
    /** The child association type */
    private QName assocType;
    
    /** The child association name */
    private QName assocName;
    
    /**
     * Constructor.  All parameters must be specified.
     * 
     * @param parent        the parent node reference
     * @param assocType     the child association type
     * @param assocName     the child association name
     */
    public ThumbnailParentAssociationDetails(NodeRef parent, QName assocType, QName assocName)
    {
        // Make sure all the details of the parent are provided
        ParameterCheck.mandatory("parent", parent);
        ParameterCheck.mandatory("assocType", assocType);
        ParameterCheck.mandatory("assocName", assocName);
        
        // Set the values
        this.parent = parent;
        this.assocType = assocType;
        this.assocName = assocName;
    }
    
    /**
     * Get the parent node reference
     * 
     * @return  NodeRef     the parent node reference
     */
    public NodeRef getParent()
    {
        return parent;
    }
    
    /**
     * Get the child association type
     * 
     * @return  QName   the child association type
     */
    public QName getAssociationType()
    {
        return assocType;
    }
    
    /**
     * Get the child association name
     * 
     * @return  QName   the child association name
     */
    public QName getAssociationName()
    {
        return assocName;
    }
   
}
