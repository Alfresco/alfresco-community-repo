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
package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Represents a group.
 *
 * @author cturlica
 *
 */
public class Group extends org.alfresco.rest.api.model.Group implements Serializable, ExpectedComparison
{

    private static final long serialVersionUID = -3580248429177260831L;

    @Override
    public void expected(Object o)
    {
        assertTrue("o is an instance of " + o.getClass(), o instanceof Group);

        Group other = (Group) o;

        AssertUtil.assertEquals("id", getId(), other.getId());
        AssertUtil.assertEquals("displayName", getDisplayName(), other.getDisplayName());
        AssertUtil.assertEquals("isRoot", getIsRoot(), other.getIsRoot());
        AssertUtil.assertEquals("parentIds", getParentIds(), other.getParentIds());
        AssertUtil.assertEquals("zones", getZones(), other.getZones());
    }

    public JSONObject toJSON()
    {
        JSONObject groupJson = new JSONObject();
        groupJson.put("id", getId());
        groupJson.put("displayName", getDisplayName());
        groupJson.put("isRoot", getIsRoot());

        if (getParentIds() != null)
        {
            groupJson.put("parentIds", new ArrayList(getParentIds()));
        }

        if (getZones() != null)
        {
            groupJson.put("zones", new ArrayList(getZones()));
        }

        return groupJson;
    }

    public static Group parseGroup(JSONObject jsonObject)
    {
        String id = (String) jsonObject.get("id");
        String displayName = (String) jsonObject.get("displayName");
        Boolean isRoot = (Boolean) jsonObject.get("isRoot");
        List<String> parentIds = (List<String>) jsonObject.get("parentIds");
        List<String> zones = (List<String>) jsonObject.get("zones");

        Group group = new Group();
        group.setId(id);
        group.setDisplayName(displayName);
        group.setIsRoot(isRoot);
        group.setParentIds(parentIds != null ? new HashSet<String>(parentIds) : null);
        group.setZones(zones != null ? new HashSet<String>(zones) : null);

        return group;
    }

    public static ListResponse<Group> parseGroups(JSONObject jsonObject)
    {
        List<Group> groups = new ArrayList<>();

        JSONObject jsonList = (JSONObject) jsonObject.get("list");
        assertNotNull(jsonList);

        JSONArray jsonEntries = (JSONArray) jsonList.get("entries");
        assertNotNull(jsonEntries);

        for (int i = 0; i < jsonEntries.size(); i++)
        {
            JSONObject jsonEntry = (JSONObject) jsonEntries.get(i);
            JSONObject entry = (JSONObject) jsonEntry.get("entry");
            groups.add(parseGroup(entry));
        }

        ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);
        ListResponse<Group> resp = new ListResponse<>(paging, groups);
        return resp;
    }

}
