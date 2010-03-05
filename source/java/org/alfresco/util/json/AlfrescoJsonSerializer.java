/*
 * Copyright (C) 2005-20010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.util.json;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.tck.atompub.utils.ISO8601DateFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        if (value instanceof Date) { return getJsonDate((Date) value); }
        if (value instanceof QName) { return getJsonQName((QName) value); }
        if (value instanceof NodeRef)
        {
            return value.toString();
        }
        else if (value instanceof Collection<?>)
        {
            return getJsonArray((Collection<?>) value);
        }
        else if (value instanceof Map<?, ?>) { return getJsonObject((Map<?, ?>) value); }
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
        if (key instanceof QName) { return getJsonQName((QName) key); }
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