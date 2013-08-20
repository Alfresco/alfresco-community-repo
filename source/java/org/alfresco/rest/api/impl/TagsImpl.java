/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.rest.api.impl;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.query.PagingResults;
import org.alfresco.repo.tagging.NonExistentTagException;
import org.alfresco.repo.tagging.TagExistsException;
import org.alfresco.repo.tagging.TaggingException;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.Tags;
import org.alfresco.rest.api.model.Tag;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.util.Pair;
import org.alfresco.util.TypeConstraint;

/**
 * Centralises access to tag services and maps between representations.
 * 
 * @author steveglover
 * @since publicapi1.0
 */
public class TagsImpl implements Tags
{
	private Nodes nodes;
	private TaggingService taggingService;
	private TypeConstraint typeConstraint;
	
	public void setTypeConstraint(TypeConstraint typeConstraint)
	{
		this.typeConstraint = typeConstraint;
	}

	public void setNodes(Nodes nodes) 
    {
		this.nodes = nodes;
	}
	
    public void setTaggingService(TaggingService taggingService)
    {
		this.taggingService = taggingService;
	}

	public List<Tag> addTags(String nodeId, final List<Tag> tags)
	{
	        NodeRef nodeRef = nodes.validateNode(nodeId);
			if(!typeConstraint.matches(nodeRef))
			{
				throw new UnsupportedResourceOperationException("Cannot tag this node");
			}

	        List<String> tagValues = new AbstractList<String>()
            {
                @Override
                public String get(int arg0)
                {
                	String tag = tags.get(arg0).getTag();
                    return tag;
                }
    
                @Override
                public int size()
                {
                    return tags.size();
                }
            };
            try
            {
		        List<Pair<String, NodeRef>> tagNodeRefs = taggingService.addTags(nodeRef, tagValues);
		        List<Tag> ret = new ArrayList<Tag>(tags.size());
		        for(Pair<String, NodeRef> pair : tagNodeRefs)
		        {
		        	ret.add(new Tag(pair.getSecond(), pair.getFirst()));
		        }
		        return ret;
            }
            catch(IllegalArgumentException e)
            {
            	throw new InvalidArgumentException(e.getMessage());
            }
	}
	
    public void deleteTag(String nodeId, String tagId)
    {
		NodeRef nodeRef = nodes.validateNode(nodeId);
		getTag(tagId);
    	NodeRef existingTagNodeRef = validateTag(tagId);
    	String tagValue = taggingService.getTagName(existingTagNodeRef);
    	taggingService.removeTag(nodeRef, tagValue);
    }

    public CollectionWithPagingInfo<Tag> getTags(StoreRef storeRef, Paging paging)
    {
    	PagingResults<Pair<NodeRef, String>> results = taggingService.getTags(storeRef, Util.getPagingRequest(paging));
    	Integer totalItems = results.getTotalResultCount().getFirst();
    	List<Pair<NodeRef, String>> page = results.getPage();
    	List<Tag> tags = new ArrayList<Tag>(page.size());
    	for(Pair<NodeRef, String> pair : page)
    	{
    		tags.add(new Tag(pair.getFirst(), pair.getSecond()));
    	}

    	return CollectionWithPagingInfo.asPaged(paging, tags, results.hasMoreItems(), (totalItems == null ? null : totalItems.intValue()));
    }
    
    public NodeRef validateTag(String tagId)
    {
    	NodeRef tagNodeRef = nodes.validateNode(tagId);
    	if(tagNodeRef == null)
    	{
    		throw new EntityNotFoundException(tagId);
    	}
    	return tagNodeRef;
    }
    
    public NodeRef validateTag(StoreRef storeRef, String tagId)
    {
    	NodeRef tagNodeRef = nodes.validateNode(storeRef, tagId);
    	if(tagNodeRef == null)
    	{
    		throw new EntityNotFoundException(tagId);
    	}
    	return tagNodeRef;
    }

    public Tag changeTag(StoreRef storeRef, String tagId, Tag tag)
    {
    	try
    	{
	    	NodeRef existingTagNodeRef = validateTag(storeRef, tagId);
	    	String existingTagName = taggingService.getTagName(existingTagNodeRef);
	    	String newTagName = tag.getTag();
	    	NodeRef newTagNodeRef = taggingService.changeTag(storeRef, existingTagName, newTagName);
	    	return new Tag(newTagNodeRef, newTagName);
    	}
    	catch(NonExistentTagException e)
    	{
    		throw new NotFoundException(e.getMessage());
    	}
    	catch(TagExistsException e)
    	{
    		throw new ConstraintViolatedException(e.getMessage());
    	}
    	catch(TaggingException e)
    	{
    		throw new InvalidArgumentException(e.getMessage());
    	}
    }

    public Tag getTag(String tagId)
    {
    	return getTag(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, tagId);
    }

    public Tag getTag(StoreRef storeRef, String tagId)
    {
    	NodeRef tagNodeRef = validateTag(storeRef, tagId);
    	String tagValue = taggingService.getTagName(tagNodeRef);
    	return new Tag(tagNodeRef, tagValue);
    }

    public CollectionWithPagingInfo<Tag> getTags(String nodeId, Parameters params)
    {
		NodeRef nodeRef = validateTag(nodeId);

		PagingResults<Pair<NodeRef, String>> results = taggingService.getTags(nodeRef, Util.getPagingRequest(params.getPaging()));
    	Integer totalItems = results.getTotalResultCount().getFirst();
    	List<Pair<NodeRef, String>> page = results.getPage();
    	List<Tag> tags = new ArrayList<Tag>(page.size());
    	for(Pair<NodeRef, String> pair : page)
    	{
    		tags.add(new Tag(pair.getFirst(), pair.getSecond()));
    	}

    	return CollectionWithPagingInfo.asPaged(params.getPaging(), tags, results.hasMoreItems(), (totalItems == null ? null : totalItems.intValue()));
    }
}
