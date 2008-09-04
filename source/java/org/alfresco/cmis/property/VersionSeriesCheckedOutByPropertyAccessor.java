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
package org.alfresco.cmis.property;

import java.io.Serializable;

import org.alfresco.cmis.dictionary.CMISMapping;
import org.alfresco.cmis.dictionary.CMISScope;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Get the CMIS version series checked out by property
 * @author andyh
 *
 */
public class VersionSeriesCheckedOutByPropertyAccessor extends AbstractNamedPropertyAccessor
{

    public Serializable getProperty(NodeRef nodeRef)
    {
        if (getServiceRegistry().getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            return null;
        }
        else
        {
            LockType type = getServiceRegistry().getLockService().getLockType(nodeRef);
            if (type == LockType.READ_ONLY_LOCK)
            {
                NodeRef wc = getServiceRegistry().getCheckOutCheckInService().getWorkingCopy(nodeRef);
                if (wc != null)
                {
                    return getServiceRegistry().getNodeService().getProperty(nodeRef, ContentModel.PROP_LOCK_OWNER);
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
    }

    @Override
    public String getPropertyName()
    {
        return CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY;
    }

    @Override
    public CMISScope getScope()
    {
        return CMISScope.DOCUMENT;
    }
}
