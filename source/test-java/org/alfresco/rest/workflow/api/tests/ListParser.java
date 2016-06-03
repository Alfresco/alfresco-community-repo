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
        List<T> entries = new ArrayList<T>();

        JSONObject jsonList = (JSONObject)jsonResponse.get("list");
        assertNotNull(jsonList);

        JSONArray jsonEntries = (JSONArray)jsonList.get("entries");
        assertNotNull(jsonEntries);

        for(int i = 0; i < jsonEntries.size(); i++)
        {
            JSONObject jsonEntry = (JSONObject)jsonEntries.get(i);
            JSONObject entry = (JSONObject)jsonEntry.get("entry");
            entries.add(parseEntry(entry));
        }

        ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);

        ListResponse<T> resp = new ListResponse<T>(paging, entries);
        return resp;
    }

    public abstract T parseEntry(JSONObject entry);
}
