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

package org.alfresco.rm.rest.api.nodes;

import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;

/**
 * Fileplan component children
 *
 * @author Ana Bozianu
 * @since 2.6
 */
@EntityResource(name="fileplan-components", title = "Fileplan Components")
public class FileplanComponentsEntityResource implements
        EntityResourceAction.ReadById<Node>,
        EntityResourceAction.Delete,
        EntityResourceAction.Update<Node>
{
    private Nodes nodes;

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    @WebApiDescription(title = "Get Node Information", description = "Get information for the fileplan component with id 'nodeId'")
    @WebApiParam(name = "nodeId", title = "The node id")
    public Node readById(String nodeId, Parameters parameters)
    {
        return nodes.getFolderOrDocument(nodeId, parameters);
    }

    @Override
    public Node update(String nodeId, Node nodeInfo, Parameters parameters)
    {
        return nodes.updateNode(nodeId, nodeInfo, parameters);
    }

    @Override
    public void delete(String nodeId, Parameters parameters)
    {
        nodes.deleteNode(nodeId, parameters);
    }
}
