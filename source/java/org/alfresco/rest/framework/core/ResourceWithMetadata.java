/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.framework.core;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * This wraps a resource object with its metadata.
 * 
 * A single source for both the information about the resource
 * and the resource itself
 *
 * @author Gethin James
 */
public class ResourceWithMetadata
{
    private Object resource = null;
    private ResourceMetadata metaData;

    public ResourceWithMetadata(Object resource, ResourceMetadata metaData)
    {
        super();
        this.resource = resource;
        this.metaData = metaData;
    }

    /**
     * Returns the REST resource object
     * @return Object
     */
    @JsonIgnore
    public Object getResource()
    {
        return this.resource;
    }

    /**
     * Returns the meta data for this resource
     * @return ResourceMetadata
     */
    public ResourceMetadata getMetaData()
    {
        return this.metaData;
    }

    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ResourceWithMetadata [metaData=");
        builder.append(this.metaData);
        builder.append(", resource=");
        builder.append(this.resource);
        builder.append("]");
        return builder.toString();
    }

    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.metaData == null) ? 0 : this.metaData.hashCode());
        result = prime * result + ((this.resource == null) ? 0 : this.resource.hashCode());
        return result;
    }

    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ResourceWithMetadata other = (ResourceWithMetadata) obj;
        if (this.metaData == null)
        {
            if (other.metaData != null) return false;
        }
        else if (!this.metaData.equals(other.metaData)) return false;
        if (this.resource == null)
        {
            if (other.resource != null) return false;
        }
        else if (!this.resource.equals(other.resource)) return false;
        return true;
    }
}
