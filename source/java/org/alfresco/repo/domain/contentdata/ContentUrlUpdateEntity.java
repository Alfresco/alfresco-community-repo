package org.alfresco.repo.domain.contentdata;


/**
 * Entity bean for updating the <b>alf_content_url</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class ContentUrlUpdateEntity
{
    private Long id;
    private Long orphanTime;
    private Long oldOrphanTime;
    
    public ContentUrlUpdateEntity()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("ContentUrlUpdateEntity")
          .append("[ ID=").append(id)
          .append(", orphanTime=").append(orphanTime)
          .append(", oldOrphanTime=").append(oldOrphanTime)
          .append("]");
        return sb.toString();
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getOrphanTime()
    {
        return orphanTime;
    }

    public void setOrphanTime(Long orphanTime)
    {
        this.orphanTime = orphanTime;
    }

    public Long getOldOrphanTime()
    {
        return oldOrphanTime;
    }

    public void setOldOrphanTime(Long oldOrphanTime)
    {
        this.oldOrphanTime = oldOrphanTime;
    }}
