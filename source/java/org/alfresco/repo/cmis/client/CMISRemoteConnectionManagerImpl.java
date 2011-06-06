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

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.SessionParameter;

/**
 * CMIS Connection manager with a remote default connection.
 */
public class CMISRemoteConnectionManagerImpl extends AbstractCMISConnectionManagerImpl implements CMISConnectionManager
{
    @Override
    public CMISConnection createDefaultConnection(CMISServer server)
    {
        lock.writeLock().lock();
        try
        {
            CMISConnection connection = getConnection();
            if (connection != null)
            {
                throw new IllegalStateException("Connection id is already in use!");
            }

            if (server == null)
            {
                throw new IllegalStateException("Server definition must be set!");
            }

            String currentUser = authenticationService.getCurrentUserName();

            if (!server.getParameters().containsKey(SessionParameter.USER))
            {
                Map<String, String> parameters = new HashMap<String, String>(server.getParameters());
                parameters.put(SessionParameter.USER, currentUser);
                server = createServerDefinition(server.getName(), parameters);
            }

            String userConnectionId = createUserConnectionId(currentUser, DEFAULT_CONNECTION_ID);
            Session session = createSession(server.getParameters());
            connection = new CMISConnectionImpl(this, userConnectionId, session, server, currentUser, true, false);

            userConnections.put(userConnectionId, connection);

            return connection;
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    /**
     * Creates a remote connection.
     */
    @Override
    public CMISConnection getConnection()
    {
        return getConnection(DEFAULT_CONNECTION_ID);
    }
}
