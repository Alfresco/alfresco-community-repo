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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.Collator;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.alfresco.rest.api.tests.PublicApiDateFormat;
import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;

public class SiteMembershipRequest implements ExpectedComparison, Comparable<SiteMembershipRequest>
{
    private Collator collator = Collator.getInstance();

    private String id; // site id
    private String message;
    private Date createdAt;
    private Date modifiedAt;
    private String title;
    private Site site;
    private Person person;

    public SiteMembershipRequest()
    {}

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Site getSite()
    {
        return site;
    }

    public void setSite(Site site)
    {
        this.site = site;
    }

    public Date getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt)
    {
        this.createdAt = createdAt;
    }

    public Date getModifiedAt()
    {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt)
    {
        this.modifiedAt = modifiedAt;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public Person getPerson()
    {
        return person;
    }

    public void setPerson(Person person)
    {
        this.person = person;
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJSON()
    {
        JSONObject siteMembershipRequestJson = new JSONObject();
        siteMembershipRequestJson.put("id", getId());
        siteMembershipRequestJson.put("message", getMessage());
        return siteMembershipRequestJson;
    }

    public static SiteMembershipRequest parseSiteMembershipRequest(JSONObject jsonObject) throws ParseException
    {
        String id = (String) jsonObject.get("id");
        String createdAt = (String) jsonObject.get("createdAt");
        String message = (String) jsonObject.get("message");
        String modifiedAt = (String) jsonObject.get("modifiedAt");
        JSONObject siteJSON = (JSONObject) jsonObject.get("site");
        JSONObject personJSON = (JSONObject) jsonObject.get("person");

        SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
        siteMembershipRequest.setId(id);
        siteMembershipRequest.setCreatedAt(PublicApiDateFormat.getDateFormat().parse(createdAt));
        siteMembershipRequest.setMessage(message);
        if (modifiedAt != null)
        {
            siteMembershipRequest.setModifiedAt(PublicApiDateFormat.getDateFormat().parse(modifiedAt));
        }
        if (siteJSON != null)
        {
            Site site = SiteImpl.parseSite(siteJSON);
            siteMembershipRequest.setSite(site);
        }
        if (personJSON != null)
        {
            Person person = Person.parsePerson(personJSON);
            siteMembershipRequest.setPerson(person);
        }

        return siteMembershipRequest;
    }

    public static ListResponse<SiteMembershipRequest> parseSiteMembershipRequests(JSONObject jsonObject) throws ParseException
    {
        List<SiteMembershipRequest> siteMembershipRequests = new ArrayList<SiteMembershipRequest>();

        JSONObject jsonList = (JSONObject) jsonObject.get("list");
        assertNotNull(jsonList);

        JSONArray jsonEntries = (JSONArray) jsonList.get("entries");
        assertNotNull(jsonEntries);

        for (int i = 0; i < jsonEntries.size(); i++)
        {
            JSONObject jsonEntry = (JSONObject) jsonEntries.get(i);
            JSONObject entry = (JSONObject) jsonEntry.get("entry");
            siteMembershipRequests.add(SiteMembershipRequest.parseSiteMembershipRequest(entry));
        }

        ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);
        return new ListResponse<>(paging, siteMembershipRequests);
    }

    @Override
    public void expected(Object o)
    {
        assertTrue(o instanceof SiteMembershipRequest);

        SiteMembershipRequest other = (SiteMembershipRequest) o;

        assertNotNull(other.getCreatedAt());

        if (other.getModifiedAt() != null)
        {
            assertFalse(other.getModifiedAt().before(other.getCreatedAt()));
        }

        if (modifiedAt != null)
        {
            // check that the modifiedAt is higher in the RHS
            assertFalse(other.getModifiedAt().before(modifiedAt));
        }

        if (createdAt != null)
        {
            AssertUtil.assertEquals("createdAt", createdAt, other.getCreatedAt());
        }

        AssertUtil.assertEquals("createdAt", createdAt, other.getCreatedAt());
        // ignore case when comparing site (membership request) id
        AssertUtil.assertEquals("id", id.toLowerCase(), other.getId().toLowerCase());
        AssertUtil.assertEquals("message", message, other.getMessage());
    }

    @Override
    public String toString()
    {
        return "SiteMembershipRequest [id=" + id + ", message=" + message
                + ", createdAt=" + createdAt + ", modifiedAt=" + modifiedAt
                + ", title=" + title + "]";
    }

    @Override
    public int compareTo(SiteMembershipRequest o)
    {
        int ret = collator.compare(site.getTitle(), o.getSite().getTitle());
        return ret;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SiteMembershipRequest other = (SiteMembershipRequest) obj;
        if (id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!id.equals(other.id))
            return false;
        return true;
    }
}
