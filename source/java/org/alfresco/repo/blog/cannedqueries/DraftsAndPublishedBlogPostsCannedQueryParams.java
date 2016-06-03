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
