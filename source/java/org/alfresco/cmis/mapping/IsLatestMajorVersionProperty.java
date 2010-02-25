/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.cmis.mapping;

import java.io.Serializable;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;

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
        super(serviceRegistry, CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.property.PropertyAccessor#getValue(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Serializable getValue(NodeRef nodeRef)
    {
        NodeRef versionSeries;
        if (isWorkingCopy(nodeRef) || (versionSeries = getVersionSeries(nodeRef)).equals(nodeRef))
        {
            return false;
        }
        ServiceRegistry serviceRegistry = getServiceRegistry();
        VersionService versionService = serviceRegistry.getVersionService();
        VersionHistory versionHistory = versionService.getVersionHistory(versionSeries);
        if (versionHistory == null)
        {
            return false;
        }
        // Go back in time to the last major version
        Version currentVersion = versionService.getCurrentVersion(versionSeries);
        while (currentVersion != null)
        {
            if (currentVersion.getVersionType() == VersionType.MAJOR)
            {
                return currentVersion.getFrozenStateNodeRef().equals(nodeRef);
            }
            // We got to the current node and its not major. We failed!
            else if (currentVersion.getFrozenStateNodeRef().equals(nodeRef))
            {
                return false;
            }
            currentVersion = versionHistory.getPredecessor(currentVersion);
        }
        return false;
    }
}
