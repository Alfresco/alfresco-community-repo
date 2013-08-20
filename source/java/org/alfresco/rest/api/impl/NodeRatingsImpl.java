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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.rest.api.NodeRatings;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.impl.node.ratings.RatingScheme;
import org.alfresco.rest.api.model.NodeRating;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.cmr.rating.RatingService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.TypeConstraint;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Centralises access to node ratings services and maps between representations.
 * 
 * @author steveglover
 * @since publicapi1.0
 */
public class NodeRatingsImpl implements NodeRatings
{
    private static Log logger = LogFactory.getLog(NodeRatingsImpl.class);  

	private Nodes nodes;
	private RatingService ratingService;
	private NamedObjectRegistry<RatingScheme> nodeRatingSchemeRegistry;
    private TypeConstraint typeConstraint;

	public void setTypeConstraint(TypeConstraint typeConstraint)
	{
		this.typeConstraint = typeConstraint;
	}

	public void setRatingService(RatingService ratingService)
    {
		this.ratingService = ratingService;
	}

	public void setNodes(Nodes nodes) 
    {
		this.nodes = nodes;
	}

	public void setNodeRatingSchemeRegistry(NamedObjectRegistry<RatingScheme> nodeRatingSchemeRegistry)
    {
		this.nodeRatingSchemeRegistry = nodeRatingSchemeRegistry;
	}

	public RatingScheme validateRatingScheme(String ratingSchemeId)
	{
		RatingScheme ratingScheme = nodeRatingSchemeRegistry.getNamedObject(ratingSchemeId);
		if(ratingScheme == null)
		{
			throw new InvalidArgumentException("Invalid ratingSchemeId " + ratingSchemeId);
		}

		return ratingScheme;
	}
	
	// TODO deal with fractional ratings - InvalidArgumentException
	public NodeRating getNodeRating(String nodeId, String ratingSchemeId)
	{
		NodeRef nodeRef = nodes.validateNode(nodeId);
		RatingScheme ratingScheme = validateRatingScheme(ratingSchemeId);
		return ratingScheme.getNodeRating(nodeRef);
	}

	public CollectionWithPagingInfo<NodeRating> getNodeRatings(String nodeId, Paging paging)
	{
		NodeRef nodeRef = nodes.validateNode(nodeId);
		Set<String> schemes = new TreeSet<String>(ratingService.getRatingSchemes().keySet());
		Iterator<String> it = schemes.iterator();

		int skipCount = paging.getSkipCount();
		int maxItems = paging.getMaxItems();
		int end = skipCount + maxItems;
		if(end < 0)
		{
			// overflow
			end = Integer.MAX_VALUE;
		}
		int count = Math.min(maxItems, schemes.size() - skipCount);
		List<NodeRating> ratings = new ArrayList<NodeRating>(count);

		for(int i = 0; i < end && it.hasNext(); i++)
		{
			String schemeName = it.next();
			if(i < skipCount)
			{
				continue;
			}

        	RatingScheme ratingScheme = validateRatingScheme(schemeName);
    		NodeRating nodeRating = ratingScheme.getNodeRating(nodeRef);
            ratings.add(nodeRating);
		}

		int totalSize = schemes.size();
		boolean hasMoreItems = (skipCount + count < totalSize);

        return CollectionWithPagingInfo.asPaged(paging, ratings, hasMoreItems, totalSize);
	}

	public void addRating(String nodeId, String ratingSchemeId, Object rating)
	{
		NodeRef nodeRef = nodes.validateNode(nodeId);
		
		RatingScheme ratingScheme = validateRatingScheme(ratingSchemeId);

		if(!typeConstraint.matches(nodeRef))
		{
			throw new UnsupportedResourceOperationException("Cannot rate this node");
		}

		ratingScheme.applyRating(nodeRef, rating);
	}

	public void removeRating(String nodeId, String ratingSchemeId)
	{
		RatingScheme ratingScheme = validateRatingScheme(ratingSchemeId);
		NodeRef nodeRef = nodes.validateNode(nodeId);
		ratingScheme.removeRating(nodeRef);
	}
}
