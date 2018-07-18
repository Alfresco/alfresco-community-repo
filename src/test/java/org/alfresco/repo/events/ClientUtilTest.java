/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.events;

import static org.junit.Assert.*;

import org.alfresco.sync.repo.Client;
import org.alfresco.util.FileFilterMode;
import org.junit.Test;

public class ClientUtilTest
{

    /**
     * If a new client is added to the FileFilterMode.Client then this unit test will
     * throw a IllegalArgument exception.  To fix it you will need to add to the
     * org.alfresco.sync.events.Client.ClientType.
     */
    @Test
    public void testFileFilterModeConversion()
    {
        // Loop through all the client types checking they work
        for (FileFilterMode.Client client : FileFilterMode.Client.values())
        {
            equalsConversion(client);
        }
        org.alfresco.sync.repo.Client client = ClientUtil.from(null);
        assertNull(client);
    }

    @Test
    public void testClientType()
    {
        Client client = Client.asType(null);
        assertNotNull(client);
    }

    private void equalsConversion(FileFilterMode.Client ffSource)
    {
        org.alfresco.sync.repo.Client client = ClientUtil.from(ffSource);
        FileFilterMode.Client ffClient = to(client);
        assertEquals(ffSource, ffClient);
    }

    /*
    @Test
    public void testClientTypeConversion()
    {
    //Loop through all the client types checking they work
    for (ClientType type : org.alfresco.events.Client.ClientType.values()) {
    equalsClientTypeConversion(type);
    }
    }
   
   
    private void equalsClientTypeConversion(ClientType type)
    {
    FileFilterMode.Client ffClient = to(Client.asType(type));
    org.alfresco.events.Client client = ClientUtil.from(ffClient);
    assertEquals(type, client.getType());
    }
    */
    
    private static FileFilterMode.Client to(org.alfresco.sync.repo.Client from)
    {
        FileFilterMode.Client client = FileFilterMode.Client.valueOf(from.getType().toString());
        return client;
    }
}
