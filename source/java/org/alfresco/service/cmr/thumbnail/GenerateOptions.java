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
package org.alfresco.service.cmr.thumbnail;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * This class provides the thumbnail generate options to the thumbnail service.
 * 
 * @author Roy Wetherall
 */
public class GenerateOptions
{
    /** Parent association details */
    private ParentAssociationDetails assocDetails;
    
    /** Name of the thumbnail */
    private String thumbnailName;
    
    /**
     * Default constructor.
     */
    public GenerateOptions()
    {       
    }    
    
    /**
     * Constructor.  Specify the name of the thumbnail.
     * 
     * @param thumbnailName the name of the thumbnail, can be null
     */
    public GenerateOptions(String thumbnailName)
    {
        this.thumbnailName= thumbnailName;
    }
    
    /**
     * Constructor.  Specify the parent association details of the thumbnail.
     * 
     * @param thumnailName  the name of the thumbnail, can be null
     * @param parent        the parent node reference
     * @param assocType     the child association type
     * @param asscoName     the child association name
     */
    public GenerateOptions(String thumbnailName, NodeRef parent, QName assocType, QName assocName)
    {
        this.assocDetails = new ParentAssociationDetails(parent, assocType, assocName);
        this.thumbnailName = thumbnailName;
    }
    
    /**
     * Gets the name of the thumbnail
     * 
     * @return String   the name of the thumbnail, null if non specified
     */
    public String getThumbnailName()
    {
        return thumbnailName;
    }
    
    /**
     * Get the parent association details
     * 
     * @return  ParentAssociationDetails    the parent association details
     */
    public ParentAssociationDetails getParentAssociationDetails()
    {
        return this.assocDetails;
    }
    
    /**
     * Encapsulates the details of a thumbnails parent association
     */
    public class ParentAssociationDetails
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
        public ParentAssociationDetails(NodeRef parent, QName assocType, QName assocName)
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
}
