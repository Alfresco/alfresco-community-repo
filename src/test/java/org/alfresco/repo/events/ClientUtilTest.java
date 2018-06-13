/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.events;

import static org.junit.Assert.*;

import org.alfresco.repo.Client;
import org.alfresco.util.FileFilterMode;
import org.junit.Test;

public class ClientUtilTest
{

    /**
     * If a new client is added to the FileFilterMode.Client then this unit test will
     * throw a IllegalArgument exception.  To fix it you will need to add to the
     * org.alfresco.events.Client.ClientType.
     */
    @Test
    public void testFileFilterModeConversion()
    {
        // Loop through all the client types checking they work
        for (FileFilterMode.Client client : FileFilterMode.Client.values())
        {
            equalsConversion(client);
        }
        org.alfresco.repo.Client client = ClientUtil.from(null);
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
        org.alfresco.repo.Client client = ClientUtil.from(ffSource);
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
    
    private static FileFilterMode.Client to(org.alfresco.repo.Client from)
    {
        FileFilterMode.Client client = FileFilterMode.Client.valueOf(from.getType().toString());
        return client;
    }
}
