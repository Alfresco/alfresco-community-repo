/*
 * #%L
 * Alfresco Repository
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

package org.alfresco.repo.quickshare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.alfresco.repo.quickshare.ClientAppConfig.ClientApp;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Map;

/**
 * This class contains tests for the class {@link ClientAppConfig}
 *
 * @author Jamal Kaabi-Mofrad
 */
public class ClientAppConfigTest
{
    private static ClassPathXmlApplicationContext context;
    private static ClientAppConfig clientAppConfig;

    @BeforeClass
    public static void setUp() throws Exception
    {
        context = new ClassPathXmlApplicationContext(new String[] { "classpath:org/alfresco/repo/quickshare/test-quickshare-clients-context.xml" },
                    ApplicationContextHelper.getApplicationContext());

        clientAppConfig = context.getBean("quickShareClientsConfigTest", ClientAppConfig.class);

    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        context.close();
    }

    @Test
    public void testClients() throws Exception
    {
        Map<String, ClientApp> clients = clientAppConfig.getClients();
        assertNotNull(clients);
        assertEquals("Incorrect number of clients", 3, clients.size());

        // loaded from org/alfresco/repo/quickshare/test-quickshare-clients-config.properties
        ClientApp client1 = clientAppConfig.getClient("test-client1");
        assertNotNull(client1);
        assertEquals("test-client1", client1.getName());
        assertEquals("http://localhost:8081/test-client1/o", client1.getSharedLinkBaseUrl());
        assertEquals("http://localhost:8081/test-client1", client1.getTemplateAssetsUrl());

        // loaded from org/alfresco/repo/quickshare/test-quickshare-clients-config.properties and overridden by
        // org/alfresco/repo/quickshare/test-global-properties.properties
        ClientApp client2 = clientAppConfig.getClient("test-client2");
        assertNotNull(client2);
        assertEquals("test-client2", client2.getName());
        assertEquals("https://127.0.0.1:8082/test-client2/t", client2.getSharedLinkBaseUrl());
        assertEquals("https://127.0.0.1:8082/test-client2", client2.getTemplateAssetsUrl());

        // loaded from org/alfresco/repo/quickshare/test-global-properties.properties
        ClientApp client3 = clientAppConfig.getClient("test-client5");
        assertNotNull(client3);
        assertEquals("test-client5", client3.getName());
        assertEquals("http://localhost:8085/test-client5/f", client3.getSharedLinkBaseUrl());
        assertEquals("http://localhost:8085/test-client5", client3.getTemplateAssetsUrl());

        // Try to add a client into an unmodifiable map
        ClientApp newClient = new ClientApp("testClient" + System.currentTimeMillis(), "http://localhost:8085/test-client/s",
                    "http://localhost:8085/testclient");
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
