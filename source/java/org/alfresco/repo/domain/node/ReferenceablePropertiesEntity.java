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
package org.alfresco.repo.domain.node;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

/**
 * Class holding properties associated with the <b>sys:referenceable</b> aspect.
 * This aspect is common enough to warrant direct inclusion on the <b>Node</b> entity.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class ReferenceablePropertiesEntity
{
    private static final Set<QName> REFERENCEABLE_PROP_QNAMES;
    static
    {
        REFERENCEABLE_PROP_QNAMES = new HashSet<QName>(8);
        REFERENCEABLE_PROP_QNAMES.add(ContentModel.PROP_STORE_PROTOCOL);
        REFERENCEABLE_PROP_QNAMES.add(ContentModel.PROP_STORE_IDENTIFIER);
        REFERENCEABLE_PROP_QNAMES.add(ContentModel.PROP_NODE_UUID);
        REFERENCEABLE_PROP_QNAMES.add(ContentModel.PROP_NODE_DBID);
    }
    
    /**
     * @return          Returns <tt>true</tt> if the property belongs to the <b>sys:referenceable</b> aspect
     */
    public static boolean isReferenceableProperty(QName qname)
    {
        return REFERENCEABLE_PROP_QNAMES.contains(qname);
    }
    
    /**
     * Remove all {@link ContentModel#ASPECT_REFERENCEABLE referencable} properties
     */
    public static void removeReferenceableProperties(Node node, Map<QName, Serializable> properties)
    {
        properties.keySet().removeAll(REFERENCEABLE_PROP_QNAMES);
        String name = DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_NAME));
        if (name != null && name.equals(node.getUuid()))
        {
            // The cm:name matches the UUID, so drop it
            properties.remove(ContentModel.PROP_NAME);
        }
    }
    
    /**
     * Remove all {@link ContentModel#ASPECT_REFERENCEABLE referencable} properties
     */
    public static void removeReferenceableProperties(Set<QName> propertyQNames)
    {
        propertyQNames.removeAll(REFERENCEABLE_PROP_QNAMES);
    }
    
    /**
     * Adds all {@link ContentModel#ASPECT_REFERENCEABLE referencable} properties.
     */
    public static void addReferenceableProperties(Node node, Map<QName, Serializable> properties)
    {
        Long nodeId = node.getId();
        NodeRef nodeRef = node.getNodeRef();
        properties.put(ContentModel.PROP_STORE_PROTOCOL, nodeRef.getStoreRef().getProtocol());
        properties.put(ContentModel.PROP_STORE_IDENTIFIER, nodeRef.getStoreRef().getIdentifier());
        properties.put(ContentModel.PROP_NODE_UUID, nodeRef.getId());
        properties.put(ContentModel.PROP_NODE_DBID, nodeId);
        // add the ID as the name, if required
        String name = DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_NAME));
        if (name == null)
        {
            properties.put(ContentModel.PROP_NAME, nodeRef.getId());
        }
    }

    public static Serializable getReferenceableProperty(Node node, QName qname)
    {
        Long nodeId = node.getId();
        NodeRef nodeRef = node.getNodeRef();
        if (qname.equals(ContentModel.PROP_STORE_PROTOCOL))
        {
            return nodeRef.getStoreRef().getProtocol();
        }
        else if (qname.equals(ContentModel.PROP_STORE_IDENTIFIER))
        {
            return nodeRef.getStoreRef().getIdentifier();
        }
        else if (qname.equals(ContentModel.PROP_NODE_UUID))
        {
            return nodeRef.getId();
        }
        else if (qname.equals(ContentModel.PROP_NODE_DBID))
        {
            return nodeId;
        }
        throw new IllegalArgumentException("Not sys:referenceable property: " + qname);
    }
}
