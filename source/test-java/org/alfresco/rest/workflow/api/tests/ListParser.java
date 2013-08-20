/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.rest.workflow.api.tests;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public abstract class ListParser<T>
{

    public ListResponse<T> parseList(JSONObject jsonResponse)
    {
        List<T> deployments = new ArrayList<T>();

        JSONObject jsonList = (JSONObject)jsonResponse.get("list");
        assertNotNull(jsonList);

        JSONArray jsonEntries = (JSONArray)jsonList.get("entries");
        assertNotNull(jsonEntries);

        for(int i = 0; i < jsonEntries.size(); i++)
        {
            JSONObject jsonEntry = (JSONObject)jsonEntries.get(i);
            JSONObject entry = (JSONObject)jsonEntry.get("entry");
            deployments.add(parseEntry(entry));
        }

        ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);

        ListResponse<T> resp = new ListResponse<T>(paging, deployments);
        return resp;
    }

    public abstract T parseEntry(JSONObject entry);
}
