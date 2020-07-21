/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.opencmis.dictionary;

import java.io.Serializable;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * CMIS Property Accessor (get and set property values)
 * 
 * @author davidc
 */
public interface CMISPropertyAccessor
{
    /**
     * Get the CMIS Property Name
     * 
     * @return String
     */
    String getName();

    /**
     * Get the (directly) mapped Alfresco property (if a direct mapping exists)
     * 
     * @return QName
     */
    QName getMappedProperty();

    /**
     * Set the property value for a node
     * 
     * @param nodeRef NodeRef
     * @param value Serializable
     */
    void setValue(NodeRef nodeRef, Serializable value);

    /**
     * Get the property value for a node or an association
     * 
     * @param nodeInfo CMISNodeInfo
     * @return Serializable
     */
    Serializable getValue(CMISNodeInfo nodeInfo);

    /**
     * Creates a node info object form the given node ref.
     */
    CMISNodeInfo createNodeInfo(NodeRef nodeRef);

    /**
     * Creates a node info object form the given association ref.
     */
    CMISNodeInfo createNodeInfo(AssociationRef assocRef);
}
