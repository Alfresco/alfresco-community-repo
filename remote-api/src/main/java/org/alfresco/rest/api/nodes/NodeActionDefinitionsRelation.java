/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.rest.api.nodes;

import org.alfresco.rest.api.Actions;
import org.alfresco.rest.api.model.ActionDefinition;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;

import java.util.List;
import java.util.stream.Collectors;

@RelationshipResource(name = "action-definitions",  entityResource = NodesEntityResource.class, title = "Node action definitions")
public class NodeActionDefinitionsRelation extends AbstractNodeRelation
        implements RelationshipResourceAction.Read<ActionDefinition>
{
    private Actions actions;

    @Override
    public void afterPropertiesSet()
    {
        super.afterPropertiesSet();
        ParameterCheck.mandatory("actions", actions);
    }

    public void setActions(Actions actions)
    {
        this.actions = actions;
    }

    @Override
    public CollectionWithPagingInfo<ActionDefinition> readAll(String entityResourceId, Parameters params)
    {
        NodeRef parentNodeRef = nodes.validateOrLookupNode(entityResourceId, null);
        return actions.getActionDefinitions(parentNodeRef, params);
    }
}
