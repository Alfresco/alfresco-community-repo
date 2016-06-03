package org.alfresco.repo.blog.cannedqueries;

import java.util.Date;

/**
 * Parameter objects for {@link GetBlogPostsCannedQuery}.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
public class GetBlogPostsCannedQueryParams extends BlogEntity
{
    private final String cmCreator;
    
    /**
     * <tt>true</tt> means the blog-posts should be cm:published, <tt>false</tt> means they should not.
     */
    private final boolean isPublished;
    
    private final Date publishedFromDate;
    private final Date publishedToDate;
    private final Long blogIntAspectQNameId;
    
    public GetBlogPostsCannedQueryParams(Long blogContainerNodeId,
                                         Long nameQNameId,
                                         Long publishedQNameId,
                                         Long contentTypeQNameId,
                                         String cmCreator,
                                         boolean isPublished,
                                         Date publishedFromDate,
                                         Date publishedToDate,
                                         Long blogIntAspectQNameId,
                                         Long blogIntPostedQNameId)
    {
        super(blogContainerNodeId, nameQNameId, publishedQNameId, contentTypeQNameId, blogIntAspectQNameId, blogIntPostedQNameId);
        
        this.cmCreator = cmCreator;
        this.isPublished = isPublished;
        this.publishedFromDate = publishedFromDate;
        this.publishedToDate = publishedToDate;
        this.blogIntAspectQNameId = blogIntAspectQNameId;
    }
    
    public String getCmCreator()
    {
        return cmCreator;
    }
    
    public boolean getIsPublished()
    {
        return this.isPublished;
    }
    
    public Date getPublishedFromDate()
    {
        return publishedFromDate;
    }

    public Date getPublishedToDate()
    {
        return publishedToDate;
    }
    
    public Long getBlogIntAspectQNameId()
    {
        return blogIntAspectQNameId;
    }
}
