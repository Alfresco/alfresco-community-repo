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

import org.junit.Before;
import org.junit.Test;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiClient.People;
import org.alfresco.rest.api.tests.client.RequestContext;

/**
 * Case sensitivity public api tests.
 * 
 * @author steveglover
 *
 */
public class TestPublicApi128 extends EnterpriseTestApi
{
    private long time;

    private static final String userId = "bOb.Jones";
    private static final String networkPrefix = "MiXeDCasE";
    private static final String networkDomain = "coM";

    private String networkId;
    private TestNetwork mixedCaseNetwork;
    @SuppressWarnings("unused")
    private TestPerson person1;

    private String getNetworkId(String networkIdPrefix, String networkIdDomain)
    {
        return networkIdPrefix + time + "." + networkIdDomain;
    }

    private String getUserName(String username, String networkIdPrefix, String networkIdDomain)
    {
        return username + "@" + getNetworkId(networkIdPrefix, networkIdDomain);
    }

    @Before
    public void setup()
    {
        // note: we don't call super.setup() since we create our own test data here !

        // mixed case network
        this.time = System.currentTimeMillis();
        this.networkId = getNetworkId(networkPrefix, networkDomain);

        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
            @SuppressWarnings("synthetic-access")
            public Void execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

                TestPublicApi128.this.mixedCaseNetwork = repoService.createNetwork(TestPublicApi128.this.networkId, true);
                TestPublicApi128.this.mixedCaseNetwork.create();

                // mixed case user
                PersonInfo personInfo = new PersonInfo("Bob", "Jones", TestPublicApi128.userId, "password", null, null, null, null, null, null, null);
                TestPublicApi128.this.person1 = mixedCaseNetwork.createUser(personInfo);

                return null;
            }
        }, false, true);
    }

    @Test
    public void testPublicApi128() throws Exception
    {
        People peopleProxy = publicApiClient.people();

        // try to get person activities given a mixture of cases for networkId and user id
        {
            publicApiClient.setRequestContext(new RequestContext(networkId, getUserName("BOB.JONES", networkPrefix, networkDomain)));

            int skipCount = 0;
            int maxItems = 10;
            Paging paging = getPaging(skipCount, maxItems);
            peopleProxy.getActivities("-me-", createParams(paging, null));

            publicApiClient.setRequestContext(new RequestContext(networkId, getUserName("BOB.JONES", "MIXEDCASE", "COM")));

            skipCount = 0;
            maxItems = 10;
            paging = getPaging(skipCount, maxItems);
            peopleProxy.getActivities("-me-", createParams(paging, null));

            publicApiClient.setRequestContext(new RequestContext(networkId.toUpperCase(), getUserName("BOB.JONES", "MIXEDCASE", "COM")));

            skipCount = 0;
            maxItems = 10;
            paging = getPaging(skipCount, maxItems);
            peopleProxy.getActivities("-me-", createParams(paging, null));

            publicApiClient.setRequestContext(new RequestContext(networkId.toUpperCase(), getUserName("Bob.JONES", "MIXEDCASE", "COM")));

            skipCount = 0;
            maxItems = 10;
            paging = getPaging(skipCount, maxItems);
            peopleProxy.getActivities("-me-", createParams(paging, null));

            publicApiClient.setRequestContext(new RequestContext(networkId, getUserName("BOB.JONES", networkPrefix, networkDomain)));

            skipCount = 0;
            maxItems = 10;
            paging = getPaging(skipCount, maxItems);
            peopleProxy.getActivities(getUserName("BOB.JONES", networkPrefix, networkDomain), createParams(paging, null));

            publicApiClient.setRequestContext(new RequestContext(networkId, getUserName("BOB.JONES", "MIXEDCASE", "CoM")));

            skipCount = 0;
            maxItems = 10;
            paging = getPaging(skipCount, maxItems);
            peopleProxy.getActivities(getUserName("BOB.JONES", "MIXEDCASE", networkDomain), createParams(paging, null));

            publicApiClient.setRequestContext(new RequestContext(networkId.toUpperCase(), getUserName("BOB.JONES", "MIXEDCASE", "coM")));

            skipCount = 0;
            maxItems = 10;
            paging = getPaging(skipCount, maxItems);
            peopleProxy.getActivities(getUserName("BOB.JONES", "MIXEDCASE", "CoM"), createParams(paging, null));

            publicApiClient.setRequestContext(new RequestContext(networkId.toUpperCase(), getUserName("Bob.JONES", "MIXEDCASE", "COM")));

            skipCount = 0;
            maxItems = 10;
            paging = getPaging(skipCount, maxItems);
            peopleProxy.getActivities(getUserName("Bob.JONES", "MIXEDCASE", networkDomain), createParams(paging, null));

            publicApiClient.setRequestContext(new RequestContext(networkId, getUserName("BOB.JONES", networkPrefix, networkDomain)));

            skipCount = 0;
            maxItems = 10;
            paging = getPaging(skipCount, maxItems);
            peopleProxy.getActivities(getUserName("Bob.JONES", networkPrefix, networkDomain), createParams(paging, null));

            publicApiClient.setRequestContext(new RequestContext(networkId, getUserName("BOB.JONES", "MIXEDCASE", "COm")));

            skipCount = 0;
            maxItems = 10;
            paging = getPaging(skipCount, maxItems);
            peopleProxy.getActivities(getUserName("BOB.JONES", "MixEDCASE", networkDomain), createParams(paging, null));

            publicApiClient.setRequestContext(new RequestContext(networkId.toUpperCase(), getUserName("BOB.JONES", "MIXEDCASE", "COM")));

            skipCount = 0;
            maxItems = 10;
            paging = getPaging(skipCount, maxItems);
            peopleProxy.getActivities(getUserName("BOB.joNES", "MIXEDcasE", networkDomain), createParams(paging, null));

            publicApiClient.setRequestContext(new RequestContext(networkId.toUpperCase(), getUserName("Bob.JONES", "MIXEDCASE", "coM")));

            skipCount = 0;
            maxItems = 10;
            paging = getPaging(skipCount, maxItems);
            peopleProxy.getActivities(getUserName("BOB.joNES", networkPrefix, "cOM"), createParams(paging, null));
        }
    }
}
