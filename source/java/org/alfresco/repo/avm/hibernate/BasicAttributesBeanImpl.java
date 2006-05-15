/**
 * 
 */
package org.alfresco.repo.avm.hibernate;

/**
 * Implementation of the BasicAttributesBean.
 * @author britt
 *
 */
public class BasicAttributesBeanImpl implements BasicAttributesBean
{
    /**
     * The id.
     */
    private Long fID;
    
    /**
     * The version for concurrency control.
     */
    private long fVers;
    
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
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.BasicAttributesBean#getId()
     */
    public Long getId()
    {
        return fID;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.BasicAttributesBean#setId(java.lang.Long)
     */
    public void setId(Long id)
    {
        fID = id;
    }

    /**
     * Default constructor.
     */
    public BasicAttributesBeanImpl()
    {
    }
    
    /**
     * A Copy constructor.
     * @param other
     */
    public BasicAttributesBeanImpl(BasicAttributesBean other)
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
    public BasicAttributesBeanImpl(String creator,
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
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.BasicAttributesBean#getVers()
     */
    public long getVers()
    {
        return fVers;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.BasicAttributesBean#setVers(long)
     */
    public void setVers(long vers)
    {
        fVers = vers;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.BasicAttributesBean#setCreator(java.lang.String)
     */
    public void setCreator(String creator)
    {
        fCreator = creator;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.BasicAttributesBean#getCreator()
     */
    public String getCreator()
    {
        return fCreator;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.BasicAttributesBean#setOwner(java.lang.String)
     */
    public void setOwner(String owner)
    {
        fOwner = owner;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.BasicAttributesBean#getOwner()
     */
    public String getOwner()
    {
        return fOwner;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.BasicAttributesBean#setLastModifier(java.lang.String)
     */
    public void setLastModifier(String lastModifier)
    {
        fLastModifier = lastModifier;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.BasicAttributesBean#getLastModifier()
     */
    public String getLastModifier()
    {
        return fLastModifier;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.BasicAttributesBean#setCreateDate(long)
     */
    public void setCreateDate(long createDate)
    {
        fCreateDate = createDate;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.BasicAttributesBean#getCreateDate()
     */
    public long getCreateDate()
    {
        return fCreateDate;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.BasicAttributesBean#setModDate(long)
     */
    public void setModDate(long modDate)
    {
        fModDate = modDate;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.BasicAttributesBean#getModDate()
     */
    public long getModDate()
    {
        return fModDate;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.BasicAttributesBean#setAccessDate(long)
     */
    public void setAccessDate(long accessDate)
    {
        fAccessDate = accessDate;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.BasicAttributesBean#getAccessDate()
     */
    public long getAccessDate()
    {
        return fAccessDate;
    }
}
