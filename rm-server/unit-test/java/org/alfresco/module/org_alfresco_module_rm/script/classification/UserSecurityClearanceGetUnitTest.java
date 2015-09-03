/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.script.classification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevel;
import org.alfresco.module.org_alfresco_module_rm.classification.ClearanceLevel;
import org.alfresco.module.org_alfresco_module_rm.classification.SecurityClearance;
import org.alfresco.module.org_alfresco_module_rm.classification.SecurityClearanceService;
import org.alfresco.module.org_alfresco_module_rm.classification.UserQueryParams;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseWebScriptUnitTest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Test for get user security clearance API
 *
 * @author Tuna Aksoy
 * @since 2.4.a
 */
public class UserSecurityClearanceGetUnitTest extends BaseWebScriptUnitTest
{
    /** Classpath location of ftl template for web script */
    private static final String WEBSCRIPT_TEMPLATE = WEBSCRIPT_ROOT_RM + "classification/usersecurityclearance.get.json.ftl";

    /** User security clearance webscript instance*/
    private @Spy @InjectMocks UserSecurityClearanceGet webscript;

    /** Mocked security clearance service */
    private @Mock SecurityClearanceService mockedSecurityClearanceService;

    /** {@inheritDoc} */
    @Override
    protected DeclarativeWebScript getWebScript()
    {
        return webscript;
    }

    /** {@inheritDoc} */
    @Override
    protected String getWebScriptTemplate()
    {
        return WEBSCRIPT_TEMPLATE;
    }

    /**
     * Test to get all security clearances (no filtering)
     * @throws Exception
     */
    @Test
    public void getUserSecurityClearances() throws Exception
    {
        String userName = "aUserName0";
        String firstName = "aFirstName0";
        String lastName = "aLastName0";
        String classificationLevelId = "id0";
        String classificationLevelDisplayLabel = "displayLabel0";

        doReturn(new PagingResults<SecurityClearance>()
        {
            @Override
            public List<SecurityClearance> getPage()
            {
                return createSecurityClearances(1);
            }

            @Override
            public String getQueryExecutionId()
            {
                return anyString();
            }

            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                return new Pair<Integer, Integer>(1, 1);
            }

            @Override
            public boolean hasMoreItems()
            {
                return false;
            }
        }).when(mockedSecurityClearanceService).getUsersSecurityClearance(any(UserQueryParams.class));

        JSONObject response = executeJSONWebScript(buildParameters("nameFilter", "userName"));
        assertNotNull(response);

        JSONObject data = response.getJSONObject("data");
        assertNotNull(data);

        assertEquals(1, data.getInt("total"));
        assertEquals(0, data.getInt("startIndex"));
        assertEquals(10, data.getInt("pageSize"));
        assertEquals(1, data.getInt("itemCount"));

        JSONArray items = data.getJSONArray("items");
        assertNotNull(items);
        assertEquals(1, items.length());

