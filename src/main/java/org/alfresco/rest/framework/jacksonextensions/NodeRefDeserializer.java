/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
