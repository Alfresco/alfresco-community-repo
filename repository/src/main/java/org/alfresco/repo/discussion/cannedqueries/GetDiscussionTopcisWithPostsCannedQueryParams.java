/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
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
    private Date topicCreatedAfter;
    private Date postCreatedAfter;

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
        this.postCreatedAfter = postCreatedAfter;
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
