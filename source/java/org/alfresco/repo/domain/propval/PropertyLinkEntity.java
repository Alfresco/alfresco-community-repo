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

/**
 * Entity bean for <b>alf_prop_link</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyLinkEntity
{
    private Long rootPropId;
    private Long propIndex;
    private Long containedIn;
    private Long keyPropId;
    private Long valuePropId;
    
    public PropertyLinkEntity()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("PropertyLinkEntity")
          .append("[ rootPropId=").append(rootPropId)
          .append(", propIndex=").append(propIndex)
          .append(", containedIn=").append(containedIn)
          .append(", keyPropId=").append(keyPropId)
          .append(", valuePropId=").append(valuePropId)
          .append("]");
        return sb.toString();
    }

    public Long getRootPropId()
    {
        return rootPropId;
    }

    public void setRootPropId(Long rootPropId)
    {
        this.rootPropId = rootPropId;
    }

    public Long getPropIndex()
    {
        return propIndex;
    }

    public void setPropIndex(Long propIndex)
    {
        this.propIndex = propIndex;
    }

    public Long getContainedIn()
    {
        return containedIn;
    }

    public void setContainedIn(Long containedIn)
    {
        this.containedIn = containedIn;
    }

    public Long getKeyPropId()
    {
        return keyPropId;
    }

    public void setKeyPropId(Long keyPropId)
    {
        this.keyPropId = keyPropId;
    }

    public Long getValuePropId()
    {
        return valuePropId;
    }

    public void setValuePropId(Long valuePropId)
    {
        this.valuePropId = valuePropId;
    }
}
