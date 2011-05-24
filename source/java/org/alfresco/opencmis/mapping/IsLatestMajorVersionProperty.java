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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Accessor for CMIS is latest major version property
 * 
 * @author dward
 */
public class IsLatestMajorVersionProperty extends AbstractVersioningProperty
{
    /**
     * Construct
     * 
     * @param serviceRegistry
     */
    public IsLatestMajorVersionProperty(ServiceRegistry serviceRegistry)
    {
        super(serviceRegistry, PropertyIds.IS_LATEST_MAJOR_VERSION);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.cmis.property.PropertyAccessor#getValue(org.alfresco.service
     * .cmr.repository.NodeRef)
     */
    public Serializable getValue(NodeRef nodeRef)
    {
        if (isWorkingCopy(nodeRef))
        {
            return false;
        }
        NodeRef versionSeries = getVersionSeries(nodeRef);
        ServiceRegistry serviceRegistry = getServiceRegistry();
        VersionService versionService = serviceRegistry.getVersionService();
        VersionHistory versionHistory = versionService.getVersionHistory(versionSeries);
        if (versionHistory == null)
        {
            return false;
        }

        NodeRef versionNodeRef = nodeRef;
        if (!nodeRef.getStoreRef().getProtocol().equals(VersionBaseModel.STORE_PROTOCOL))
        {
            String versionLabel = (String) serviceRegistry.getNodeService().getProperty(nodeRef,
                    ContentModel.PROP_VERSION_LABEL);
            if (versionLabel == null)
            {
                return false;
            }
            Version version = versionHistory.getVersion(versionLabel);
            if (version == null)
            {
                return false;
            }
            versionNodeRef = version.getFrozenStateNodeRef();
        }

        // Go back in time to the last major version
        Version currentVersion = versionService.getCurrentVersion(versionSeries);
        while (currentVersion != null)
        {
            if (currentVersion.getVersionType() == VersionType.MAJOR)
            {
                return currentVersion.getFrozenStateNodeRef().equals(versionNodeRef);
            }
            // We got to the current node and its not major. We failed!
            else if (currentVersion.getFrozenStateNodeRef().equals(versionNodeRef))
            {
                return false;
            }
            currentVersion = versionHistory.getPredecessor(currentVersion);
        }
        return false;
    }
}
