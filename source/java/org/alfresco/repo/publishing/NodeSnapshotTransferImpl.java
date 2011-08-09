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

package org.alfresco.repo.publishing;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.service.cmr.publishing.NodeSnapshot;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;

/**
 * @author Brian
 * @author Nick Smith
 * @since 4.0
 *
 */
public class NodeSnapshotTransferImpl implements NodeSnapshot
{
    private final TransferManifestNormalNode transferNode; 
    private final String version;
    
    /**
     * @param transferNode
     */
    public NodeSnapshotTransferImpl(TransferManifestNormalNode transferNode)
    {
        this.transferNode = transferNode;
        Map<QName, Serializable> props = transferNode.getProperties();
        this.version = (String) props.get(ContentModel.PROP_VERSION_LABEL);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.NodeSnapshot#getAllParentAssocs()
     */
    public List<ChildAssociationRef> getAllParentAssocs()
    {
        return transferNode.getParentAssocs();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.NodeSnapshot#getAspects()
     */
    
    public Set<QName> getAspects()
    {
        return transferNode.getAspects();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.NodeSnapshot#getNodeRef()
     */
    
    public NodeRef getNodeRef()
    {
        return transferNode.getNodeRef();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.NodeSnapshot#getOutboundPeerAssociations()
     */
    
    public List<AssociationRef> getOutboundPeerAssociations()
    {
        return transferNode.getTargetAssocs();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.NodeSnapshot#getPrimaryParentAssoc()
     */
    
    public ChildAssociationRef getPrimaryParentAssoc()
    {
        return transferNode.getPrimaryParentAssoc();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.NodeSnapshot#getPrimaryPath()
     */
    
    public Path getPrimaryPath()
    {
        return transferNode.getParentPath();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.NodeSnapshot#getProperties()
     */
    
    public Map<QName, Serializable> getProperties()
    {
        return transferNode.getProperties();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.NodeSnapshot#getType()
     */
    
    public QName getType()
    {
        return transferNode.getType();
    }

    /**
    * {@inheritDoc}
    */
    
    public String getVersion()
    {
        return version;
    }
    
    /**
     * @return the transferNode
     */
    public TransferManifestNormalNode getTransferNode()
    {
        return transferNode;
    }
}
