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
     * @param tagName   tag name
     * @param tagCount  tag count
     */
    /*package*/ TagDetailsImpl(String tagName, int tagCount)
    {
        this.tagName = tagName;
        this.tagCount = tagCount;
    }

    /**
     * @see org.alfresco.service.cmr.tagging.TagDetails#getName()
     */
    public String getName()
    {
        return this.tagName;
    }

    /**
     * @see org.alfresco.service.cmr.tagging.TagDetails#getCount()
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
