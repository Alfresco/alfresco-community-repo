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

package org.alfresco.repo.forms.processor.node;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

/**
 * Simple data transfer object used by the ContentModelFormProcessor and its
 * descendants.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class ContentModelItemData<ItemType> implements TransientValueGetter
{
    private final ItemType item;
    private final Map<QName, PropertyDefinition> propDefs;
    private final Map<QName, AssociationDefinition> assocDefs;
    private final Map<QName, Serializable> propValues;
    private final Map<QName, Serializable> assocValues;
    private final Map<String, Object> transientValues;

    public ContentModelItemData(ItemType item,
                Map<QName, PropertyDefinition> propDefs,
                Map<QName, AssociationDefinition> assocDefs,
                Map<QName, Serializable> propValues,
                Map<QName, Serializable> assocValues,
                Map<String, Object> transientValues)
    {
        this.item = item;
        this.propDefs = propDefs;
        this.assocDefs = assocDefs;
        this.propValues = propValues;
        this.assocValues = assocValues;
        this.transientValues = transientValues;
    }

    /**
     * @return the item
     */
    public ItemType getItem()
    {
        return this.item;
    }

    /**
     * @return the property value associated with the <code>key</code> or
     *         <code>null</null> if none exists.
     */
    public Serializable getPropertyValue(QName key)
    {
        return getValue(key, propValues);
    }

    /**
     * @return the association value associated with the <code>key</code> or
     *         <code>null</null> if none exists.
     */
    public Serializable getAssociationValue(QName key)
    {
        return getValue(key, assocValues);
    }

    /**
     * @return the value associated with the transient property specified by the
     *         fieldName or <code>null</null> if none exists.
     */
    public Object getTransientValue(String fieldName)
    {
        Object value = null;
        
        if (transientValues != null)
        {
            value = transientValues.get(fieldName);
        }
        
        return value;
    }

    private Serializable getValue(QName key, Map<QName, Serializable> values)
    {
        Serializable value = null;
        if (values != null)
        {
            value = values.get(key);
        }
        return value;
    }

    /**
     * @return The PropertyDefinition associated with the <code>propName</code>
     *         or <code>null</code> if none exists.
     */
    public PropertyDefinition getPropertyDefinition(QName propName)
    {
        PropertyDefinition propDef = null;
        if (propDefs != null)
        {
            propDef = propDefs.get(propName);
        }
        return propDef;
    }

    /**
     * @return The AssociationDefinition associated with the
     *         <code>assocName</code> or <code>null</code> if none exists.
     */
    public AssociationDefinition getAssociationDefinition(QName assocName)
    {
        AssociationDefinition assocDef = null;
        if (assocDefs != null)
        {
            assocDef = assocDefs.get(assocName);
        }
        return assocDef;
    }

    /**
     * @return Returns an unmodifiable Collection containing all the association
     *         definition {@link QName QNames} for the item.
     */
    public Collection<QName> getAllAssociationDefinitionNames()
    {
        if (assocDefs == null)
        {
            return Collections.emptyList();
        }
        else
        {
            return Collections.unmodifiableCollection(assocDefs.keySet());
        }
    }

    /**
     * @return Returns an unmodifiable Collection containing all the property
     *         definitions for the item.
     */
    public Collection<QName> getAllPropertyDefinitionNames()
    {
        if (propDefs == null)
        {
            return Collections.emptyList();
        }
        else
        {
            return Collections.unmodifiableCollection(propDefs.keySet());
        }
    }

    /**
     * @return Returns an unmodifiable Collection containing all the property
     *         definitions for the item.
     */
    public Collection<String> getAllTransientFieldNames()
    {
        if (transientValues == null)
        {
            return Collections.emptyList();
        }
        else
        {
            return Collections.unmodifiableCollection(transientValues.keySet());
        }
    }
}
