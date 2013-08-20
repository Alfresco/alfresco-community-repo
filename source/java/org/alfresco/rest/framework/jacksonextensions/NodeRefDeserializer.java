package org.alfresco.rest.framework.jacksonextensions;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

public class NodeRefDeserializer extends JsonDeserializer<NodeRef>
{
	private NodeRef getNodeRef(String nodeRefString)
	{
		NodeRef nodeRef = null;

        if(!NodeRef.isNodeRef(nodeRefString))
        {
        	nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeRefString);
        }
        else
        {
        	nodeRef = new NodeRef(nodeRefString);
        }

		return nodeRef;
	}

	@Override
	public NodeRef deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException
	{
        JsonToken curr = jp.getCurrentToken();

        if (curr == JsonToken.VALUE_STRING)
        {
            String nodeRefString = jp.getText();
            NodeRef nodeRef = getNodeRef(nodeRefString);
            return nodeRef;
        }
        else
        {
        	throw new IOException("Unable to deserialize nodeRef: " + curr.asString());
        }
	}
}
