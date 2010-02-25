/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.transfer.manifest;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;

/**
 * Data value object - part of the transfer manifest
 * 
 * Represents a single node and either a create or an update.
 *
 * @author Mark Rogers
 */
public class TransferManifestNormalNode implements TransferManifestNode
{
    private NodeRef nodeRef;   
    private ChildAssociationRef primaryParentAssoc;
    private String uuid;
    private QName type;
    private Map<QName,Serializable> properties;
    private Set<QName> aspects;
    private List<ChildAssociationRef> childAssocs;
    private List<ChildAssociationRef> parentAssocs;
    private List<AssociationRef> sourceAssocs;
    private List<AssociationRef> targetAssocs; 
    private Path parentPath; 

    public void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }

    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    public String getUuid()
    {
        return uuid;
    }
       
    /**
     * Gets all properties for the node
     * 
     * @return the properties
     */
    public Map<QName,Serializable> getProperties()
    {
        return properties;
    }
//
//    /**
//     * Gets the property data type
//     * 
//     * @param propertyName  name of property
//     * @return  data type of named property
//     */
//    public DataTypeDefinition getPropertyDataType(QName propertyName);
//    
//    /**
//     * @return  the aspects of this node
//     */
//    public Set<QName> getNodeAspects();
//    
//    /**
//     * @return  true => the node inherits permissions from its parent
//     */
//    public boolean getInheritPermissions();
//    
//    /**
//     * @return  the permissions applied to this node
//     */
//    public List<AccessPermission> getAccessControlEntries();

    public void setProperties(Map<QName,Serializable> properties)
    {
        this.properties = properties;
    }

    public void setAspects(Set<QName> aspects)
    {
        this.aspects = aspects;
    }

    public Set<QName> getAspects()
    {
        return aspects;
    }

    public void setType(QName type)
    {
        this.type = type;
    }

    public QName getType()
    {
        return type;
    }

    public void setChildAssocs(List<ChildAssociationRef> childAssocs)
    {
        this.childAssocs = childAssocs;
    }

    public List<ChildAssociationRef> getChildAssocs()
    {
        return childAssocs;
    }

    public void setParentAssocs(List<ChildAssociationRef> parentAssocs)
    {
        this.parentAssocs = parentAssocs;
    }

    public List<ChildAssociationRef> getParentAssocs()
    {
        return parentAssocs;
    }

    public void setParentPath(Path parentPath)
    {
        this.parentPath = parentPath;
    }

    public Path getParentPath()
    {
        return parentPath;
    }

    public void setSourceAssocs(List<AssociationRef> sourceAssocs)
    {
        this.sourceAssocs = sourceAssocs;
    }

    public List<AssociationRef> getSourceAssocs()
    {
        return sourceAssocs;
    }

    public void setTargetAssocs(List<AssociationRef> targetAssocs)
    {
        this.targetAssocs = targetAssocs;
    }

    public List<AssociationRef> getTargetAssocs()
    {
        return targetAssocs;
    }

    public void setPrimaryParentAssoc(ChildAssociationRef primaryParentAssoc)
    {
        this.primaryParentAssoc = primaryParentAssoc;
    }

    public ChildAssociationRef getPrimaryParentAssoc()
    {
        return primaryParentAssoc;
    }

}
