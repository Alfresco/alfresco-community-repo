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

package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static org.alfresco.rest.api.tests.util.RestApiUtil.parsePaging;
import static org.alfresco.rest.api.tests.util.RestApiUtil.parseRestApiEntries;
import static org.alfresco.rest.api.tests.util.RestApiUtil.parseRestApiEntry;

import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.alfresco.rest.api.model.ModulePackage;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.util.RestApiUtil;

/**
 * Basic modulepackages api calls
 * 
 * @author Gethin James.
 */
public class ModulePackagesApiTest extends AbstractBaseApiTest
{
    public static final String MODULEPACKAGES = "modulepackages";
    protected String nonAdminUserName;

    @Before
    public void setup() throws Exception
    {
        networkOne = null; // used by setRequestContext

        nonAdminUserName = createUser("nonAdminUser" + System.currentTimeMillis(), "password", null);

        // used-by teardown (deleteUser) to cleanup
        // users.add(nonAdminUserName);
    }

    @After
    public void tearDown() throws Exception
    {
        deleteUser(nonAdminUserName, networkOne);
    }

    @Test
    public void testAllModulePackages() throws Exception
    {
        setRequestContext(nonAdminUserName);

        HttpResponse response = getAll(MODULEPACKAGES, null, HttpStatus.SC_OK);
        assertNotNull(response);

        PublicApiClient.ExpectedPaging paging = parsePaging(response.getJsonResponse());
        assertNotNull(paging);

        if (paging.getCount() > 0)
        {
            List<ModulePackage> modules = parseRestApiEntries(response.getJsonResponse(), ModulePackage.class);
            assertNotNull(modules);
            assertEquals(paging.getCount().intValue(), modules.size());
        }

    }

    @Test
    public void testSingleModulePackage() throws Exception
    {
        setRequestContext(nonAdminUserName);

        HttpResponse response = getSingle(MODULEPACKAGES, "NonSENSE_NOTFOUND", HttpStatus.SC_NOT_FOUND);
        assertNotNull(response);

        response = getSingle(MODULEPACKAGES, "alfresco-simple-module", HttpStatus.SC_OK);
        assertNotNull(response);

        ModulePackage simpleModule = parseRestApiEntry(response.getJsonResponse(), ModulePackage.class);
        assertNotNull(simpleModule);
        assertTrue("Simple module must be the correct version", "1.0.0-SNAPSHOT".equals(simpleModule.getVersion().toString()));
    }

    @Test
    public void testErrorUrls() throws Exception
    {
        setRequestContext(null);

        Map<String, String> params = createParams(null, null);

        // Call an endpoint that doesn't exist
        HttpResponse response = publicApiClient.get(getScope(), MODULEPACKAGES + "/fred/blogs/king/kong/got/if/wrong", null, null, null, params);
        assertNotNull(response);
        assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusCode());
        assertEquals("no-cache", response.getHeaders().get("Cache-Control"));
        assertEquals("application/json;charset=utf-8", response.getHeaders().get("Content-Type"));

        PublicApiClient.ExpectedErrorResponse errorResponse = RestApiUtil.parseErrorResponse(response.getJsonResponse());
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getErrorKey());
        assertNotNull(errorResponse.getBriefSummary());
    }

    @Override
    public String getScope()
    {
        return "private";
    }
}
