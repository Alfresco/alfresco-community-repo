/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
    private QName ancestorType;
    private Map<QName,Serializable> properties;
    private Set<QName> aspects;
    private List<ChildAssociationRef> childAssocs;
    private List<ChildAssociationRef> parentAssocs;
    private List<AssociationRef> sourceAssocs;
    private List<AssociationRef> targetAssocs;
    private Path parentPath;
    private ManifestAccessControl accessControl;

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

    public void setAccessControl(ManifestAccessControl accessControl)
    {
        this.accessControl = accessControl;
    }

    public ManifestAccessControl getAccessControl()
    {
        return accessControl;
    }

    public QName getAncestorType()
    {
        return ancestorType;
    }

    public void setAncestorType(QName ancestorType)
    {
        this.ancestorType = ancestorType;
    }

}
