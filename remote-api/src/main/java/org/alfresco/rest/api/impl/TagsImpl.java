/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.rest.api.impl;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.util.Pair;
import org.alfresco.util.TypeConstraint;
import org.apache.commons.collections.CollectionUtils;

/**
 * Centralises access to tag services and maps between representations.
 * 
 * @author steveglover
 * @since publicapi1.0
 */
public class TagsImpl implements Tags
{
	private static final Object PARAM_INCLUDE_COUNT = "count";
	static final String NOT_A_VALID_TAG = "An invalid parameter has been supplied";
	static final String NO_PERMISSION_TO_MANAGE_A_TAG = "Current user does not have permission to manage a tag";

    private Nodes nodes;
	private TaggingService taggingService;
	private TypeConstraint typeConstraint;
	private AuthorityService authorityService;

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

	public void setAuthorityService(AuthorityService authorityService)
	{
		this.authorityService = authorityService;
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

    @Override
	public void deleteTagById(StoreRef storeRef, String tagId) {
		verifyAdminAuthority();

		NodeRef tagNodeRef = validateTag(storeRef, tagId);
		String tagValue = taggingService.getTagName(tagNodeRef);
		taggingService.deleteTag(storeRef, tagValue);
	}

    public CollectionWithPagingInfo<Tag> getTags(StoreRef storeRef, Parameters params)
    {
        Paging paging = params.getPaging();
        PagingResults<Pair<NodeRef, String>> results = taggingService.getTags(storeRef, Util.getPagingRequest(paging));
        taggingService.getPagedTags(storeRef, 0, paging.getMaxItems());
        Integer totalItems = results.getTotalResultCount().getFirst();
        List<Pair<NodeRef, String>> page = results.getPage();
        List<Tag> tags = new ArrayList<Tag>(page.size());
        List<Pair<String, Integer>> tagsByCount = null;
        Map<String, Integer> tagsByCountMap = new HashMap<String, Integer>();

        if (params.getInclude().contains(PARAM_INCLUDE_COUNT))
        {
            tagsByCount = taggingService.findTaggedNodesAndCountByTagName(storeRef);
            if (tagsByCount != null)
            {
                for (Pair<String, Integer> tagByCountElem : tagsByCount)
                {
                    tagsByCountMap.put(tagByCountElem.getFirst(), tagByCountElem.getSecond());
                }
            }
        }
        for (Pair<NodeRef, String> pair : page)
        {
            Tag selectedTag = new Tag(pair.getFirst(), pair.getSecond());
            selectedTag.setCount(Optional.ofNullable(tagsByCountMap.get(selectedTag.getTag())).orElse(0));
            tags.add(selectedTag);
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

	@Experimental
	@Override
	public List<Tag> createTags(final StoreRef storeRef, final List<Tag> tags, final Parameters parameters)
	{
		verifyAdminAuthority();
		final List<String> tagNames = Optional.ofNullable(tags).orElse(Collections.emptyList()).stream()
			.filter(Objects::nonNull)
			.map(Tag::getTag)
			.distinct()
			.collect(Collectors.toList());

		if (CollectionUtils.isEmpty(tagNames))
		{
			throw new InvalidArgumentException(NOT_A_VALID_TAG);
		}

		return taggingService.createTags(storeRef, tagNames).stream()
			.map(pair -> Tag.builder().tag(pair.getFirst()).nodeRef(pair.getSecond()).create())
			.peek(tag -> {
				if (parameters.getInclude().contains(PARAM_INCLUDE_COUNT))
				{
					tag.setCount(0);
				}
			}).collect(Collectors.toList());
	}

	private void verifyAdminAuthority()
	{
		if (!authorityService.hasAdminAuthority())
		{
			throw new PermissionDeniedException(NO_PERMISSION_TO_MANAGE_A_TAG);
		}
	}
}
