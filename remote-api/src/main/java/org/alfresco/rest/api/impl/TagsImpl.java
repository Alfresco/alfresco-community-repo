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

import static java.util.stream.Collectors.toList;

import static org.alfresco.rest.antlr.WhereClauseParser.EQUALS;
import static org.alfresco.rest.antlr.WhereClauseParser.IN;
import static org.alfresco.rest.antlr.WhereClauseParser.MATCHES;
import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
import static org.alfresco.service.cmr.tagging.TaggingService.TAG_ROOT_NODE_REF;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.query.ListBackedPagingResults;
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
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.framework.resource.parameters.where.QueryImpl;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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
	public static final String PARAM_INCLUDE_COUNT = "count";
	private static final String PARAM_WHERE_TAG = "tag";
	static final String NOT_A_VALID_TAG = "An invalid parameter has been supplied";
	static final String NO_PERMISSION_TO_MANAGE_A_TAG = "Current user does not have permission to manage a tag";

    private Nodes nodes;
	private NodeService nodeService;
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
	public void setNodeService(NodeService nodeService)
	{
		this.nodeService = nodeService;
	}
	
    public void setTaggingService(TaggingService taggingService)
    {
		this.taggingService = taggingService;
	}

	public void setAuthorityService(AuthorityService authorityService)
	{
		this.authorityService = authorityService;
	}

    public List<Tag> addTags(String nodeId, final List<Tag> tags, final Parameters parameters)
    {
        NodeRef nodeRef = nodes.validateOrLookupNode(nodeId);
        if (!typeConstraint.matches(nodeRef))
        {
            throw new UnsupportedResourceOperationException("Cannot tag this node");
        }

        List<String> tagValues = tags.stream().map(Tag::getTag).collect(toList());
        try
        {
            List<Pair<String, NodeRef>> tagNodeRefs = taggingService.addTags(nodeRef, tagValues);
            List<Tag> ret = new ArrayList<>(tags.size());
			List<Pair<String, Integer>> tagsCountPairList = taggingService.findTaggedNodesAndCountByTagName(nodeRef.getStoreRef());
			Map<String, Long> tagsCountMap = tagsCountPairList.stream().collect(Collectors.toMap(Pair::getFirst, pair -> Long.valueOf(pair.getSecond())));
            for (Pair<String, NodeRef> pair : tagNodeRefs)
            {
				Tag createdTag = new Tag(pair.getSecond(), pair.getFirst());
				if (parameters.getInclude().contains(PARAM_INCLUDE_COUNT))
				{
					createdTag.setCount(Optional.ofNullable(tagsCountMap.get(createdTag.getTag())).orElse(0L) + 1);
				}
                ret.add(createdTag);
            }
            return ret;
        }
        catch (IllegalArgumentException e)
        {
            throw new InvalidArgumentException(e.getMessage());
        }
    }
	
    public void deleteTag(String nodeId, String tagId)
    {
		NodeRef nodeRef = nodes.validateNode(nodeId);
		getTag(STORE_REF_WORKSPACE_SPACESSTORE, tagId, null);
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

	@Override
    public CollectionWithPagingInfo<Tag> getTags(StoreRef storeRef, Parameters params)
    {
	    Paging paging = params.getPaging();
		Pair<String, Boolean> sorting = !params.getSorting().isEmpty() ? new Pair<>(params.getSorting().get(0).column, params.getSorting().get(0).asc) : null;
		Map<Integer, Collection<String>> namesFilters = resolveTagNamesQuery(params.getQuery());

		List<Pair<NodeRef, Integer>> results = taggingService.getTags(storeRef, params.getInclude(), sorting, namesFilters.get(EQUALS), namesFilters.get(MATCHES));
		final Map<NodeRef, Long> tagsByCountMap = new HashMap<>();

		List<Tag> tagsList = results.stream().map(pair -> new Tag(pair.getFirst(), (String)nodeService.getProperty(pair.getFirst(), ContentModel.PROP_NAME))).collect(Collectors.toList());


		if (params.getInclude().contains(PARAM_INCLUDE_COUNT))
		{
			for (Pair<NodeRef, Integer> pair : results)
			{
				tagsByCountMap.put(pair.getFirst(), Long.valueOf(pair.getSecond()));
			}

			tagsList.forEach(tag -> tag.setCount(Optional.ofNullable(tagsByCountMap.get(tag.getTag())).orElse(0L) +1));
		}

		ListBackedPagingResults listBackedPagingResults = new ListBackedPagingResults(tagsList, Util.getPagingRequest(params.getPaging()));

		return CollectionWithPagingInfo.asPaged(paging, listBackedPagingResults.getPage(), listBackedPagingResults.hasMoreItems(), (Integer) listBackedPagingResults.getTotalResultCount().getFirst());
    }

    public NodeRef validateTag(String tagId)
    {
    	NodeRef tagNodeRef = nodes.validateNode(tagId);
		return checkTagRootAsNodePrimaryParent(tagId, tagNodeRef);
    }
    
    public NodeRef validateTag(StoreRef storeRef, String tagId)
    {
    	NodeRef tagNodeRef = nodes.validateNode(storeRef, tagId);
		return checkTagRootAsNodePrimaryParent(tagId, tagNodeRef);
    }

	/**
	 * Find the number of times the given tag is used (if requested).
	 *
	 * @param storeRef The store the tag is in.
	 * @param tagName The name of the tag.
	 * @param parameters The request parameters object containing the includes parameter.
	 * @return The number of times the tag is applied, or null if "count" wasn't in the include parameter.
	 */
	private Long findCountIfRequested(StoreRef storeRef, String tagName, Parameters parameters)
	{
		Long count = null;
		if (parameters != null && parameters.getInclude() != null && parameters.getInclude().contains(PARAM_INCLUDE_COUNT))
		{
			count = taggingService.findCountByTagName(storeRef, tagName);
		}
		return count;
	}

    @Override
    public Tag changeTag(StoreRef storeRef, String tagId, Tag tag, Parameters parameters)
    {
    	try
    	{
	    	NodeRef existingTagNodeRef = validateTag(storeRef, tagId);
	    	String existingTagName = taggingService.getTagName(existingTagNodeRef);
			Long count = findCountIfRequested(storeRef, existingTagName, parameters);
			String newTagName = tag.getTag();
			NodeRef newTagNodeRef = taggingService.changeTag(storeRef, existingTagName, newTagName);
			return Tag.builder().nodeRef(newTagNodeRef).tag(newTagName).count(count).create();
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

	@Override
    public Tag getTag(StoreRef storeRef, String tagId, Parameters parameters)
    {
    	NodeRef tagNodeRef = validateTag(storeRef, tagId);
    	String tagName = taggingService.getTagName(tagNodeRef);
    	Long count = findCountIfRequested(storeRef, tagName, parameters);
    	return Tag.builder().nodeRef(tagNodeRef).tag(tagName).count(count).create();
    }

    @Override
    public CollectionWithPagingInfo<Tag> getTags(String nodeId, Parameters params)
    {
        NodeRef nodeRef = nodes.validateOrLookupNode(nodeId);
        PagingResults<Pair<NodeRef, String>> results = taggingService.getTags(nodeRef, Util.getPagingRequest(params.getPaging()));
        Integer totalItems = results.getTotalResultCount().getFirst();
        List<Pair<NodeRef, String>> page = results.getPage();
        List<Tag> tags = new ArrayList<>(page.size());
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
			.collect(toList());

		if (CollectionUtils.isEmpty(tagNames))
		{
			throw new InvalidArgumentException(NOT_A_VALID_TAG);
		}

		return taggingService.createTags(storeRef, tagNames).stream()
			.map(pair -> Tag.builder().tag(pair.getFirst()).nodeRef(pair.getSecond()).create())
			.peek(tag -> {
				if (parameters.getInclude().contains(PARAM_INCLUDE_COUNT))
				{
					tag.setCount(0L);
				}
			}).collect(toList());
	}

	private void verifyAdminAuthority()
	{
		if (!authorityService.hasAdminAuthority())
		{
			throw new PermissionDeniedException(NO_PERMISSION_TO_MANAGE_A_TAG);
		}
	}

	/**
	 * Method resolves where query looking for clauses: EQUALS, IN or MATCHES.
	 * Expected values for EQUALS and IN will be merged under EQUALS clause.
	 * @param namesQuery Where query with expected tag name(s).
	 * @return Map of expected exact and alike tag names.
	 */
	private Map<Integer, Collection<String>> resolveTagNamesQuery(final Query namesQuery)
	{
		if (namesQuery == null || namesQuery == QueryImpl.EMPTY)
		{
			return Collections.emptyMap();
		}

		final Map<Integer, Collection<String>> properties = QueryHelper
			.resolve(namesQuery)
			.usingOrOperator()
			.withoutNegations()
			.getProperty(PARAM_WHERE_TAG)
			.getExpectedValuesForAnyOf(EQUALS, IN, MATCHES)
			.skipNegated();

		return properties.entrySet().stream()
			.collect(Collectors.groupingBy((entry) -> {
				if (entry.getKey() == EQUALS || entry.getKey() == IN)
				{
					return EQUALS;
				}
				else
				{
					return MATCHES;
				}
			}, Collectors.flatMapping((entry) -> entry.getValue().stream().map(String::toLowerCase), Collectors.toCollection(HashSet::new))));
	}

	private NodeRef checkTagRootAsNodePrimaryParent(String tagId, NodeRef tagNodeRef)
	{
		if ( tagNodeRef == null || !nodeService.getPrimaryParent(tagNodeRef).getParentRef().equals(TAG_ROOT_NODE_REF))
		{
			throw new EntityNotFoundException(tagId);
		}
		return tagNodeRef;
	}

	private List<Tag> sortTags(List<Tag> tagsList, String sortBy, boolean sortAsc)
	{
		if(sortBy.equals(PARAM_WHERE_TAG))
		{
			tagsList.sort(Comparator.comparing(Tag::getTag));
		}

		//first check that count exists and then check if we should sort by count
		else if(tagsList.get(0).getCount()!=null && sortBy.equals(PARAM_INCLUDE_COUNT))
		{
			tagsList.sort(Comparator.comparingLong(Tag::getCount));
		}

		if(!sortAsc)
		{
			Collections.reverse(tagsList);
		}

		return tagsList;
	}
}
