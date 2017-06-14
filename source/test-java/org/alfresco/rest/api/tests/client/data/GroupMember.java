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
import java.util.List;

import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Represents a group member.
 *
 * @author cturlica
 *
 */
public class GroupMember extends org.alfresco.rest.api.model.GroupMember implements Serializable, ExpectedComparison
{

    @Override
    public void expected(Object o)
    {
        assertTrue("o is an instance of " + o.getClass(), o instanceof GroupMember);

        GroupMember other = (GroupMember) o;

        AssertUtil.assertEquals("id", getId(), other.getId());
        AssertUtil.assertEquals("displayName", getDisplayName(), other.getDisplayName());
        AssertUtil.assertEquals("memberType", getMemberType(), other.getMemberType());
    }

    public JSONObject toJSON()
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", getId());
        jsonObject.put("displayName", getDisplayName());
        jsonObject.put("memberType", getMemberType());

        return jsonObject;
    }

    public static GroupMember parseGroupMember(JSONObject jsonObject)
    {
        String id = (String) jsonObject.get("id");
        String displayName = (String) jsonObject.get("displayName");
        String memberType = (String) jsonObject.get("memberType");

        GroupMember group = new GroupMember();
        group.setId(id);
        group.setDisplayName(displayName);
        group.setMemberType(memberType);

        return group;
    }

    public static ListResponse<GroupMember> parseGroupMembers(JSONObject jsonObject)
    {
        List<GroupMember> groupMembers = new ArrayList<>();

        JSONObject jsonList = (JSONObject) jsonObject.get("list");
        assertNotNull(jsonList);

        JSONArray jsonEntries = (JSONArray) jsonList.get("entries");
        assertNotNull(jsonEntries);

        for (int i = 0; i < jsonEntries.size(); i++)
        {
            JSONObject jsonEntry = (JSONObject) jsonEntries.get(i);
            JSONObject entry = (JSONObject) jsonEntry.get("entry");
            groupMembers.add(parseGroupMember(entry));
        }

        ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);
        ListResponse<GroupMember> resp = new ListResponse<>(paging, groupMembers);
        return resp;
    }

}
