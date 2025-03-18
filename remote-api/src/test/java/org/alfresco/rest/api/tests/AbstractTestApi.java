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

import java.text.Collator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.springframework.context.ApplicationContext;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.client.AuthenticatedHttp;
import org.alfresco.rest.api.tests.client.AuthenticationDetailsProvider;
import org.alfresco.rest.api.tests.client.HttpClientProvider;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiHttpClient;
import org.alfresco.rest.api.tests.client.UserAuthenticationDetailsProviderImpl;
import org.alfresco.rest.api.tests.client.UserData;
import org.alfresco.rest.api.tests.client.UserDataService;
import org.alfresco.rest.api.tests.client.data.ExpectedComparison;

public abstract class AbstractTestApi
{
    private static final Log logger = LogFactory.getLog(AbstractTestApi.class);

    protected RepoService repoService;
    protected ApplicationContext applicationContext;

    protected PublicApiHttpClient httpClient;
    protected PublicApiClient publicApiClient;
    protected RetryingTransactionHelper transactionHelper;
    protected Collator collator = Collator.getInstance();

    public RepoService getRepoService()
    {
        return repoService;
    }

    @Before
    public void setupTests() throws Exception
    {
        TestFixture testFixture = getTestFixture(false);
        this.applicationContext = testFixture.getApplicationContext();
        this.repoService = testFixture.getRepoService();
        this.transactionHelper = (RetryingTransactionHelper) applicationContext.getBean("retryingTransactionHelper");

        HttpClientProvider httpClientProvider = (HttpClientProvider) applicationContext.getBean("httpClientProvider");

        UserDataService userDataService = new UserDataService() {
            @Override
            public UserData findUserByUserName(String userName)
            {
                UserData userData = new UserData();
                TestPerson person = getRepoService().getPerson(userName.toLowerCase());
                userData.setUserName(person.getId());
                userData.setPassword(person.getPassword());
                userData.setId(person.getId());
                return userData;
            }
        };
        AuthenticationDetailsProvider authenticationDetailsProvider = new UserAuthenticationDetailsProviderImpl(userDataService, "admin", "admin");
        AuthenticatedHttp authenticatedHttp = new AuthenticatedHttp(httpClientProvider, authenticationDetailsProvider);
        this.httpClient = new PublicApiHttpClient(TestFixture.HOST, TestFixture.PORT, TestFixture.CONTEXT_PATH,
                TestFixture.PUBLIC_API_SERVLET_NAME, authenticatedHttp);
        this.publicApiClient = new PublicApiClient(httpClient, userDataService);
    }

    protected void log(String msg)
    {
        log(msg, null);
    }

    protected void log(String msg, Throwable t)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug(msg, t);
        }
    }

    protected Paging getPaging(Integer skipCount, Integer maxItems)
    {
        return new Paging(skipCount, maxItems, null);
    }

    protected Paging getPaging(Integer skipCount, Integer maxItems, int total, Integer expectedTotal)
    {
        ExpectedPaging expectedPaging = ExpectedPaging.getExpectedPaging(skipCount, maxItems, total, expectedTotal);
        return new Paging(skipCount, maxItems, expectedPaging);
    }

    protected <T extends ExpectedComparison> void check(ExpectedComparison expected, T actual)
    {
        assertNotNull(expected);
        assertNotNull(actual);

        expected.expected(actual);
    }

    protected String stripCMISSuffix(String nodeId)
    {
        String ret = null;

        int idx = nodeId.indexOf(";");
        if (idx != -1)
        {
            ret = nodeId.substring(0, idx);
        }
        else
        {
            ret = nodeId;
        }

        return ret;
    }

    protected Map<String, String> createParams(Paging paging, Map<String, String> otherParams)
    {
        Map<String, String> params = new HashMap<String, String>(2);
        if (paging != null)
        {
            if (paging.getSkipCount() != null)
            {
                params.put("skipCount", String.valueOf(paging.getSkipCount()));
            }
            if (paging.getMaxItems() != null)
            {
                params.put("maxItems", String.valueOf(paging.getMaxItems()));
            }
        }
        if (otherParams != null)
        {
            params.putAll(otherParams);
        }
        return params;
    }

    protected <T1 extends ExpectedComparison, T extends Object> void checkList(List<T1> expected, ExpectedPaging expectedPaging, ListResponse<T> actual)
    {
        assertNotNull(expectedPaging);
        assertNotNull(actual);

        log("Expected paging " + expectedPaging.toString());

        for (T1 expectedEntry : expected)
        {
            log("Expected entry " + expectedEntry.toString());
        }

        log("Actual paging " + actual.getPaging().toString());

        for (Object actualEntry : actual.getList())
        {
            log("Actual entry " + actualEntry.toString());
        }

        assertEquals(expectedPaging, actual.getPaging());
        assertEquals(expected.size(), actual.getList().size());

        for (int i = 0; i < expected.size(); i++)
        {
            ExpectedComparison expectedEntry = (ExpectedComparison) expected.get(i);
            T actualEntry = actual.getList().get(i);
            assertNotNull(actualEntry);
            assertNotNull(expectedEntry);
            expectedEntry.expected(actualEntry);
        }
    }

    protected <T extends Object> List<T> sublist(List<T> list, int skipCount, int maxItems)
    {
        List<T> ret = null;

        int size = list.size();
        int start = skipCount;
        int end = Math.min(skipCount + maxItems, size);

        if (start > size)
        {
            ret = Collections.emptyList();
        }
        else
        {
            ret = list.subList(start, end);
        }

        return ret;
    }

    protected abstract TestFixture getTestFixture() throws Exception;

    /**
     * @param createTestData
     *            The created instance can optionally create test data if required
     */
    protected abstract TestFixture getTestFixture(boolean createTestData) throws Exception;
}
