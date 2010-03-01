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
        if (this.tagDetails.size() < topN)
        {
            topN = this.tagDetails.size();
        }
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
            if (tagDetails.getName().equals(tag) == true)
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
