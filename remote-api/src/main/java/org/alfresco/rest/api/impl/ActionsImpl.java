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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.rest.api.Actions;
import org.alfresco.rest.api.model.Action;
import org.alfresco.rest.api.model.ActionDefinition;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

public class ActionsImpl implements Actions
{
    private ActionService actionService;
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    private NamespacePrefixResolver prefixResolver;

    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPrefixResolver(NamespacePrefixResolver prefixResolver)
    {
        this.prefixResolver = prefixResolver;
    }

    @Override
    public ActionDefinition getActionDefinitionById(String actionDefinitionId)
    {
        if (actionDefinitionId == null)
        {
            throw new IllegalArgumentException("Missing actionDefinitionId");
        }

        // Non-existing actionDefinitionId -> 404
        ActionDefinition result = null;
        try
        {
            result = getActionDefinition(actionService.getActionDefinition(actionDefinitionId));
        }
        catch (NoSuchBeanDefinitionException nsbdx)
        {
            // Intentionally empty.
        }

        if (result == null)
        {
            throw new EntityNotFoundException(actionDefinitionId);
        }

        return result;
    }
    
    private ActionDefinition getActionDefinition( 
            org.alfresco.service.cmr.action.ActionDefinition actionDefinitionId)
    {        
        List<ActionDefinition.ParameterDefinition> paramDefs =
                actionDefinitionId.
                        getParameterDefinitions().
                        stream().
                        map(this::toModel).
                        collect(Collectors.toList());
        return new ActionDefinition(
                actionDefinitionId.getName(), // ID is a synonym for name.
                actionDefinitionId.getName(),
                actionDefinitionId.getTitle(),
                actionDefinitionId.getDescription(),
                toShortQNames(actionDefinitionId.getApplicableTypes()),
                actionDefinitionId.getAdhocPropertiesAllowed(),
                actionDefinitionId.getTrackStatus(),
                paramDefs);
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
        Actions.SortKey sortKey = SortKey.NAME; // default
        Boolean sortAsc = true; // default
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
        switch (sortKey)
        {
            case TITLE:
                comparator = comparing(ActionDefinition::getTitle, nullsFirst(naturalOrder()));
                break;
            case NAME:
                comparator = comparing(ActionDefinition::getName, nullsFirst(naturalOrder()));
                break;
            default:
                throw new IllegalArgumentException("Invalid sort key, must be either 'title' or 'name'.");
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

    @Override
    public Action executeAction(Action action, Parameters parameters)
    {

        if (action == null)
        {
            throw new InvalidArgumentException("action is null");
        }

        // Check action definition.

        if (action.getActionDefinitionId() == null || action.getActionDefinitionId().isEmpty())
        {
            throw new InvalidArgumentException("action.actionDefinitionId is null or empty");
        }

        org.alfresco.service.cmr.action.ActionDefinition actionDef = null;
        try
        {
            actionDef = actionService.getActionDefinition(action.getActionDefinitionId());
        }
        catch (NoSuchBeanDefinitionException nsbdx)
        {
            // Intentionally empty.
        }

        // The null check was intentionally added to catch the case when the bean is
        // found but it isn't an instance of ActionExecuter. This is the only case when
        // the result of getActionDefinition can be null and not throw the exception.
        if (actionDef == null)
        {
            throw new EntityNotFoundException(action.getActionDefinitionId());
        }

        // targetId is optional, however, currently targetId must be a valid node ID.
        NodeRef actionedUponNodeRef = null;
        if (action.getTargetId() != null && !action.getTargetId().isEmpty())
        {
            // Does it exist in the repo?
            actionedUponNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, action.getTargetId());

            if (!nodeService.exists(actionedUponNodeRef))
            {
                throw new EntityNotFoundException(action.getTargetId());
            }
        }

        org.alfresco.service.cmr.action.Action cmrAction;
        if (action.getParams() != null && !action.getParams().isEmpty())
        {
            cmrAction = actionService.createAction(action.getActionDefinitionId(), extractActionParams(actionDef, action.getParams()));
        }
        else
        {
            cmrAction = actionService.createAction(action.getActionDefinitionId());
        }

        actionService.executeAction(cmrAction, actionedUponNodeRef, true, true);

        // Create user result.
        Action result = new Action();
        result.setId(cmrAction.getId());

        return result;
    }

    private Map<String, Serializable> extractActionParams(org.alfresco.service.cmr.action.ActionDefinition actionDefinition, Map<String, String> params)
    {
        Map<String, Serializable> parameterValues = new HashMap<>();

        try
        {
            for (Map.Entry<String, String> entry : params.entrySet())
            {
                String propertyName = entry.getKey();
                Object propertyValue = entry.getValue();

                // Get the parameter definition we care about
                ParameterDefinition paramDef = actionDefinition.getParameterDefintion(propertyName);
                if (paramDef == null && !actionDefinition.getAdhocPropertiesAllowed())
                {
                    throw new AlfrescoRuntimeException("Invalid parameter " + propertyName + " for action/condition " + actionDefinition.getName());
                }
                if (paramDef != null)
                {
                    QName typeQName = paramDef.getType();

                    // Convert the property value
                    Serializable value = convertValue(typeQName, propertyValue);
                    parameterValues.put(propertyName, value);
                }
                else
                {
                    // If there is no parameter definition we can only rely on the .toString()
                    // representation of the ad-hoc property
                    parameterValues.put(propertyName, propertyValue.toString());
                }
            }
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from req.", je);
        }

        return parameterValues;
    }

    private Serializable convertValue(QName typeQName, Object propertyValue) throws JSONException
    {
        Serializable value;

        DataTypeDefinition typeDef = dictionaryService.getDataType(typeQName);
        if (typeDef == null)
        {
            throw new AlfrescoRuntimeException("Action property type definition " + typeQName.toPrefixString() + " is unknown.");
        }

        if (propertyValue instanceof JSONArray)
        {
            // Convert property type to java class
            String javaClassName = typeDef.getJavaClassName();
            try
            {
                Class.forName(javaClassName);
            }
            catch (ClassNotFoundException e)
            {
                throw new DictionaryException("Java class " + javaClassName + " of property type " + typeDef.getName() + " is invalid", e);
            }

            int length = ((JSONArray) propertyValue).length();
            List<Serializable> list = new ArrayList<>(length);
            for (int i = 0; i < length; i++)
            {
                list.add(convertValue(typeQName, ((JSONArray) propertyValue).get(i)));
            }
            value = (Serializable) list;
        }
        else
        {
            if (typeQName.equals(DataTypeDefinition.QNAME) && typeQName.toString().contains(":"))
            {
                value = QName.createQName(propertyValue.toString(), namespaceService);
            }
            else
            {
                value = (Serializable) DefaultTypeConverter.INSTANCE.convert(dictionaryService.getDataType(typeQName), propertyValue);
            }
        }

        return value;
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
