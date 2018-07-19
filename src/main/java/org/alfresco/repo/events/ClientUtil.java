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

import org.alfresco.sync.repo.Client.ClientType;
import org.alfresco.util.FileFilterMode;

/**
 * Allows us to convert between a FileFilterMode.Client and org.alfresco.sync.events.Client.
 * This is a one way conversion. org.alfresco.sync.events.Client may evolve independently
 * from FileFilterMode.Client, ie. there's a "from" method but no "to" method.
 *
 * @author Gethin James
 */
public class ClientUtil
{
    /**
     * If a new client is added to the FileFilterMode.Client then the unit test will
     * throw a IllegalArgument exception.  To fix it you will need to add to the
     * org.alfresco.sync.events.Client.ClientType.
     * 
     * @param from FileFilterMode.Client
     * @return org.alfresco.sync.events.Client
     */
    public static org.alfresco.sync.repo.Client from(FileFilterMode.Client from)
    {
        if (from == null) return null;
        ClientType type = org.alfresco.sync.repo.Client.ClientType.valueOf(from.toString());
        return org.alfresco.sync.repo.Client.asType(type);
    }
    
//    public static FileFilterMode.Client to(org.alfresco.events.Client from)
//    {
//        FileFilterMode.Client client = FileFilterMode.Client.valueOf(from.getType().toString());
//        return client;
//    }
}
