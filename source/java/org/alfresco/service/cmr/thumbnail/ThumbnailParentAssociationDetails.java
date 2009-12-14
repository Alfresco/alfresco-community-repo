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
