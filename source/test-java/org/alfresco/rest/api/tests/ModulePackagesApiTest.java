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

package org.alfresco.rest.api.tests;

import static org.alfresco.rest.api.tests.util.RestApiUtil.parsePaging;
import static org.alfresco.rest.api.tests.util.RestApiUtil.parseRestApiEntries;
import static org.alfresco.rest.api.tests.util.RestApiUtil.parseRestApiEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.alfresco.rest.api.model.ModulePackage;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Basic modulepackages api calls
 * @author Gethin James.
 */
public class ModulePackagesApiTest extends AbstractBaseApiTest
{
    protected String nonAdminUserName;

    @Before
    public void setup() throws Exception
    {
        this.nonAdminUserName = createUser("nonAdminUser" + System.currentTimeMillis());
    }

    @Test
    public void testAllModulePackages() throws Exception
    {
        HttpResponse response = getAll("modulepackages", nonAdminUserName, null, HttpStatus.SC_OK);
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
        HttpResponse response = getSingle("modulepackages", nonAdminUserName, "NonSENSE_NOTFOUND", HttpStatus.SC_NOT_FOUND);
        assertNotNull(response);

        response = getSingle("modulepackages", nonAdminUserName, "alfresco-simple-module", HttpStatus.SC_OK);
        assertNotNull(response);

        ModulePackage simpleModule = parseRestApiEntry(response.getJsonResponse(),ModulePackage.class);
        assertNotNull(simpleModule);
        assertTrue("Simple module must be the correct version","1.0.0-SNAPSHOT".equals(simpleModule.getVersion().toString()));
    }

    @Override
    public String getScope()
    {
        return "private";
    }
}
