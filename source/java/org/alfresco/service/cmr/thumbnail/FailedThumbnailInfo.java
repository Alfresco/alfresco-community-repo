/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.service.cmr.thumbnail;

import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.thumbnail.CreateThumbnailActionExecuter;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A simple pojo to hold data related to a {@link ContentModel#TYPE_FAILED_THUMBNAIL failed thumbnail attempt}.
 * A failed thumbnail attempt is when {@link CreateThumbnailActionExecuter create-thumbnail} has been used
 * to produce a thumbnail for content and that action has thrown an exception.
 * If a thumbnail was not attempted (e.g. due to unavailability of transformers) this is not a failure in this context.
 * 
 * @author Neil Mc Erlean
 * @since 3.5.0
 */
public class FailedThumbnailInfo
{
    private final String thumbnailDefinitionName;
    private final Date mostRecentFailure;
    private final int failureCount;
    private final NodeRef failedThumbnailNode;
    
    public FailedThumbnailInfo(String thumbnailDefinitionName, Date failureDate,
                           int failureCount, NodeRef failedThumbnailNode)
    {
        this.thumbnailDefinitionName = thumbnailDefinitionName;
        this.mostRecentFailure = failureDate;
        this.failureCount = failureCount;
        this.failedThumbnailNode = failedThumbnailNode;
    }
    
    /**
     * Get the {@link ThumbnailDefinition#getName() thumbnail definition name} that has failed.
     */
    public String getThumbnailDefinitionName()
    {
        return thumbnailDefinitionName;
    }

    /**
     * Get the time of the most recent failure.
     */
    public Date getMostRecentFailure()
    {
        return mostRecentFailure;
    }

    /**
     * Get the total number of failed attempts which have been made to produce a thumbnail.
     * @return
     */
    public int getFailureCount()
    {
        return failureCount;
    }

    /**
     * Get the {@link NodeRef} of the {@link ContentModel#TYPE_FAILED_THUMBNAIL failedThumbnail} node.
     * Note that this is not the NodeRef which was not thumbnailed - that will be the primary parent of
     * this node.
     */
    public NodeRef getFailedThumbnailNode()
    {
        return failedThumbnailNode;
    }
}
