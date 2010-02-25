/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.cmis.mapping;

import java.io.Serializable;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;

/**
 * Accessor for the CMIS Checkin Comment
 * 
 * @author dward
 */
public class CheckinCommentProperty extends AbstractVersioningProperty
{
    /**
     * Construct
     * 
     * @param serviceRegistry
     */
    public CheckinCommentProperty(ServiceRegistry serviceRegistry)
    {
        super(serviceRegistry, CMISDictionaryModel.PROP_CHECKIN_COMMENT);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyAccessor#getValue(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Serializable getValue(NodeRef nodeRef)
    {
        NodeRef versionSeries;
        if (isWorkingCopy(nodeRef) || (versionSeries = getVersionSeries(nodeRef)).equals(nodeRef))
        {
            return null;
        }
        ServiceRegistry serviceRegistry = getServiceRegistry();
        String versionLabel = (String)serviceRegistry.getNodeService().getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);
        if (versionLabel == null)
        {
            return null;
        }        
        VersionHistory versionHistory = serviceRegistry.getVersionService().getVersionHistory(versionSeries);
        if (versionHistory == null)
        {
            return null;
        }
        Version version = versionHistory.getVersion(versionLabel);
        if (version == null)
        {
            return null;
        }
        return version.getDescription();
    }
}
