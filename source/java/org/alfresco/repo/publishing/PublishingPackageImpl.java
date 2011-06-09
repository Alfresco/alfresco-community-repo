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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.transfer.manifest.TransferManifestNode;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.service.cmr.publishing.NodeSnapshot;
import org.alfresco.service.cmr.publishing.PublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingPackageEntry;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 *
 */
public class PublishingPackageImpl implements PublishingPackage
{
    private Map<NodeRef, PublishingPackageEntry> entries = new HashMap<NodeRef,PublishingPackageEntry>(23);
    

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.PublishingPackage#getEntries()
     */
    @Override
    public Collection<PublishingPackageEntry> getEntries()
    {
        return Collections.unmodifiableCollection(entries.values());
    }

    public Map<NodeRef,PublishingPackageEntry> getEntryMap()
    {
        return entries;
    }
    
    public void setEntryMap(Map<NodeRef,PublishingPackageEntry> entryMap)
    {
        entries = new HashMap<NodeRef, PublishingPackageEntry>(entryMap);
    }
    
    protected static class PublishingPackageEntryImpl implements PublishingPackageEntry
    {
        private final boolean publish; 
        private final NodeRef nodeRef;
        private final TransferManifestNormalNode payload;
        
        /**
         * 
         */
        public PublishingPackageEntryImpl(boolean publish, NodeRef nodeRef, TransferManifestNormalNode payload)
        {
            this.publish = publish;
            this.nodeRef = nodeRef;
            this.payload = payload;
            
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.publishing.PublishingPackageEntry#getNodeRef()
         */
        @Override
        public NodeRef getNodeRef()
        {
            return nodeRef;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.publishing.PublishingPackageEntry#isPublish()
         */
        @Override
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

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.publishing.PublishingPackageEntry#getSnapshot()
         */
        @Override
        public NodeSnapshot getSnapshot()
        {
            return new NodeSnapshotTransferImpl(payload);
        }
    }
}
