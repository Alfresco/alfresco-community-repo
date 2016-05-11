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

import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;

import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.rest.api.tests.RepoService.SiteInformation;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiHttpClient.BinaryPayload;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.service.cmr.site.SiteVisibility;

import java.util.Map;

/**
 * Generic methods for calling the Api, taken from BaseCustomModelApiTest
 */
public abstract class AbstractBaseApiTest extends EnterpriseTestApi
{

    /**
     * The api scope. either public or private
     * @return public or private
     */
    public abstract String getScope();

    protected HttpResponse post(String url, String runAsUser, String body, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));

        HttpResponse response = publicApiClient.post(getScope(), url, null, null, null, body);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse post(String url, String runAsUser, String body,  String queryString, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));
        if (queryString != null)
        {
            url += queryString;
        }
        HttpResponse response = publicApiClient.post(getScope(), url, null, null, null, body);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse post(String url, String runAsUser, String body, String queryString, String contentType, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));
        if (queryString != null)
        {
            url += queryString;
        }
        HttpResponse response = publicApiClient.post(getScope(), url, null, null, null, body, contentType);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse post(String url, String runAsUser, byte[] body, String queryString, String contentType, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));
        if (queryString != null)
        {
            url += queryString;
        }
        HttpResponse response = publicApiClient.post(getScope(), url, null, null, null, body, contentType);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse post(String runAsUser, String entityCollectionName, String entityId, String relationCollectionName, byte[] body, String queryString, String contentType, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));
        HttpResponse response = publicApiClient.post(getScope(), entityCollectionName, entityId, relationCollectionName, null, body, contentType);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse getAll(String url, String runAsUser, PublicApiClient.Paging paging, int expectedStatus) throws Exception
    {
        return getAll(url, runAsUser, paging, null, expectedStatus);
    }

    protected HttpResponse getAll(String url, String runAsUser, PublicApiClient.Paging paging, Map<String, String> otherParams, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));
        Map<String, String> params = (paging == null) ? null : createParams(paging, otherParams);

        HttpResponse response = publicApiClient.get(getScope(), url, null, null, null, params);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse getAll(Class<?> entityResource, String runAsUser, PublicApiClient.Paging paging, Map<String, String> otherParams, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));

        HttpResponse response = publicApiClient.get(entityResource, null, null, otherParams);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse getSingle(String url, String runAsUser, String entityId, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));

        HttpResponse response = publicApiClient.get(getScope(), url, entityId, null, null, null);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse getSingle(Class<?> entityResource, String runAsUser, String entityId, Map<String, String> params, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));

        HttpResponse response = publicApiClient.get(entityResource, entityId, null, params);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse put(String url, String runAsUser, String entityId, String body, String queryString, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));
        if (queryString != null)
        {
            entityId += queryString;
        }
        HttpResponse response = publicApiClient.put(getScope(), url, entityId, null, null, body, null);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse putBinary(String url, int version, String runAsUser, BinaryPayload payload, String queryString, Map<String, String> params,
                int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));
        if (queryString != null)
        {
            url += queryString;
        }

        HttpResponse response = publicApiClient.putBinary(getScope(), version, url, null, null, null, payload, params);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse putBinary(String url, String runAsUser, BinaryPayload payload, String queryString, Map<String, String> params,
                int expectedStatus) throws Exception
    {
        return putBinary(url, 1, runAsUser, payload, queryString, params, expectedStatus);
    }

    protected HttpResponse delete(String url, String runAsUser, String entityId, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));

        HttpResponse response = publicApiClient.delete(getScope(), url, entityId, null, null);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected String createUser(String username)
    {
        PersonInfo personInfo = new PersonInfo(username, username, username, "password", null, null, null, null, null, null, null);
        RepoService.TestPerson person = repoService.createUser(personInfo, username, null);
        return person.getId();
    }

    protected TestSite createSite(final TestNetwork testNetwork, TestPerson user, final SiteVisibility siteVisibility)
    {
        final String siteName = "RandomSite" + System.currentTimeMillis();
        final TestSite site = TenantUtil.runAsUserTenant(new TenantUtil.TenantRunAsWork<TestSite>()
        {
            @Override
            public TestSite doWork() throws Exception
            {
                SiteInformation siteInfo = new SiteInformation(siteName, siteName, siteName, siteVisibility);
                return repoService.createSite(testNetwork, siteInfo);
            }
        }, user.getId(), testNetwork.getId());
        assertNotNull(site);

        return site;
    }

    protected void checkStatus(int expectedStatus, int actualStatus)
    {
        if (expectedStatus > 0 && expectedStatus != actualStatus)
        {
            fail("Status code " + actualStatus + " returned, but expected " + expectedStatus);
        }
    }
}
