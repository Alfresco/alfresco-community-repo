/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.events;

import org.alfresco.repo.Client.ClientType;
import org.alfresco.util.FileFilterMode;

/**
 * Allows us to convert between a FileFilterMode.Client and org.alfresco.events.Client.
 * This is a one way conversion. org.alfresco.events.Client may evolve independently
 * from FileFilterMode.Client, ie. there's a "from" method but no "to" method.
 *
 * @author Gethin James
 */
public class ClientUtil
{
    /**
     * If a new client is added to the FileFilterMode.Client then the unit test will
     * throw a IllegalArgument exception.  To fix it you will need to add to the
     * org.alfresco.events.Client.ClientType.
     * 
     * @param from FileFilterMode.Client
     * @return org.alfresco.events.Client
     */
    public static org.alfresco.repo.Client from(FileFilterMode.Client from)
    {
        if (from == null) return null;
        ClientType type = org.alfresco.repo.Client.ClientType.valueOf(from.toString());
        return org.alfresco.repo.Client.asType(type);
    }
    
//    public static FileFilterMode.Client to(org.alfresco.events.Client from)
//    {
//        FileFilterMode.Client client = FileFilterMode.Client.valueOf(from.getType().toString());
//        return client;
//    }
}
