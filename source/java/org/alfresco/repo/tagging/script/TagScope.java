/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.tagging.script;

import java.util.List;

import org.alfresco.service.cmr.tagging.TagDetails;

/**
 * Script object representing a tag scope.
 * 
 * @author Roy Wetherall
 */
public class TagScope
{
    /** Repository tag scope object */
    private org.alfresco.service.cmr.tagging.TagScope tagScopeImpl;
    
    /**
     * Constructor
     * 
     * @param tagScopeImpl  repository tag scope object
     */
    public TagScope(org.alfresco.service.cmr.tagging.TagScope tagScopeImpl)
    {
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
}
