package org.alfresco.repo.blog.cannedqueries;

import java.util.Date;

/**
 * Parameters for {@link DraftsAndPublishedBlogPostsCannedQuery}.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
public class DraftsAndPublishedBlogPostsCannedQueryParams extends BlogEntity
{
    private final String cmCreator;
    private final Date createdFromDate;
    private final Date createdToDate;
    
    public DraftsAndPublishedBlogPostsCannedQueryParams(Long blogContainerNodeId,
                                                        Long nameQNameId,
                                                        Long publishedQNameId,
                                                        Long contentTypeQNameId,
                                                        String cmCreator,
                                                        Date createdFromDate,
                                                        Date createdToDate)
    {
        super(blogContainerNodeId, nameQNameId, publishedQNameId, contentTypeQNameId, null, null);
        
        this.cmCreator = cmCreator;
        this.createdFromDate = createdFromDate;
        this.createdToDate = createdToDate;
    }
    
    public String getCmCreator()
    {
        return cmCreator;
    }
    
    public Date getCreatedFromDate()
    {
        return createdFromDate;
    }

    public Date getCreatedToDate()
    {
        return createdToDate;
    }
}
