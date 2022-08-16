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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.action.ParameterizedItemDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;

@Experimental
public class ActionParameterConverter
{
    private final DictionaryService dictionaryService;
    private final ActionService actionService;
    private final NamespaceService namespaceService;

    public ActionParameterConverter(DictionaryService dictionaryService, ActionService actionService,
                                    NamespaceService namespaceService)
    {
        this.dictionaryService = dictionaryService;
        this.actionService = actionService;
        this.namespaceService = namespaceService;
    }

    Map<String, Serializable> getConvertedParams(Map<String, Serializable> params, String name) {
        final Map<String, Serializable> parameters = new HashMap<>(params.size());
        final ParameterizedItemDefinition definition = actionService.getActionDefinition(name);
        if (definition == null)
        {
            throw new NotFoundException(NotFoundException.DEFAULT_MESSAGE_ID, new String[]{name});
        }

        for (Map.Entry<String, Serializable> param : params.entrySet())
        {
            final ParameterDefinition paramDef = definition.getParameterDefintion(param.getKey());
            if (paramDef == null && !definition.getAdhocPropertiesAllowed())
            {
                throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID, new String[]{param.getKey(), name});
            }
            if (paramDef != null)
            {
                final QName typeQName = paramDef.getType();
                parameters.put(param.getKey(), convertValue(typeQName, param.getValue()));
            } else
            {
                parameters.put(param.getKey(), param.getValue().toString());
            }
        }
        return parameters;
    }

    private Serializable convertValue(QName typeQName, Object propertyValue) throws JSONException
    {
        Serializable value;

        final DataTypeDefinition typeDef = dictionaryService.getDataType(typeQName);
        if (typeDef == null)
        {
            throw new NotFoundException(NotFoundException.DEFAULT_MESSAGE_ID, new String[]{typeQName.toPrefixString()});
        }

        if (propertyValue instanceof JSONArray)
        {
            final String javaClassName = typeDef.getJavaClassName();
            try
            {
                Class.forName(javaClassName);
            } catch (ClassNotFoundException e)
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
        } else
        {
            if (typeQName.equals(DataTypeDefinition.QNAME) && typeQName.toString().contains(":"))
            {
                value = QName.createQName(propertyValue.toString(), namespaceService);
            } else
            {
                value = (Serializable) DefaultTypeConverter.INSTANCE.convert(dictionaryService.getDataType(typeQName), propertyValue);
            }
        }
        return value;
    }
}
