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
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
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
            // Due to the remapping done for us by the versioned node services, we can simply look up the properties
            // containing the component parts of the node ref to map back to the original node
            Map<QName, Serializable> properties = getServiceRegistry().getNodeService().getProperties(nodeRef);
            return new NodeRef((String) properties.get(ContentModel.PROP_STORE_PROTOCOL),
                    (String) properties.get(ContentModel.PROP_STORE_IDENTIFIER), (String) properties
                            .get(ContentModel.PROP_NODE_UUID));
        }
        else if (isWorkingCopy(nodeRef))
        {
            return (NodeRef) getServiceRegistry().getNodeService().getProperty(nodeRef, ContentModel.PROP_COPY_REFERENCE);
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
}
