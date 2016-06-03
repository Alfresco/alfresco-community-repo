package org.alfresco.rest.api.nodes;

import java.util.Collections;
import java.util.List;

import org.alfresco.rest.api.NodeRatings;
import org.alfresco.rest.api.model.NodeRating;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.WebApiParameters;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

@RelationshipResource(name = "ratings", entityResource = NodesEntityResource.class, title = "Document or folder ratings")
public class NodeRatingsRelation implements RelationshipResourceAction.Read<NodeRating>, RelationshipResourceAction.ReadById<NodeRating>, RelationshipResourceAction.Delete,
RelationshipResourceAction.Create<NodeRating>, InitializingBean
{
	private NodeRatings nodeRatings;

	public void setNodeRatings(NodeRatings nodeRatings)
	{
		this.nodeRatings = nodeRatings;
	}

	@Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("nodeRatings", this.nodeRatings);
    }

	@Override
    @WebApiDescription(title="A paged list of ratings for node 'nodeId'.")
	@WebApiParam(name="nodeId", title="The unique id of the Node being addressed", description="A single node id")
	public CollectionWithPagingInfo<NodeRating> readAll(String nodeId, Parameters parameters)
	{
		return nodeRatings.getNodeRatings(nodeId, parameters.getPaging());
	}

	/**
	 * Create a rating for the node with id 'nodeId'.
	 * 
	 */
	@Override
    @WebApiDescription(title="Rate a node for 'nodeId'.")
	@WebApiParam(name="ratingEntity", title="A single rating", description="A single node rating, multiple ratings are not supported.", 
	             kind=ResourceParameter.KIND.HTTP_BODY_OBJECT, allowMultiple=false)
	public List<NodeRating> create(String nodeId, List<NodeRating> ratingEntity, Parameters parameters)
	{
	    //There will always be 1 value because allowMultiple=false
        NodeRating rating = ratingEntity.get(0);
        String ratingSchemeId = rating.getScheme();
        nodeRatings.addRating(nodeId, ratingSchemeId, rating.getMyRating());
        return Collections.singletonList(nodeRatings.getNodeRating(nodeId, ratingSchemeId));
	}

	/**
	 * Returns the rating with id 'schemeName' for node with id 'nodeId'.
	 * 
	 */
	@Override
    @WebApiDescription(title="Get the rating with id 'ratingSchemeId' for node 'nodeId'.")
	   @WebApiParameters({
	                @WebApiParam(name="nodeId", title="The unique id of the Node being addressed", description="A single node id"),
	                @WebApiParam(name="ratingSchemeId", title="The rating scheme type", description="Possible values are likesRatingScheme.")})
	public NodeRating readById(String nodeId, String ratingSchemeId, Parameters parameters)
	{
		return nodeRatings.getNodeRating(nodeId, ratingSchemeId);
	}

	@Override
    @WebApiDescription(title="Deletes a node rating")
	public void delete(String nodeId, String ratingSchemeId, Parameters parameters)
	{
		nodeRatings.removeRating(nodeId, ratingSchemeId);		
	}
	
}