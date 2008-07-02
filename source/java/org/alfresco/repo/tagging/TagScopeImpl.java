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
package org.alfresco.repo.tagging;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.tagging.TagDetails;
import org.alfresco.service.cmr.tagging.TagScope;

/**
 * Tag Scope.
 * 
 * Represents the roll up of tags within the scope of a node tree.
 * 
 * @author Roy Wetherall
 */
public class TagScopeImpl implements TagScope
{
    /** Node reference of node that has the tag scope aspect applied */
    private NodeRef nodeRef;
    
    /** Ordered list of tag details */
    private List<TagDetails> tagDetails;
    
    /**
     * Constructor
     * 
     * @param nodeRef   node reference
     */
    /*package*/ TagScopeImpl(NodeRef nodeRef, List<TagDetails> tagDetails)
    {
        this.nodeRef = nodeRef;
        this.tagDetails = tagDetails;
    }
    
    /**
     * Get the node reference of the tag scope
     * 
     * @return  node reference of the tag scope
     */
    public NodeRef getNodeRef()
    {
        return this.nodeRef;
    }
    
    /**
     * @see org.alfresco.service.cmr.tagging.TagScope#getTags()
     */
    public List<TagDetails> getTags()
    {      
        return this.tagDetails;
    }
    
    /**
     * @see org.alfresco.service.cmr.tagging.TagScope#getTags(int)
     */
    public List<TagDetails> getTags(int topN)
    {
        return this.tagDetails.subList(0, topN);
    }
    
    /**
     * @see org.alfresco.service.cmr.tagging.TagScope#getTag(java.lang.String)
     */
    public TagDetails getTag(String tag)
    {
        TagDetails result = null;
        for (TagDetails tagDetails : this.tagDetails)
        {
            if (tagDetails.getTagName().equals(tag) == true)
            {
                result = tagDetails;
                break;
            }
        }
        return result;
    }
    
    /**
     * @see org.alfresco.service.cmr.tagging.TagScope#isTagInScope(java.lang.String)
     */
    public boolean isTagInScope(String tag)
    {
        return (getTag(tag) != null);
    }
    
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() 
    {
        return this.nodeRef.hashCode();
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) 
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof TagScopeImpl)
        {
            TagScopeImpl that = (TagScopeImpl) obj;
            return (this.nodeRef.equals(that.nodeRef));
        }
        else
        {
            return false;
        }
    }
}
