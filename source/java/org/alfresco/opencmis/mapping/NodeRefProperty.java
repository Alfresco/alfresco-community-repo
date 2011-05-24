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
package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Get the CMIS object id property.
 */
public class NodeRefProperty extends AbstractVersioningProperty
{
    public static final String NodeRefPropertyId = "alf:nodeRef";

    /**
     * Construct
     * 
     * @param serviceRegistry
     */
    public NodeRefProperty(ServiceRegistry serviceRegistry)
    {
        super(serviceRegistry, NodeRefPropertyId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.cmis.mapping.AbstractProperty#getValue(org.alfresco.service
     * .cmr.repository.NodeRef)
     */
    public Serializable getValue(NodeRef nodeRef)
    {
        return getLiveNodeRef(nodeRef);
    }
}
