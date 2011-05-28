/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.cmis.client;

import java.util.Map;

/**
 * CMIS Connection manager with a remote default connection.
 */
public class CMISRemoteConnectionManagerImpl extends AbstractCMISConnectionManagerImpl implements CMISConnectionManager
{
    private CMISServer defaultServer;

    /**
     * Sets the remote server details.
     */
    public void setDefaultServer(Map<String, String> defaultServerProperties)
    {
        defaultServer = createServerDefinition(defaultServerProperties);
    }

    /**
     * Creates a remote connection.
     */
    @Override
    public CMISConnection getConnection()
    {
        lock.writeLock().lock();
        try
        {
            CMISConnection connection = getUserConnections(LOCAL_CONNECTION_ID);
            if (connection != null)
            {
                return connection;
            }

            String currentUser = authenticationService.getCurrentUserName();

            if (defaultServer == null)
            {
                throw new IllegalStateException("No default server defined!");
            }

            CMISServer server = createServerDefinition(defaultServer, currentUser, null);

            return createUserConnection(server, LOCAL_CONNECTION_ID);
        } finally
        {
            lock.writeLock().unlock();
        }
    }
}
