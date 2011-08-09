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

package org.alfresco.service.cmr.publishing;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author Brian
 * @author Nick Smith
 * @since 4.0
 *
 */
public interface NodeSnapshot
{
    /**
     * Retrieve the identifier of the node of which this is a snapshot
     * @return The NodeRef object that identifies the node
     */
    NodeRef getNodeRef();
    
    /**
     * The property values assigned to the node at the moment the snapshot was taken.
     * @return A map that associates property names to property values for the node.
     */
    Map<QName, Serializable> getProperties();

    /**
     * Retrieve the type of the node at the moment the snapshot was taken.
     * @return The QName that identifies the type of the node
     */
    QName getType();
    
    /**
     * Retrieve all the aspects that were applied to the node at the moment the snapshot was taken
     * @return A set of QName objects, each identifying an aspect that is applied to the node
     */
    Set<QName> getAspects();
    
    /**
     * @return the version of the node when the snapshot was taken.
     */
    String getVersion();
}
