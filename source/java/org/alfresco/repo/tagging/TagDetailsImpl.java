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
package org.alfresco.repo.tagging;

import org.alfresco.service.cmr.tagging.TagDetails;

/**
 * Contains the details of a tag within a specific tag scope.
 * 
 * @author Roy Wetherall
 */
public class TagDetailsImpl implements TagDetails
{
   /** Tag name */
    private String tagName;
    
    /** Tag count */
    private int tagCount;
    
    /**
     * Constructor
     * 
     * @param tagScope  tag scope
     * @param tagName   tag name
     * @param tagCount  tag count
     */
    /*package*/ TagDetailsImpl(String tagName, int tagCount)
    {
        this.tagName = tagName;
        this.tagCount = tagCount;
    }

    /**
     * @see org.alfresco.service.cmr.tagging.TagDetails#getTagName()
     */
    public String getName()
    {
        return this.tagName;
    }

    /**
     * @see org.alfresco.service.cmr.tagging.TagDetails#getTagCount()
     */
    public int getCount()
    {
        return this.tagCount;
    }
    
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() 
    {
        return this.tagName.hashCode();
    }
    
    /**
     * Increment the tag count.
     */
    /*protected*/ void incrementCount()
    {
        this.tagCount = this.tagCount + 1;
    }
    
    /**
     * Decrement the tag count
     */
    /*protected*/ void decrementCount()
    {
        this.tagCount = tagCount - 1;
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) 
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof TagDetailsImpl)
        {
            TagDetailsImpl that = (TagDetailsImpl) obj;
            return (this.tagName.equals(that.tagName));
        }
        else
        {
            return false;
        }
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(TagDetails o)
    {
        int result = 0;
        if (this.tagCount < o.getCount())
        {
            result = 1;
        }
        else if (this.tagCount > o.getCount())
        {
            result =  -1;
        }
        return result;
    }
    
    public String toString()
    {
       return "Tag: '" + tagName + "' @ " + tagCount + " instances";
    }
}
