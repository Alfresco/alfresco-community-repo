package org.alfresco.repo.discussion.cannedqueries;

import java.util.Date;

/**
 * Parameter objects for {@link GetDiscussionTopcisWithPostsCannedQuery}.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class GetDiscussionTopcisWithPostsCannedQueryParams extends NodeWithChildrenEntity
{
    private boolean excludePrimaryPost; 
    private Date    topicCreatedAfter;
    private Date    postCreatedAfter;
    
    public GetDiscussionTopcisWithPostsCannedQueryParams(Long parentNodeId,
                                         Long nameQNameId,
                                         Long contentTypeQNameId,
                                         Long childrenTypeId,
                                         Date topicCreatedAfter,
                                         Date postCreatedAfter,
                                         boolean excludePrimaryPost)
                                         
    {
        super(parentNodeId, nameQNameId, contentTypeQNameId, childrenTypeId);
        this.excludePrimaryPost = excludePrimaryPost;
        this.topicCreatedAfter = topicCreatedAfter;
        this.postCreatedAfter  = postCreatedAfter;
    }

    public Date getTopicCreatedAfter() 
    {
       return topicCreatedAfter;
    }

    public Date getPostCreatedAfter() 
    {
       return postCreatedAfter;
    }
    
    public boolean getExcludePrimaryPost()
    {
       return excludePrimaryPost;
    }
}
