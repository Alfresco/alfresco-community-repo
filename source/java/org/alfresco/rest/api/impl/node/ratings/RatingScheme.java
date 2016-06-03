package org.alfresco.rest.api.impl.node.ratings;

import org.alfresco.rest.api.model.NodeRating;
import org.alfresco.service.cmr.repository.NodeRef;

public interface RatingScheme
{
	public void applyRating(NodeRef nodeRef, Object rating);
	public void removeRating(NodeRef nodeRef);
	public NodeRating getNodeRating(NodeRef nodeRef);
}
