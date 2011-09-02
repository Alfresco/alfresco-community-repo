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

package org.alfresco.repo.publishing;

import java.io.Serializable;
import java.util.Collections;
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
 */
public class NodeSnapshotTransferImpl implements NodeSnapshot
{
    private final TransferManifestNormalNode transferNode; 
    
    /**
     * @param transferNode
     */
    public NodeSnapshotTransferImpl(TransferManifestNormalNode transferNode)
    {
        this.transferNode = transferNode;
    }

    public List<ChildAssociationRef> getAllParentAssocs()
    {
        if (transferNode == null)
        {
            return Collections.emptyList();
        }
        return transferNode.getParentAssocs();
    }

    /**
    * {@inheritDoc}
     */
    public Set<QName> getAspects()
    {
        if (transferNode == null)
        {
            return Collections.emptySet();
        }
        return transferNode.getAspects();
    }

    /**
     * {@inheritDoc}
      */
    public NodeRef getNodeRef()
    {
        if (transferNode == null)
        {
            return null;
        }
        return transferNode.getNodeRef();
    }

    /**
     * @return
     */
    public List<AssociationRef> getOutboundPeerAssociations()
    {
        if (transferNode == null)
        {
            return Collections.emptyList();
        }
        return transferNode.getTargetAssocs();
    }

    /**
     * @return
     */
    public ChildAssociationRef getPrimaryParentAssoc()
    {
        if (transferNode == null)
        {
            return null;
        }
        return transferNode.getPrimaryParentAssoc();
    }

    /**
     * @return
     */
    public Path getPrimaryPath()
    {
        if (transferNode == null)
        {
            return null;
        }
        return transferNode.getParentPath();
    }

    /**
     * {@inheritDoc}
     */
    public Map<QName, Serializable> getProperties()
    {
        if (transferNode == null)
        {
            return Collections.emptyMap();
        }
        return transferNode.getProperties();
    }

    /**
     * {@inheritDoc}
     */
    public QName getType()
    {
        if (transferNode == null)
        {
            return null;
        }
        return transferNode.getType();
    }

    /**
    * {@inheritDoc}
    */
    public String getVersion()
    {
        return (String) getProperties().get(ContentModel.PROP_VERSION_LABEL);
    }
    
    /**
     * @return the transferNode
     */
    public TransferManifestNormalNode getTransferNode()
    {
        return transferNode;
    }
}
