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

import org.alfresco.repo.transfer.manifest.TransferManifestNode;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.service.cmr.publishing.NodeSnapshot;
import org.alfresco.service.cmr.publishing.PublishingPackageEntry;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @author Nick Smith
 * @since 4.0
 *
 */
// Package protected
class PublishingPackageEntryImpl implements PublishingPackageEntry
{
    private final boolean publish; 
    private final NodeRef nodeRef;
    private final TransferManifestNormalNode payload;
    private final String version;
    
    public PublishingPackageEntryImpl(boolean publish,
            NodeRef nodeRef, TransferManifestNormalNode payload,
            String version)
    {
        this.publish = publish;
        this.nodeRef = nodeRef;
        this.payload = payload;
        this.version = version;
    }
    
    /**
    * {@inheritDoc}
     */
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    /**
     * {@inheritDoc}
      */
    public boolean isPublish()
    {
        return publish;
    }

    /**
     * @return the payload
     */
    public TransferManifestNode getPayload()
    {
        return payload;
    }

    /**
     * {@inheritDoc}
      */
    public NodeSnapshot getSnapshot()
    {
        //TODO Add versioning information.
        return new NodeSnapshotTransferImpl(payload, version);
    }
}
