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
package org.alfresco.repo.domain.propval;

import java.io.Serializable;

import org.alfresco.util.Pair;

/**
 * Entity bean for <b>alf_prop_serializable_value</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertySerializableValueEntity
{
    private Long id;
    private Serializable serializableValue;
    
    public PropertySerializableValueEntity()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("PropertySerializableValueEntity")
          .append("[ id=").append(id)
          .append(", value=").append(serializableValue)
          .append("]");
        return sb.toString();
    }
    
    /**
     * @return          Returns the ID-value pair
     */
    public Pair<Long, Serializable> getEntityPair()
    {
        return new Pair<Long, Serializable>(id, serializableValue);
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Serializable getSerializableValue()
    {
        return serializableValue;
    }

    public void setSerializableValue(Serializable serializableValue)
    {
        this.serializableValue = serializableValue;
    }    
}
