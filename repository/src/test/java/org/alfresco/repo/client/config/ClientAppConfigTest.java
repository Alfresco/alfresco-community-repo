/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

package org.alfresco.repo.client.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.alfresco.repo.client.config.ClientAppConfig.ClientApp;
import org.alfresco.service.cmr.repository.TemporalSourceOptions;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.testing.category.LuceneTests;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Collections;
import java.util.Map;

/**
 * This class contains tests for the class {@link ClientAppConfig}
 *
 * @author Jamal Kaabi-Mofrad
 */
@Category(LuceneTests.class)
public class ClientAppConfigTest
{
    private ApplicationContext context;
    private ClientAppConfig clientAppConfig;

    @Before
    public void setUp() throws Exception
    {
        ApplicationContextHelper.closeApplicationContext();
        context = ApplicationContextHelper.getApplicationContext(new String[] { ApplicationContextHelper.CONFIG_LOCATIONS[0],
                "classpath:org/alfresco/repo/client/config/test-repo-clients-apps-context.xml" });

        clientAppConfig = context.getBean("clientAppConfigTest", ClientAppConfig.class);
    }

    @Test
    public void testClients() throws Exception
    {
        Map<String, ClientApp> clients = clientAppConfig.getClients();
        assertNotNull(clients);
        assertEquals("Incorrect number of clients", 4, clients.size());

        // loaded from org/alfresco/repo/client/config/test-repo-clients-apps.properties
        ClientApp client1 = clientAppConfig.getClient("test-client1");
        assertNotNull(client1);
        assertEquals("test-client1", client1.getName());
        assertEquals("http://localhost:8081/test-client1/o", client1.getProperty("sharedLinkBaseUrl"));
        assertEquals("http://localhost:8081/test-client1", client1.getTemplateAssetsUrl());

        // loaded from org/alfresco/repo/client/config/test-repo-clients-apps.properties and overridden by
        // org/alfresco/repo/client/config/test-global-properties.properties
        ClientApp client2 = clientAppConfig.getClient("test-client2");
        assertNotNull(client2);
        assertEquals("test-client2", client2.getName());
        assertEquals("https://127.0.0.1:8082/test-client2/t", client2.getProperty("sharedLinkBaseUrl"));
        assertEquals("https://127.0.0.1:8082/test-client2", client2.getTemplateAssetsUrl());

        // loaded from org/alfresco/repo/client/config/test-global-properties.properties
        ClientApp client3 = clientAppConfig.getClient("test-client5");
        assertNotNull(client3);
        assertEquals("test-client5", client3.getName());
        assertEquals("http://localhost:8085/test-client5/f", client3.getProperty("myProp1"));
        assertEquals("http://localhost:8085/test-client5", client3.getProperty("myProp2"));
        assertEquals("test prop3", client3.getProperty("myProp3"));
        assertEquals("test prop4", client3.getProperty("myProp4"));
        assertEquals("test prop5", client3.getProperty("myProp5-with-hyphen"));
        assertNull(client3.getTemplateAssetsUrl());

        // loaded from org/alfresco/repo/client/config/test-global-properties.properties
        ClientApp client4 = clientAppConfig.getClient("test-client11");
        assertNotNull(client4);
        assertEquals("test-client11", client4.getName());
        assertEquals("http://localhost:8811/test-client11/t", client4.getProperty("myClientRequiredConfigUrl"));
        assertNull(client4.getTemplateAssetsUrl());
        // Try to add a property into an unmodifiable map
        try
        {
            client4.getProperties().put("newProperty"," test value");
            fail("Shouldn't be able to modify the client's processed properties.");
        }
        catch (UnsupportedOperationException ex)
        {
            // expected
        }

        // Try to add a client into an unmodifiable map
        ClientApp newClient = new ClientApp("testClient" + System.currentTimeMillis(),
                    "http://localhost:8085/testclient", Collections.singletonMap("sharedLinkBaseUrl", "http://localhost:8085/test-client/s"));
        try
        {
            clients.put(newClient.getName(), newClient);
            fail("Shouldn't be able to modify the clients map.");
        }
        catch (UnsupportedOperationException ex)
        {
            // expected
        }
    }
}