        JSONObject securityClearance = items.getJSONObject(0);
        assertNotNull(securityClearance);
        assertEquals(userName, securityClearance.getString("userName"));
        assertEquals(firstName, securityClearance.getString("firstName"));
        assertEquals(lastName, securityClearance.getString("lastName"));
        assertEquals(classificationLevelId, securityClearance.getString("classificationId"));
        assertEquals(classificationLevelDisplayLabel, securityClearance.getString("clearanceLabel"));
        String fullName = firstName + " " + lastName;
        assertEquals(fullName, securityClearance.getString("fullName"));
        assertEquals(fullName + " (" + userName + ")", securityClearance.getString("completeName"));
    }

    /**
     * Test to get all security clearances (with paging)
     * @throws Exception
     */
    @Test
    public void getUserSecurityClearancesWithPaging() throws Exception
    {
        int startIndex = 0;
        int pageSize = 5;
        int numberOfUsers = 25;
        int fromIndex = startIndex * pageSize;
        int toIndex = fromIndex + pageSize > numberOfUsers ? numberOfUsers : fromIndex + pageSize;
        List<SecurityClearance> securityClearances = createSecurityClearances(numberOfUsers);
        List<SecurityClearance> items = securityClearances.subList(fromIndex, toIndex);

        doReturn(new PagingResults<SecurityClearance>()
        {
            @Override
            public List<SecurityClearance> getPage()
            {
                return items;
            }

            @Override
            public String getQueryExecutionId()
            {
                return anyString();
            }

            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                return new Pair<Integer, Integer>(numberOfUsers, numberOfUsers);
            }

            @Override
            public boolean hasMoreItems()
            {
                return true;
            }
        }).when(mockedSecurityClearanceService).getUsersSecurityClearance(any(UserQueryParams.class));

        JSONObject response = executeJSONWebScript(buildParameters("nameFilter", "userName", "pageSize", Integer.toString(pageSize), "startIndex", Integer.toString(startIndex)));
        assertNotNull(response);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode expected = mapper.readTree(getExpectedResult(numberOfUsers, startIndex, pageSize, fromIndex, toIndex - 1, items.size()));
        assertEquals(expected, mapper.readTree(response.toString()));
    }

    /**
     * Check that when supplying a single field with no sort direction, the UserQueryParams are populated with the
     * specified field and the default direction (true).
     */
    @Test
    public void testSetSortProps_singleField()
    {
        UserQueryParams userQueryParams = mock(UserQueryParams.class);
        WebScriptRequest req = mock(WebScriptRequest.class);
        when(req.getParameter("sortField")).thenReturn("field");
        when(mockedNamespaceService.getNamespaceURI("")).thenReturn("namespace");

        // Call the method under test.
        webscript.setSortProps(userQueryParams, req);

        // Check the userQueryParams contains the field (and the direction has defaulted to ascending (TRUE)).
        Pair<QName, Boolean> sortPair = new Pair<>(QName.createQName("field", mockedNamespaceService), Boolean.TRUE);
        List<Pair<QName, Boolean>> expectedSortProps = Arrays.asList(sortPair);
        verify(userQueryParams).withSortProps(expectedSortProps);
    }

    /**
     * Check that when supplying three fields with different sort directions (ascending, descending, unspecified), the
     * UserQueryParams gets populated correctly.
     */
    @Test
    public void testSetSortProps_multipleFieldsAndDirections()
    {
        UserQueryParams userQueryParams = mock(UserQueryParams.class);
        WebScriptRequest req = mock(WebScriptRequest.class);
        when(req.getParameter("sortField")).thenReturn("fieldA,fieldB,fieldC");
        // The sort order for the fields is ascending, descending, unspecified (which should default to ascending).
        when(req.getParameter("sortAscending")).thenReturn("true,false");
        when(mockedNamespaceService.getNamespaceURI("")).thenReturn("namespace");

        // Call the method under test.
        webscript.setSortProps(userQueryParams, req);

        Pair<QName, Boolean> sortPairA = new Pair<>(QName.createQName("fieldA", mockedNamespaceService), Boolean.TRUE);
        Pair<QName, Boolean> sortPairB = new Pair<>(QName.createQName("fieldB", mockedNamespaceService), Boolean.FALSE);
        Pair<QName, Boolean> sortPairC = new Pair<>(QName.createQName("fieldC", mockedNamespaceService), Boolean.TRUE);
        List<Pair<QName, Boolean>> expectedSortProps = Arrays.asList(sortPairA, sortPairB, sortPairC);
        verify(userQueryParams).withSortProps(expectedSortProps);
    }

    /** Check that if no sort information is given there are no exceptions. */
    @Test
    public void testSetSortProps_noFields()
    {
        UserQueryParams userQueryParams = mock(UserQueryParams.class);
        WebScriptRequest req = mock(WebScriptRequest.class);
        when(req.getParameter("sortField")).thenReturn(null);
        when(mockedNamespaceService.getNamespaceURI("")).thenReturn("namespace");

        // Call the method under test.
        webscript.setSortProps(userQueryParams, req);

        verifyNoMoreInteractions(userQueryParams);
    }

    private String getExpectedResult(int total, int startIndex, int pageSize, int fromIndex, int toIndex, int itemCount)
    {
        return "{" +
            "\"data\": {" +
            "\"total\": " + total + "," +
            "\"startIndex\": " + startIndex + "," +
            "\"pageSize\": " + pageSize + "," +
            "\"items\": [" +
                getItems(fromIndex, toIndex) +
            "]," +
            "\"itemCount\": " + itemCount +
            "}" +
         "}";
    }

    private String getItems(int fromIndex, int toIndex)
    {
        String items = "";
        for (; fromIndex <= toIndex; fromIndex++)
        {
            items += "{" +
                    "\"firstName\": \"aFirstName" + fromIndex + "\"," +
                    "\"lastName\": \"aLastName" + fromIndex + "\"," +
                    "\"completeName\": \"aFirstName" + fromIndex + " aLastName" + fromIndex + " (aUserName" + fromIndex + ")\"," +
                    "\"fullName\": \"aFirstName" + fromIndex + " aLastName" + fromIndex + "\"," +
                    "\"clearanceLabel\": \"displayLabel" + fromIndex + "\"," +
                    "\"userName\": \"aUserName" + fromIndex + "\"," +
                    "\"classificationId\": \"id" + fromIndex + "\"" +
                "}";
            if (fromIndex <= toIndex - 1)
            {
                items += ",";
            }
        }
        return items;
    }

    private List<SecurityClearance> createSecurityClearances(int number)
    {
        List<SecurityClearance> securityClearances = new ArrayList<>();
        for (int i = 0; i < number; i++)
        {
            PersonInfo personInfo = new PersonInfo(new NodeRef("a://noderef/" + i), "aUserName" + i, "aFirstName" + i, "aLastName" + i);
            ClassificationLevel classificationLevel = new ClassificationLevel("id" + i, "displayLabel" + i);
            ClearanceLevel clearanceLevel = new ClearanceLevel(classificationLevel, "displayLabel" + i);
            SecurityClearance securityClearance = new SecurityClearance(personInfo, clearanceLevel);
            securityClearances.add(securityClearance);
        }
        return securityClearances;
    }
}
