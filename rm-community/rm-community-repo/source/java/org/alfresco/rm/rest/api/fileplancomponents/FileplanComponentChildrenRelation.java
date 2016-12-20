/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rm.rest.api.fileplancomponents;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.exceptions.ApiException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.MultiPartRelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.rm.rest.api.RMNodes;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * An implementation of an Entity Resource for a fileplan component
 *
 * @author Ana Bozianu
 * @since 2.6
 */
@RelationshipResource(name="children", entityResource = FileplanComponentsEntityResource.class, title = "Children of fileplan component")
public class FileplanComponentChildrenRelation implements RelationshipResourceAction.Read<Node>,
                                                 RelationshipResourceAction.Create<Node>,
                                                 MultiPartRelationshipResourceAction.Create<Node>
{
    private RMNodes nodes;

    public void setNodes(RMNodes nodes)
    {
        this.nodes = nodes;
    }

    @Override
    @WebApiDescription(title = "Return a paged list of fileplan components for the container identified by parentFolderNodeId")
    public CollectionWithPagingInfo<Node> readAll(String parentFolderNodeId, Parameters parameters)
    {
        return nodes.listChildren(parentFolderNodeId, parameters);
    }

    @Override
    @WebApiDescription(title="Create one (or more) nodes as children of container identified by parentFolderNodeId")
    public List<Node> create(String parentFolderNodeId, List<Node> nodeInfos, Parameters parameters)
    {
        List<Node> result = new ArrayList<>(nodeInfos.size());

        for (Node nodeInfo : nodeInfos)
        {
            result.add(nodes.createNode(parentFolderNodeId, nodeInfo, parameters));
        }

        return result;
    }

    @Override
    @WebApiDescription(title = "Upload file content and meta-data into the repository.")
    @WebApiParam(name = "formData", title = "A single form data", description = "A single form data which holds FormFields.")
    public Node create(String parentFolderNodeId, FormData formData, Parameters parameters, WithResponse withResponse)
    {
        try
        {
            return nodes.upload(parentFolderNodeId, formData, parameters);
        }
        catch (ApiException apiException)
        {
            /*
             * The upload method encapsulates most exceptions that can occur on node creation in an ApiException. 
             * To allow the API framework to correctly map the exception to the API error code we throw the original exception.
             */
            Throwable originalException = apiException.getCause();
            if (originalException != null && originalException instanceof RuntimeException)
            {
                throw (RuntimeException) originalException;
            }
            else
            {
                throw apiException;
            }
        }
    }
}
