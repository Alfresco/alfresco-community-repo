/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

package org.alfresco.rest.api.model;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class CustomModelDownload implements Comparable<CustomModelDownload>
{
    private String nodeRef;

    public CustomModelDownload()
    {
    }

    public CustomModelDownload(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef.toString();
    }

    public String getNodeRef()
    {
        return this.nodeRef;
    }

    public void setNodeRef(String nodeRef)
    {
        this.nodeRef = nodeRef;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.nodeRef == null) ? 0 : this.nodeRef.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof CustomModelDownload))
        {
            return false;
        }
        CustomModelDownload other = (CustomModelDownload) obj;
        if (this.nodeRef == null)
        {
            if (other.nodeRef != null)
            {
                return false;
            }
        }
        else if (!this.nodeRef.equals(other.nodeRef))
        {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(CustomModelDownload other)
    {
        return this.nodeRef.toString().compareTo(other.getNodeRef().toString());
    }
}
