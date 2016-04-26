package org.alfresco.repo.domain.contentdata;

/**
 * Entity bean for <b>alf_content_url</b> queries table.
 * 
 * @author Derek Hulley
 * @since 3.3.5
 */
public class ContentUrlOrphanQuery
{
    private Long maxOrphanTimeExclusive;
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("ContentUrlOrphanQuery")
          .append("[ maxOrphanTimeExclusive=").append(maxOrphanTimeExclusive)
          .append("]");
        return sb.toString();
    }

    public Long getMaxOrphanTimeExclusive()
    {
        return maxOrphanTimeExclusive;
    }

    public void setMaxOrphanTimeExclusive(Long maxOrphanTimeExclusive)
    {
        this.maxOrphanTimeExclusive = maxOrphanTimeExclusive;
    }
}