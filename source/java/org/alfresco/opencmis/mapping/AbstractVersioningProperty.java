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
package org.alfresco.opencmis.mapping;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.namespace.QName;

/**
 * Base class for versioning property accessors.
 * 
 * @author dward
 * 
 */
public abstract class AbstractVersioningProperty extends AbstractProperty
{

    /**
     * Construct
     * 
     * @param serviceRegistry
     * @param propertyName
     */
    protected AbstractVersioningProperty(ServiceRegistry serviceRegistry, String propertyName)
    {
        super(serviceRegistry, propertyName);
    }

    public NodeRef getVersionSeries(NodeRef nodeRef)
    {
        if (nodeRef.getStoreRef().getProtocol().equals(VersionBaseModel.STORE_PROTOCOL))
        {
            // Due to the remapping done for us by the versioned node services,
            // we can simply look up the properties
            // containing the component parts of the node ref to map back to the
            // original node
            Map<QName, Serializable> properties = getServiceRegistry().getNodeService().getProperties(nodeRef);
            return new NodeRef((String) properties.get(ContentModel.PROP_STORE_PROTOCOL),
                    (String) properties.get(ContentModel.PROP_STORE_IDENTIFIER),
                    (String) properties.get(ContentModel.PROP_NODE_UUID));
        } else if (isWorkingCopy(nodeRef))
        {
            return (NodeRef) getServiceRegistry().getNodeService().getProperty(nodeRef,
                    ContentModel.PROP_COPY_REFERENCE);
        }
        return nodeRef;
    }

    public boolean isWorkingCopy(NodeRef nodeRef)
    {
        return getServiceRegistry().getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY);
    }

    public boolean hasWorkingCopy(NodeRef nodeRef)
    {
        return getServiceRegistry().getLockService().getLockType(nodeRef) == LockType.READ_ONLY_LOCK;
    }

    public NodeRef getLiveNodeRef(NodeRef nodeRef)
    {
        if (nodeRef.getStoreRef().getProtocol().equals(VersionBaseModel.STORE_PROTOCOL))
        {
            VersionHistory versionHistory = getServiceRegistry().getVersionService().getVersionHistory(nodeRef);
            if (versionHistory == null)
            {
                return nodeRef;
            }

            Version currentVersion = versionHistory.getHeadVersion();
            Serializable versionLabel = getServiceRegistry().getNodeService().getProperty(nodeRef,
                    ContentModel.PROP_VERSION_LABEL);

            if (currentVersion.getVersionLabel().equals(versionLabel))
            {
                return currentVersion.getVersionedNodeRef();
            }
        }

        return nodeRef;
    }

    public boolean isCurrentVersion(NodeRef nodeRef)
    {
        if (nodeRef.getStoreRef().getProtocol().equals(VersionBaseModel.STORE_PROTOCOL))
        {
            VersionHistory versionHistory = getServiceRegistry().getVersionService().getVersionHistory(nodeRef);
            if (versionHistory == null)
            {
                return true;
            }

            Version currentVersion = versionHistory.getHeadVersion();
            Serializable versionLabel = getServiceRegistry().getNodeService().getProperty(nodeRef,
                    ContentModel.PROP_VERSION_LABEL);

            return currentVersion.getVersionLabel().equals(versionLabel);
        }

        return true;
    }
}
