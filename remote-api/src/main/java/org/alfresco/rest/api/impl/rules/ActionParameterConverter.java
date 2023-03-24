/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api.impl.rules;

import static org.alfresco.rest.framework.core.exceptions.NotFoundException.DEFAULT_MESSAGE_ID;
import static org.alfresco.service.cmr.security.AccessStatus.ALLOWED;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.action.ParameterizedItemDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.logging.log4j.util.Strings;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

@Experimental
public class ActionParameterConverter
{
    static final String ACTION_PARAMETER_SHOULD_NOT_HAVE_EMPTY_OR_NULL_VALUE =
            "Action parameter should not have empty or null value";
    private final DictionaryService dictionaryService;
    private final ActionService actionService;
    private final NamespaceService namespaceService;
    private final PermissionService permissionService;
    private final Nodes nodes;

    public ActionParameterConverter(DictionaryService dictionaryService, ActionService actionService, NamespaceService namespaceService,
                                    PermissionService permissionService, Nodes nodes)
    {
        this.dictionaryService = dictionaryService;
        this.actionService = actionService;
        this.namespaceService = namespaceService;
        this.permissionService = permissionService;
        this.nodes = nodes;
    }

    public Map<String, Serializable> getConvertedParams(Map<String, Serializable> params, String name)
    {
        final Map<String, Serializable> parameters = new HashMap<>(params.size());
        final ParameterizedItemDefinition definition;
        try
        {
            definition = actionService.getActionDefinition(name);
            if (definition == null)
            {
                throw new NotFoundException(DEFAULT_MESSAGE_ID, new String[]{name});
            }
        }
        catch (NoSuchBeanDefinitionException e)
        {
            throw new NotFoundException(DEFAULT_MESSAGE_ID, new String[]{name});
        }

        for (Map.Entry<String, Serializable> param : params.entrySet())
        {
            if (Objects.toString(param.getValue(), Strings.EMPTY).isEmpty()) {
                throw new InvalidArgumentException(ACTION_PARAMETER_SHOULD_NOT_HAVE_EMPTY_OR_NULL_VALUE, new String[] {param.getKey()});
            }
            final ParameterDefinition paramDef = definition.getParameterDefintion(param.getKey());
            if (paramDef == null && !definition.getAdhocPropertiesAllowed())
            {
                throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID, new String[]{param.getKey(), name});
            }
            if (paramDef != null)
            {
                final QName typeQName = paramDef.getType();
                parameters.put(param.getKey(), convertValue(typeQName, param.getValue()));
            }
            else
            {
                parameters.put(param.getKey(), param.getValue().toString());
            }
        }
        return parameters;
    }

    public Serializable convertParamFromServiceModel(Serializable param)
    {
        if (param instanceof QName)
        {
            return ((QName) param).toPrefixString(namespaceService);
        }
        else if (param instanceof NodeRef)
        {
            return ((NodeRef) param).getId();
        }
        else
        {
            return param;
        }
    }

    private Serializable convertValue(QName typeQName, Object propertyValue) throws JSONException
    {
        Serializable value;

        final DataTypeDefinition typeDef = dictionaryService.getDataType(typeQName);
        if (typeDef == null)
        {
            throw new NotFoundException(DEFAULT_MESSAGE_ID, new String[]{typeQName.toPrefixString()});
        }

        if (propertyValue instanceof JSONArray)
        {
            final String javaClassName = typeDef.getJavaClassName();
            try
            {
                Class.forName(javaClassName);
            }
            catch (ClassNotFoundException e)
            {
                throw new DictionaryException("Java class " + javaClassName + " of property type " + typeDef.getName() + " is invalid", e);
            }

            final int length = ((JSONArray) propertyValue).length();
            final List<Serializable> list = new ArrayList<>(length);
            for (int i = 0; i < length; i++)
            {
                list.add(convertValue(typeQName, ((JSONArray) propertyValue).get(i)));
            }
            value = (Serializable) list;
        }
        else
        {
            final String stringValue = Objects.toString(propertyValue, Strings.EMPTY);
            if (typeQName.isMatch(DataTypeDefinition.QNAME) && typeQName.toString().contains(":"))
            {
                value = QName.createQName(stringValue, namespaceService);
            }
            else if (typeQName.isMatch(DataTypeDefinition.NODE_REF))
            {
                NodeRef nodeRef = nodes.validateOrLookupNode(stringValue);
                if (permissionService.hasReadPermission(nodeRef) != ALLOWED)
                {
                    throw new EntityNotFoundException(stringValue);
                }
                value = nodeRef;
            }
            else
            {
                value = (Serializable) DefaultTypeConverter.INSTANCE.convert(dictionaryService.getDataType(typeQName), propertyValue);
            }
        }
        return value;
    }
}
