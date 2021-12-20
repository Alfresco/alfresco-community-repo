/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.admin.RepoHealthChecker;
import org.alfresco.rest.api.discovery.DiscoveryApiWebscript;
import org.alfresco.rest.api.probes.ProbeEntityResource;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.alfresco.rest.api.probes.ProbeEntityResource.*;
import static org.alfresco.rest.api.probes.ProbeEntityResource.ProbeType.LIVE;
import static org.alfresco.rest.api.probes.ProbeEntityResource.ProbeType.READY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.lenient;

/**
 * V1 REST API tests for Probes (Live and Ready)
 *
 * <ul>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/probe/<probeId>} </li>
 * </ul>
 */
@RunWith(MockitoJUnitRunner.class)
public class ProbeApiTest extends AbstractBaseApiTest
{
    private static final boolean OK = true;

    private ProbeEntityResource probe;
    private DiscoveryApiWebscript origDiscovery;

    @Mock
    private DiscoveryApiWebscript goodDiscovery;

    @Mock
    private DiscoveryApiWebscript badDiscovery;

    @Mock
    private RepoHealthChecker repoHealthChecker;

    @Before
    @Override
    public void setup() throws Exception
    {
//      super.setup(); -- Takes a very long time and we need no test networks, sites or users.
        setRequestContext(null, null, null);

        String beanName = ProbeEntityResource.class.getCanonicalName()+".get";
        probe = applicationContext.getBean(beanName, ProbeEntityResource.class);
        lenient().when(badDiscovery.getRepositoryInfo()).thenThrow(AlfrescoRuntimeException.class);
        Mockito.doNothing().when(repoHealthChecker).checkDatabase();
        probe.setRepoHealthChecker(repoHealthChecker);
        origDiscovery = probe.setDiscovery(badDiscovery);
    }

    @After
    @Override
    public void tearDown() throws Exception
    {
        probe.setDiscovery(origDiscovery);
        super.tearDown();
    }

    @Override
    public String getScope()
    {
        return "public";
    }

    private void assertResponse(ProbeType probeType, Boolean ready, String expected, int expectedStatus) throws Exception
    {
        String[] keys = expectedStatus == 200
                ? new String[]{"entry", "message"}
                : new String[]{"error", "briefSummary"};

        probe.setDiscovery(ready == null
                ? null // force a NPE if used - never should be
                : ready
                ? goodDiscovery
                : badDiscovery);

        HttpResponse response = getSingle(ProbeEntityResource.class, probeType.getValue(), null, expectedStatus);
        Object object = response.getJsonResponse();
        for (String key: keys)
        {
            object = ((JSONObject)object).get(key);
            assertNotNull("Missing \""+key+"\" in json", object);
        }
        String message = object.toString();

        if (expectedStatus != 200) // Strip the leading number from the exception message.
        {
            int i = message.indexOf(' ');
            if (i != -1)
            {
                message = message.substring(i + 1);
            }
        }

        assertEquals(expected, message);
    }

    @Test
    public void testProbes() throws Exception
    {
        // Live first
        assertResponse(LIVE, OK, "liveProbe: Success - Tested", 200);
        assertResponse(READY, null, "readyProbe: Failure - Tested", 503);

        Thread.currentThread().sleep(CHECK_PERIOD);
        assertResponse(READY, OK, "readyProbe: Success - Tested", 200);
        assertResponse(READY, null, "readyProbe: Success - No test", 200);

        Thread.currentThread().sleep(CHECK_PERIOD);
        assertResponse(LIVE, OK, "liveProbe: Success - Tested", 200);
        assertResponse(READY, null, "readyProbe: Failure - Tested", 503);

        // Ready first
        Thread.currentThread().sleep(CHECK_PERIOD);
        assertResponse(READY, OK, "readyProbe: Success - Tested", 200);
        assertResponse(LIVE, OK, "liveProbe: Success - Tested", 200);
        assertResponse(READY, null, "readyProbe: Success - No test", 200);

        // check db failure
        Thread.currentThread().sleep(CHECK_PERIOD);
        Mockito.doThrow(AlfrescoRuntimeException.class).when(repoHealthChecker).checkDatabase();
        assertResponse(READY, OK, "readyProbe: Failure - Tested", 503);
        assertResponse(READY, OK, "readyProbe: Failure - No test", 503);
        
    }

}
