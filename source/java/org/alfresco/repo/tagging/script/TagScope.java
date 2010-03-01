/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.tagging.script;

import java.util.List;

import org.alfresco.service.cmr.tagging.TagDetails;
import org.alfresco.service.cmr.tagging.TaggingService;

/**
 * Script object representing a tag scope.
 * 
 * @author Roy Wetherall
 */
public class TagScope
{
    /** Tagging service */
    private TaggingService taggingService;
    
    /** Repository tag scope object */
    private org.alfresco.service.cmr.tagging.TagScope tagScopeImpl;
    
    /**
     * Constructor
     * 
     * @param tagScopeImpl  repository tag scope object
     */
    public TagScope(TaggingService taggingService, org.alfresco.service.cmr.tagging.TagScope tagScopeImpl)
    {
        this.taggingService = taggingService;
        this.tagScopeImpl = tagScopeImpl;
    }
    
    /**
     * Gets all the tags, ordered by count, for the tag scope
     * 
     * @return  TagDetails[]    tags ordered by count
     */
    public TagDetails[] getTags()
    {
        List<TagDetails> tags = tagScopeImpl.getTags();
        return (TagDetails[])tags.toArray(new TagDetails[tags.size()]);
    }
    
    /**
     * Gets the top N tags ordered by count
     * 
     * @param topN              the number of top tags to return
     * @return TagDetails[]     the top N tags ordered by count
     */
    public TagDetails[] getTopTags(int topN)
    {
        List<TagDetails> tags = tagScopeImpl.getTags(topN);
        return (TagDetails[])tags.toArray(new TagDetails[tags.size()]);
    }
    
    /**
     * Get the count of a tag, 0 if not present
     * 
     * @param tag   tag name
     * @return int  tag count
     */
    public int getCount(String tag)
    {
        int result = 0;
        TagDetails tagDetails = tagScopeImpl.getTag(tag);
        if (tagDetails != null)
        {
            result = tagDetails.getCount();
        }
        return result;
    }
    
    /**
     * Refresh the tag scope
     */
    public void refresh()
    {
        // Refresh the tag scope
        this.taggingService.refreshTagScope(tagScopeImpl.getNodeRef(), false);
        
        // Update the tag scope implementation
        this.tagScopeImpl = this.taggingService.findTagScope(tagScopeImpl.getNodeRef());
    }
}
