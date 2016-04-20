package org.alfresco.rest.api.nodes;

import java.util.List;

import org.alfresco.rest.api.Tags;
import org.alfresco.rest.api.model.Tag;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

@RelationshipResource(name = "tags", entityResource = NodesEntityResource.class, title = "Document or folder tags")
public class NodeTagsRelation implements RelationshipResourceAction.Create<Tag>, RelationshipResourceAction.Delete, RelationshipResourceAction.Read<Tag>, InitializingBean
{
	private Tags tags;

	public void setTags(Tags tags)
	{
		this.tags = tags;
	}

	@Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("tags", this.tags);
    }

	/**
	 * Add the tag to the node with id 'nodeId'.
	 * 
	 */
	@Override
	@WebApiDescription(title="Adds one or more tags to the node with id 'nodeId'.")
    public List<Tag> create(String nodeId, List<Tag> tagsToCreate, Parameters parameters)
	{
	    return tags.addTags(nodeId, tagsToCreate);
	}

	@Override
	@WebApiDescription(title="Remove the tag from the node with id 'nodeId'.")
	public void delete(String nodeId, String tagId, Parameters parameters)
	{
		tags.deleteTag(nodeId, tagId);
	}

	@Override
	@WebApiDescription(title="A paged list of tags on the node 'nodeId'.")
	public CollectionWithPagingInfo<Tag> readAll(String nodeId, Parameters params)
	{
		return tags.getTags(nodeId, params);
	}
	
}