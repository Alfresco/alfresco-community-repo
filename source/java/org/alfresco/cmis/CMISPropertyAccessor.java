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
package org.alfresco.cmis;

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
     * @return
     */
    String getName();

    /**
     * Get the (directly) mapped Alfresco property (if a direct mapping exists)
     * 
     * @return
     */
    QName getMappedProperty();

    /**
     * Get the property value for a node or an association
     * 
     * @param nodeRef
     * @return
     */
    public Serializable getValue(NodeRef nodeRef);

    /**
     * Set the property value for a node
     * 
     * @param nodeRef
     * @Param value
     */
    void setValue(NodeRef nodeRef, Serializable value);

    /**
     * Get the property value for an association
     * 
     * @param nodeRef
     * @return
     */
    public Serializable getValue(AssociationRef assocRef);
}
