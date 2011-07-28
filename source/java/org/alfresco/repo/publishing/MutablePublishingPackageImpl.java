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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transfer.manifest.TransferManifestNodeFactory;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.service.cmr.publishing.MutablePublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingPackageEntry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;

/**
 * @author Brian
 * @author Nick Smith
 * 
 */
public class MutablePublishingPackageImpl implements MutablePublishingPackage
{
    private final VersionService versionService;
    private final TransferManifestNodeFactory transferManifestNodeFactory;
    private final Map<NodeRef, PublishingPackageEntry> entryMap = new HashMap<NodeRef, PublishingPackageEntry>();
    private final Set<NodeRef> nodesToPublish = new HashSet<NodeRef>();
    private final Set<NodeRef> nodesToUnpublish= new HashSet<NodeRef>();
    
    /**
     * @param transferManifestNodeFactory
     */
    public MutablePublishingPackageImpl(TransferManifestNodeFactory transferManifestNodeFactory,
            VersionService versionService)
    {
        this.transferManifestNodeFactory = transferManifestNodeFactory;
        this.versionService = versionService;
    }

    /**
    * {@inheritDoc}
     */
    public void addNodesToPublish(NodeRef... nodesToAdd)
    {
        addNodesToPublish(Arrays.asList(nodesToAdd));
    }

    /**
    * {@inheritDoc}
     */
    public void addNodesToPublish(final Collection<NodeRef> nodesToAdd)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                versionNodes(nodesToAdd);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
        nodesToPublish.addAll(nodesToAdd);
    }

    private void versionNodes(Collection<NodeRef> nodesToAdd)
    {
        for (NodeRef nodeRef : nodesToAdd)
        {
            Version version = versionService.createVersion(nodeRef, null);
            String versionLabel = null;
            if(version != null)
            {
                versionLabel = version.getVersionLabel();
            }
            TransferManifestNormalNode payload = (TransferManifestNormalNode) transferManifestNodeFactory.createTransferManifestNode(nodeRef, null);
            if (TransferManifestNormalNode.class.isAssignableFrom(payload.getClass()))
            {
                PublishingPackageEntryImpl publishingPackage = new PublishingPackageEntryImpl(true, nodeRef, payload, versionLabel);
                entryMap.put(nodeRef, publishingPackage);
            }
        }
    }

    /**
    * {@inheritDoc}
     */
    public void addNodesToUnpublish(NodeRef... nodesToRemove)
    {
        addNodesToUnpublish(Arrays.asList(nodesToRemove));
    }

    /**
    * {@inheritDoc}
     */
    public void addNodesToUnpublish(Collection<NodeRef> nodesToRemove)
    {
        for (NodeRef nodeRef : nodesToRemove)
        {
            entryMap.put(nodeRef, new PublishingPackageEntryImpl(false, nodeRef, null, null));
        }
        nodesToUnpublish.addAll(nodesToRemove);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Collection<PublishingPackageEntry> getEntries()
    {
        return entryMap.values();
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Set<NodeRef> getNodesToPublish()
    {
        return nodesToPublish;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Set<NodeRef> getNodesToUnpublish()
    {
        return nodesToUnpublish;
    }
    
    /**
    * {@inheritDoc}
    */
    @Override
    public Map<NodeRef, PublishingPackageEntry> getEntryMap()
    {
        return entryMap;
    }
}
