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
package org.alfresco.rest.api.impl;

import org.alfresco.rest.api.Actions;
import org.alfresco.rest.api.model.ActionDefinition;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ActionsImpl implements Actions
{
    private ActionService actionService;
    private NamespacePrefixResolver prefixResolver;

    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    public void setPrefixResolver(NamespacePrefixResolver prefixResolver)
    {
        this.prefixResolver = prefixResolver;
    }

    @Override
    public CollectionWithPagingInfo<ActionDefinition> getActionDefinitions(NodeRef nodeRef, Parameters params)
    {
        return actionDefinitions(actionService.getActionDefinitions(nodeRef), params);
    }

    @Override
    public CollectionWithPagingInfo<ActionDefinition> getActionDefinitions(Parameters params)
    {
        return actionDefinitions(actionService.getActionDefinitions(), params);
    }

    private CollectionWithPagingInfo<ActionDefinition> actionDefinitions(
            List<org.alfresco.service.cmr.action.ActionDefinition> actionDefinitions,
            Parameters params)
    {
        List<SortColumn> sorting = params.getSorting();
        Actions.SortKey sortKey = null;
        Boolean sortAsc = null;
        if (sorting != null && !sorting.isEmpty())
        {
            if (sorting.size() > 1)
            {
                throw new IllegalArgumentException("Only a single sort field ('name' or 'title') is supported.");
            }
            sortKey = Actions.SortKey.valueOf(sorting.get(0).column.toUpperCase());
            sortAsc = sorting.get(0).asc;
        }
        
        Comparator<? super ActionDefinition> comparator;
        if (sortKey == null)
        {
            sortKey = SortKey.NAME; // default
        }
        
        switch (sortKey)
        {
            case TITLE:
                comparator = Comparator.comparing(ActionDefinition::getTitle);
                break;
            case NAME:
                comparator = Comparator.comparing(ActionDefinition::getName);
                break;
            default:
                throw new IllegalArgumentException("Invalid sort key, must be either 'title' or 'name'.");
        }
        
        if (sortAsc == null)
        {
            sortAsc = true;
        }
        if (!sortAsc)
        {
            comparator = comparator.reversed();
        }


        final int maxItems = params.getPaging().getMaxItems();
        final int skip = params.getPaging().getSkipCount();

        List<ActionDefinition> sortedPage = actionDefinitions.
                stream().
                map(actionDefinition -> {
                    List<ActionDefinition.ParameterDefinition> paramDefs =
                            actionDefinition.
                                    getParameterDefinitions().
                                    stream().
                                    map(this::toModel).
                                    collect(Collectors.toList());
                    return new ActionDefinition(
                            actionDefinition.getName(), // ID is a synonym for name.
                            actionDefinition.getName(),
                            actionDefinition.getTitle(),
                            actionDefinition.getDescription(),
                            toShortQNames(actionDefinition.getApplicableTypes()),
                            actionDefinition.getAdhocPropertiesAllowed(),
                            actionDefinition.getTrackStatus(),
                            paramDefs);
                }).
                sorted(comparator).
                skip(skip).
                limit(maxItems).
                collect(Collectors.toList());
        
        boolean hasMoreItems = actionDefinitions.size() > (skip + maxItems);

        return CollectionWithPagingInfo.asPaged(
                params.getPaging(),
                sortedPage,
                hasMoreItems,
                actionDefinitions.size());
    }

    private List<String> toShortQNames(Set<QName> types)
    {
        return types.
                stream().
                map(this::toShortQName).
                collect(Collectors.toList());
    }

    private String toShortQName(QName type)
    {
        return type.toPrefixString(prefixResolver);
    }

    private ActionDefinition.ParameterDefinition toModel(ParameterDefinition p)
    {
        return new ActionDefinition.ParameterDefinition(
                p.getName(),
                toShortQName(p.getType()),
                p.isMultiValued(),
                p.isMandatory(),
                p.getDisplayLabel(),
                p.getParameterConstraintName()
        );
    }
}
