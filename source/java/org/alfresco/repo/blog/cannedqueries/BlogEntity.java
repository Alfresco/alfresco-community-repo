package org.alfresco.repo.blog.cannedqueries;

import org.alfresco.repo.query.NodeBackedEntity;

/**
 * Blog Entity - used by GetBlogs CQ
 *
 * @author janv
 * @since 4.0
 */
public class BlogEntity extends NodeBackedEntity
{
    private String publishedDate;
    private String postedDate;
    
    // Supplemental query-related parameters
    private Long publishedQNameId;
    
    private Long blogIntAspectQNameId;
    private Long blogIntPostedQNameId;
    
    /**
     * Default constructor
     */
    public BlogEntity()
    {
        super();
    }
    
    public BlogEntity(Long parentNodeId, Long nameQNameId, Long publishedQNameId, Long contentTypeQNameId, Long blogIntAspectQNameId, Long blogIntPostedQNameId)
    {
        super(parentNodeId, nameQNameId, contentTypeQNameId);
        this.publishedQNameId = publishedQNameId;
        
        this.blogIntAspectQNameId = blogIntAspectQNameId;
        this.blogIntPostedQNameId = blogIntPostedQNameId;
    }
    
    // (ISO-8061)
    public String getPublishedDate()
    {
        return publishedDate;
    }
    
    public void setPublishedDate(String published)
    {
        this.publishedDate = published;
    }
    
    // (ISO-8061)
    public String getPostedDate()
    {
        return postedDate;
    }
    
    public void setPostedDate(String postedDateISO8061)
    {
        this.postedDate = postedDateISO8061;
    }
    
    // Supplemental query-related parameters
    
    public Long getPublishedQNameId()
    {
        return publishedQNameId;
    }
    
    public Long getBlogIntAspectQNameId()
    {
        return blogIntAspectQNameId;
    }
    
    public Long getBlogIntPostedQNameId()
    {
        return blogIntPostedQNameId;
    }
}