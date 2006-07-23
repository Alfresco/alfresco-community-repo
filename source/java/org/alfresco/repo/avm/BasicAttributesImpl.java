/**
 * 
 */
package org.alfresco.repo.avm;

import java.io.Serializable;


/**
 * Implementation of the BasicAttributesBean.
 * @author britt
 */
class BasicAttributesImpl implements BasicAttributes, Serializable
{
    private static final long serialVersionUID = -3796354564923670005L;

    /**
     * The creator.
     */
    private String fCreator;
    
    /**
     * The owner.
     */
    private String fOwner;
    
    /**
     * The last modifier.
     */
    private String fLastModifier;
    
    /**
     * The creation date.
     */
    private long fCreateDate;
    
    /**
     * The modification date.
     */
    private long fModDate;
    
    /**
     * The access date.
     */
    private long fAccessDate;
    
    /**
     * Default constructor.
     */
    public BasicAttributesImpl()
    {
    }
    
    /**
     * A Copy constructor.
     * @param other
     */
    public BasicAttributesImpl(BasicAttributes other)
    {
        fCreator = other.getCreator();
        fOwner = other.getOwner();
        fLastModifier = other.getLastModifier();
        fCreateDate = other.getCreateDate();
        fModDate = other.getModDate();
        fAccessDate = other.getAccessDate();
    }
    
    /**
     * Fill in the blanks constructor.
     * @param creator
     * @param owner
     * @param modifier
     * @param createDate
     * @param modDate
     * @param accessDate
     */
    public BasicAttributesImpl(String creator,
                                   String owner,
                                   String modifier,
                                   long createDate,
                                   long modDate,
                                   long accessDate)
    {
        fCreator = creator;
        fOwner = owner;
        fLastModifier = modifier;
        fCreateDate = createDate;
        fModDate = modDate;
        fAccessDate = accessDate;
    }
    
    /**
     * Set the creator.
     * @param creator
     */
    public void setCreator(String creator)
    {
        fCreator = creator;
    }

    /**
     * Get the creator.
     * @return The creator.
     */
    public String getCreator()
    {
        return fCreator;
    }

    /**
     * Set the owner.
     * @param owner
     */
    public void setOwner(String owner)
    {
        fOwner = owner;
    }

    /**
     * Get the owner.
     * @return The owner.
     */
    public String getOwner()
    {
        return fOwner;
    }
    
    /**
     * Set the last modifier.
     * @param lastModifier
     */
    public void setLastModifier(String lastModifier)
    {
        fLastModifier = lastModifier;
    }

    /**
     * Get the last modifier.
     * @return The last modifier.
     */
    public String getLastModifier()
    {
        return fLastModifier;
    }

    /**
     * Set the create date.
     * @param createDate 
     */
    public void setCreateDate(long createDate)
    {
        fCreateDate = createDate;
    }

    /**
     * Get the create date.
     * @return The create date.
     */
    public long getCreateDate()
    {
        return fCreateDate;
    }

    /**
     * Set the modification date.
     * @param modDate
     */
    public void setModDate(long modDate)
    {
        fModDate = modDate;
    }

    /**
     * Get the modification date.
     * @return modDate
     */
    public long getModDate()
    {
        return fModDate;
    }

    // TODO Do we want this?
    /**
     * Set the access date.
     * @param accessDate
     */
    public void setAccessDate(long accessDate)
    {
        fAccessDate = accessDate;
    }

    /**
     * Get the access date.
     * @return The access date.
     */
    public long getAccessDate()
    {
        return fAccessDate;
    }
}
