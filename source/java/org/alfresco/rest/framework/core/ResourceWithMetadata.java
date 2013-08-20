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
     * @return
     */
    @JsonIgnore
    public Object getResource()
    {
        return this.resource;
    }

    /**
     * Returns the meta data for this resource
     * @return
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
