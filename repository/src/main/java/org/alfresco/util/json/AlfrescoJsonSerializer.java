/*
 * #%L
 * Alfresco Repository
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

package org.alfresco.util.json;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 */
public class AlfrescoJsonSerializer
{
    private final NamespaceService namespaceService;
    private final Map<Class<?>, JsonSerializer<?, ?>> serializers = new HashMap<Class<?>, JsonSerializer<?, ?>>();

    public AlfrescoJsonSerializer(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void register(Class<?> clazz, JsonSerializer<?, ?> serializer)
    {
        serializers.put(clazz, serializer);
    }

    public Object getJsonValue(Object value) throws JSONException
    {
        if (value instanceof Date)
        {
            return getJsonDate((Date) value);
        }
        if (value instanceof QName)
        {
            return getJsonQName((QName) value);
        }
        if (value instanceof NodeRef)
        {
            return value.toString();
        }
        else if (value instanceof Collection<?>)
        {
            return getJsonArray((Collection<?>) value);
        }
        else if (value instanceof Map<?, ?>)
        {
            return getJsonObject((Map<?, ?>) value);
        }
        return value;
    }

    private JSONObject getJsonObject(Map<?, ?> map) throws JSONException
    {
        JSONObject object = new JSONObject();
        for (Entry<?, ?> entry : map.entrySet())
        {
            String key = getJsonKey(entry.getKey());
            Object value = getJsonValue(entry.getValue());
            object.put(key, value);
        }
        return object;
    }

    private String getJsonKey(Object key)
    {
        if (key instanceof QName)
        {
            return getJsonQName((QName) key);
        }
        return key.toString();
    }

    private String getJsonQName(QName name)
    {
        String nameString = name.toPrefixString(namespaceService);
        return nameString.replaceFirst(":", "_");
    }

    private JSONObject getJsonDate(Date date) throws JSONException
    {
        JSONObject isoDate = new JSONObject();
        isoDate.put("iso8601", ISO8601DateFormat.format(date));
        return isoDate;
    }

    private JSONArray getJsonArray(Collection<?> values) throws JSONException
    {
        JSONArray array = new JSONArray();
        for (Object val : values)
        {
            array.put(getJsonValue(val));
        }
        return array;
    }

}
