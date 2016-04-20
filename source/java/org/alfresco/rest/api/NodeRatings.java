package org.alfresco.rest.api;

import org.alfresco.rest.api.impl.node.ratings.RatingScheme;
import org.alfresco.rest.api.model.NodeRating;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;

public interface NodeRatings
{
	public RatingScheme validateRatingScheme(String ratingSchemeId);
	public NodeRating getNodeRating(String nodeId, String ratingSchemeId);
	public CollectionWithPagingInfo<NodeRating> getNodeRatings(String nodeId, Paging paging);
	public void addRating(String nodeId, String ratingSchemeId, Object rating);
	public void removeRating(String nodeId, String ratingSchemeId);
}
