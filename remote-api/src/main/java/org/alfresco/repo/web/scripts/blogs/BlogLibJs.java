/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.repo.web.scripts.blogs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import org.alfresco.model.BlogIntegrationModel;
import org.alfresco.service.namespace.QName;

/**
 * This class is a port of a previous JavaScript library.
 * 
 * @author Neil Mc Erlean (based on previous JavaScript)
 * @since 4.0
 * @deprecated This class should not be extended or reused as it may be refactored.
 */
public class BlogLibJs
{
    /**
     * Fetches the blog properties from the json object and adds them to an array using the correct property names as indexes.
     */
    public static Map<QName, Serializable> getBlogPropertiesArray(JSONObject json)
    {
        Map<QName, Serializable> arr = new HashMap<QName, Serializable>();

        putJSONEntryInMap(json, arr, "blogType", BlogIntegrationModel.PROP_BLOG_IMPLEMENTATION);
        putJSONEntryInMap(json, arr, "blogId", BlogIntegrationModel.PROP_ID);
        putJSONEntryInMap(json, arr, "blogName", BlogIntegrationModel.PROP_NAME);
        putJSONEntryInMap(json, arr, "blogDescription", BlogIntegrationModel.PROP_DESCRIPTION);
        putJSONEntryInMap(json, arr, "blogUrl", BlogIntegrationModel.PROP_URL);
        putJSONEntryInMap(json, arr, "username", BlogIntegrationModel.PROP_USER_NAME);
        putJSONEntryInMap(json, arr, "password", BlogIntegrationModel.PROP_PASSWORD);
        return arr;
    }

    private static void putJSONEntryInMap(JSONObject json,
            Map<QName, Serializable> arr, String jsonKey, QName mapKey)
    {
        if (json.containsKey(jsonKey))
        {
            arr.put(mapKey, (Serializable) json.get(jsonKey));
        }
    }
}
