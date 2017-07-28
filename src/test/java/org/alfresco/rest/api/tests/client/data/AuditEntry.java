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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.util.ISO8601DateFormat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * A representation of an Audit Application Entry in JUnit Test
 * 
 * @author Andrei Forascu
 *
 */
public class AuditEntry extends org.alfresco.rest.api.model.AuditEntry implements Serializable, ExpectedComparison
{

    private static final long serialVersionUID = 1L;

    public AuditEntry(Long id, String auditApplicationId, org.alfresco.rest.api.model.UserInfo createdByUser, Date createdAt, Map<String, Serializable> values)
    {
        super(id, auditApplicationId, createdByUser, createdAt, values);
    }

    @Override
    public void expected(Object o)
    {
        assertTrue("o is an instance of " + o.getClass(), o instanceof AuditEntry);

        AuditEntry other = (AuditEntry) o;

        AssertUtil.assertEquals("id", getId(), other.getId());
        AssertUtil.assertEquals("auditApplicationId", getAuditApplicationId(), other.getAuditApplicationId());
        AssertUtil.assertEquals("values", getValues(), other.getValues());
        AssertUtil.assertEquals("createdByUser", getCreatedByUser(), other.getCreatedByUser());
        AssertUtil.assertEquals("createdAt", getCreatedAt(), other.getCreatedAt());
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJSON()
    {
        JSONObject auditEntryJson = new JSONObject();
        if (getId() != null)
        {
            auditEntryJson.put("id", getId());
        }
        auditEntryJson.put("auditApplicationId", getAuditApplicationId());
        if (createdByUser != null)
        {
            auditEntryJson.put("createdByUser", new UserInfo(createdByUser.getId(), createdByUser.getDisplayName()).toJSON());
        }
        auditEntryJson.put("values", getValues());
        auditEntryJson.put("createdAt", getCreatedAt());

        return auditEntryJson;
    }

    @SuppressWarnings("unchecked")
    public static AuditEntry parseAuditEntry(JSONObject jsonObject)
    {
        Long id = (Long) jsonObject.get("id");
        String auditApplicationId = (String) jsonObject.get("auditApplicationId");
        Map<String, Serializable> values = (Map<String, Serializable>) jsonObject.get("values");
        UserInfo createdByUser = null;
        JSONObject createdByUserJson = (JSONObject) jsonObject.get("createdByUser");
        if (createdByUserJson != null)
        {
            String userId = (String) createdByUserJson.get("id");
            String displayName = (String) createdByUserJson.get("displayName");
            createdByUser = new UserInfo(userId, displayName);
        }
        Date createdAt = ISO8601DateFormat.parse((String) jsonObject.get("createdAt"));

        AuditEntry auditEntry = new AuditEntry(id, auditApplicationId, createdByUser, createdAt, values);
        return auditEntry;
    }

    public static ListResponse<AuditEntry> parseAuditEntries(JSONObject jsonObject)
    {
        List<AuditEntry> entries = new ArrayList<>();

        JSONObject jsonList = (JSONObject) jsonObject.get("list");
        assertNotNull(jsonList);

        JSONArray jsonEntries = (JSONArray) jsonList.get("entries");
        assertNotNull(jsonEntries);

        for (int i = 0; i < jsonEntries.size(); i++)
        {
            JSONObject jsonEntry = (JSONObject) jsonEntries.get(i);
            JSONObject entry = (JSONObject) jsonEntry.get("entry");
            entries.add(parseAuditEntry(entry));
        }

        ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);
        ListResponse<AuditEntry> resp = new ListResponse<AuditEntry>(paging, entries);
        return resp;
    }

}
