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

import java.util.Arrays;
import java.util.Collection;

import org.alfresco.repo.transfer.manifest.TransferManifestNode;
import org.alfresco.repo.transfer.manifest.TransferManifestNodeFactory;
import org.alfresco.service.cmr.publishing.MutablePublishingPackage;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 *
 */
public class MutablePublishingPackageImpl extends PublishingPackageImpl implements MutablePublishingPackage
{

    private TransferManifestNodeFactory transferManifestNodeFactory;
    
    /**
     * @param transferManifestNodeFactory
     */
    public MutablePublishingPackageImpl(TransferManifestNodeFactory transferManifestNodeFactory)
    {
        super();
        this.transferManifestNodeFactory = transferManifestNodeFactory;
    }

    /**
     * @param transferManifestNodeFactory the transferManifestNodeFactory to set
     */
    public void setTransferManifestNodeFactory(TransferManifestNodeFactory transferManifestNodeFactory)
    {
        this.transferManifestNodeFactory = transferManifestNodeFactory;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.MutablePublishingPackage#addNodesToPublish(org.alfresco.service.cmr.repository.NodeRef[])
     */
    @Override
    public void addNodesToPublish(NodeRef... nodesToPublish)
    {
        addNodesToPublish(Arrays.asList(nodesToPublish));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.MutablePublishingPackage#addNodesToPublish(java.util.Collection)
     */
    @Override
    public void addNodesToPublish(Collection<NodeRef> nodesToPublish)
    {
        for (NodeRef nodeRef : nodesToPublish)
        {
            TransferManifestNode payload = transferManifestNodeFactory.createTransferManifestNode(nodeRef, null);
            getEntryMap().put(nodeRef, new PublishingPackageEntryImpl(true, payload));
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.MutablePublishingPackage#addNodesToUnpublish(org.alfresco.service.cmr.repository.NodeRef[])
     */
    @Override
    public void addNodesToUnpublish(NodeRef... nodesToRemove)
    {
        addNodesToUnpublish(Arrays.asList(nodesToRemove));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.MutablePublishingPackage#addNodesToUnpublish(java.util.Collection)
     */
    @Override
    public void addNodesToUnpublish(Collection<NodeRef> nodesToRemove)
    {
        for (NodeRef nodeRef : nodesToRemove)
        {
            //FIXME: BJR: 20110513: Handle unpublish case correctly
            TransferManifestNode payload = transferManifestNodeFactory.createTransferManifestNode(nodeRef, null);
            getEntryMap().put(nodeRef, new PublishingPackageEntryImpl(false, payload));
        }
    }
}
