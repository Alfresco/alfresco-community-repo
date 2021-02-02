/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.rest.api.tests.client.data;

import org.alfresco.rest.api.model.PropertyDefinition;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class Type extends org.alfresco.rest.api.model.Type implements Serializable, ExpectedComparison
{

    @Override
    public void expected(Object model)
    {
        assertTrue("model is an instance of " + model.getClass(), model instanceof Type);

        Type other = (Type) model;

        AssertUtil.assertEquals("id", getId(), other.getId());
        AssertUtil.assertEquals("title", getTitle(), other.getTitle());
        AssertUtil.assertEquals("description", getDescription(), other.getDescription());
        AssertUtil.assertEquals("parenId", getParentId(), other.getParentId());
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJSON()
    {
        JSONObject jsonObject = new JSONObject();
        if (getId() != null)
        {
            jsonObject.put("id", getId());
        }

        jsonObject.put("title", getTitle());

        if (getParentId() != null)
        {
            jsonObject.put("parentId", getParentId());
        }

        if (getDescription() != null)
        {
            jsonObject.put("description", getDescription());
        }

        if (getProperties() != null)
        {
            jsonObject.put("properties", getProperties());
        }

        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static Type parseType(JSONObject jsonObject)
    {
        String id = (String) jsonObject.get("id");
        String title = (String) jsonObject.get("title");
        String description = (String) jsonObject.get("description");
        String parentId = (String) jsonObject.get("parentId");
        List<PropertyDefinition> properties = (List<PropertyDefinition>) jsonObject.get("properties");

        Type action = new Type();
        action.setId(id);
        action.setTitle(title);
        action.setDescription(description);
        action.setParentId(parentId);
        action.setProperties(properties);

        return action;
    }

    @SuppressWarnings("unchecked")
    public static PublicApiClient.ListResponse<Type> parseTypes(JSONObject jsonObject)
    {
        List<Type> aspects = new ArrayList<Type>();

        JSONObject jsonList = (JSONObject)jsonObject.get("list");
        assertNotNull(jsonList);

        JSONArray jsonEntries = (JSONArray)jsonList.get("entries");
        assertNotNull(jsonEntries);

        for(int i = 0; i < jsonEntries.size(); i++)
        {
            JSONObject jsonEntry = (JSONObject)jsonEntries.get(i);
            JSONObject entry = (JSONObject)jsonEntry.get("entry");
            aspects.add(parseType(entry));
        }

        PublicApiClient.ExpectedPaging paging = PublicApiClient.ExpectedPaging.parsePagination(jsonList);
        PublicApiClient.ListResponse<Type> response = new PublicApiClient.ListResponse<Type>(paging, aspects);
        return response;
    }

}
