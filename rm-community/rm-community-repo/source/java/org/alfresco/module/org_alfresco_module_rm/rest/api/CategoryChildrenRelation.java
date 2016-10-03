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

package org.alfresco.module.org_alfresco_module_rm.rest.api;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;

@RelationshipResource(name="category-children", entityResource = RMNodesEntityResource.class, title = "Children of Category type")
public class CategoryChildrenRelation implements 
    RelationshipResourceAction.Create<Node>
{
    private Nodes nodes;

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    @Override
    public List<Node> create(String parentFolderNodeId, List<Node> nodeInfos, Parameters parameters)
    {
        List<Node> result = new ArrayList<>(nodeInfos.size());

        for (Node nodeInfo : nodeInfos)
        {
            nodeInfo.setNodeType(RecordsManagementModel.RM_PREFIX + ":" + RecordsManagementModel.TYPE_RECORD_CATEGORY.getPrefixString());
            result.add(nodes.createNode(parentFolderNodeId, nodeInfo, parameters));
        }

        return result;
    }

}
