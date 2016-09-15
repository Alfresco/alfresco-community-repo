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
package org.alfresco.repo.index.shard;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import org.alfresco.service.cmr.repository.StoreRef;

/**
 * @author Andy
 *
 */
public class Floc implements Serializable
{
    private static final long serialVersionUID = 3198471269440656171L;

    private HashSet<StoreRef> storeRefs = new HashSet<StoreRef>();
    
    private int numberOfShards;
    
    ShardMethodEnum shardMethod;

    private String template;
   
    private boolean hasContent;
    
    private HashMap<String, String> propertyBag = new HashMap<String, String>();
    
    /**
     * 
     */
    public Floc()
    {
    }

    /**
     * @return the storeRefs
     */
    public HashSet<StoreRef> getStoreRefs()
    {
        return storeRefs;
    }

    /**
     * @param storeRefs the storeRefs to set
     */
    public void setStoreRefs(HashSet<StoreRef> storeRefs)
    {
        this.storeRefs = storeRefs;
    }

    /**
     * @return the numberOfShards
     */
    public int getNumberOfShards()
    {
        return numberOfShards;
    }

    /**
     * @param numberOfShards the numberOfShards to set
     */
    public void setNumberOfShards(int numberOfShards)
    {
        this.numberOfShards = numberOfShards;
    }

    /**
     * @return the shardMethod
     */
    public ShardMethodEnum getShardMethod()
    {
        return shardMethod;
    }

    /**
     * @param shardMethod the shardMethod to set
     */
    public void setShardMethod(ShardMethodEnum shardMethod)
    {
        this.shardMethod = shardMethod;
    }

    /**
     * @return the template
     */
    public String getTemplate()
    {
        return template;
    }

    /**
     * @param template the template to set
     */
    public void setTemplate(String template)
    {
        this.template = template;
    }

    /**
     * @return the hasContent
     */
    public boolean hasContent()
    {
        return hasContent;
    }

    /**
     * @param hasContent the hasContent to set
     */
    public void setHasContent(boolean hasContent)
    {
        this.hasContent = hasContent;
    }

    /**
     * @return the propertyBag
     */
    public HashMap<String, String> getPropertyBag()
    {
        return propertyBag;
    }

    /**
     * @param propertyBag the propertyBag to set
     */
    public void setPropertyBag(HashMap<String, String> propertyBag)
    {
        this.propertyBag = propertyBag;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (hasContent ? 1231 : 1237);
        result = prime * result + numberOfShards;
        result = prime * result + ((propertyBag == null) ? 0 : propertyBag.hashCode());
        result = prime * result + ((shardMethod == null) ? 0 : shardMethod.hashCode());
        result = prime * result + ((storeRefs == null) ? 0 : storeRefs.hashCode());
        result = prime * result + ((template == null) ? 0 : template.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Floc other = (Floc) obj;
        if (hasContent != other.hasContent)
            return false;
        if (numberOfShards != other.numberOfShards)
            return false;
        if (propertyBag == null)
        {
            if (other.propertyBag != null)
                return false;
        }
        else if (!propertyBag.equals(other.propertyBag))
            return false;
        if (shardMethod != other.shardMethod)
            return false;
        if (storeRefs == null)
        {
            if (other.storeRefs != null)
                return false;
        }
        else if (!storeRefs.equals(other.storeRefs))
            return false;
        if (template == null)
        {
            if (other.template != null)
                return false;
        }
        else if (!template.equals(other.template))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "Floc [storeRefs="
                + storeRefs + ", numberOfShards=" + numberOfShards + ", shardMethod=" + shardMethod + ", template=" + template + ", hasContent=" + hasContent + ", propertyBag="
                + propertyBag + "]";
    }
    
    

}
