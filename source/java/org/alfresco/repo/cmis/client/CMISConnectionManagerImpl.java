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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.springframework.beans.factory.InitializingBean;

public class CMISConnectionManagerImpl implements CMISConnectionManager, InitializingBean
{
    public static final String SERVER_NAME = "name";
    public static final String SERVER_DESCRIPTION = "description";
    public static final String LOCAL_CONNECTION_ID = "local";
    public static final char RESERVED_ID_CHAR = '$';

    private AuthenticationService authenticationService;

    private SessionFactory sessionFactory;

    private LinkedHashMap<String, CMISConnection> sharedConnections;
    private LinkedHashMap<String, CMISConnection> userConnections;
    private int userConnectionsCapacity = 1000;
    private int sharedConnectionsCapacity = 1000;

    private Map<String, CMISServer> servers;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public CMISConnectionManagerImpl()
    {
    }

    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public void setServers(List<Map<String, String>> serverList)
    {
        servers = new HashMap<String, CMISServer>();

        for (Map<String, String> serverData : serverList)
        {
            String name = serverData.get(SERVER_NAME);
            serverData.remove(SERVER_NAME);

            String description = serverData.get(SERVER_DESCRIPTION);
            serverData.remove(SERVER_DESCRIPTION);

            if (name != null)
            {
                CMISServer server = new CMISServerImpl(name, description, serverData);
                servers.put(server.getName(), server);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        lock.writeLock().lock();
        try
        {
            sessionFactory = SessionFactoryImpl.newInstance();

            sharedConnections = new LinkedHashMap<String, CMISConnection>(sharedConnectionsCapacity,
                    (int) Math.ceil(sharedConnectionsCapacity / 0.75) + 1, true)
            {
                private static final long serialVersionUID = 1;

                @Override
                protected boolean removeEldestEntry(Map.Entry<String, CMISConnection> eldest)
                {
                    return size() > sharedConnectionsCapacity;
                }
            };

            userConnections = new LinkedHashMap<String, CMISConnection>(userConnectionsCapacity,
                    (int) Math.ceil(userConnectionsCapacity / 0.75) + 1, true)
            {
                private static final long serialVersionUID = 1;

                @Override
                protected boolean removeEldestEntry(Map.Entry<String, CMISConnection> eldest)
                {
                    return size() > userConnectionsCapacity;
                }
            };
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    @Override
    public CMISConnection createUserConnection(CMISServer server, String connectionId)
    {
        String currentUser = authenticationService.getCurrentUserName();
        if (currentUser == null)
        {
            throw new IllegalStateException("No current user!");
        }

        String userConnectionId = createUserConnectionId(currentUser, connectionId);

        CMISConnection connection;

        lock.writeLock().lock();
        try
        {
            if (userConnections.containsKey(userConnectionId))
            {
                throw new IllegalStateException("Connection id is already in use!");
            }

            connection = createConnection(server, userConnectionId);

            userConnections.put(connection.getId(), connection);
        } finally
        {
            lock.writeLock().unlock();
        }

        return connection;
    }

    private String createUserConnectionId(String username, String connectionId)
    {
        return connectionId + RESERVED_ID_CHAR + username;
    }

    @Override
    public CMISConnection createSharedConnection(CMISServer server, String connectionId)
    {
        CMISConnection connection;

        lock.writeLock().lock();
        try
        {
            if (sharedConnections.containsKey(connectionId))
            {
                throw new IllegalStateException("Connection id is already in use!");
            }

            connection = createConnection(server, connectionId);

            sharedConnections.put(connection.getId(), connection);
        } finally
        {
            lock.writeLock().unlock();
        }

        return connection;
    }

    private CMISConnection createConnection(CMISServer server, String connectionId)
    {
        if (connectionId == null || connectionId.length() == 0 || connectionId.indexOf(RESERVED_ID_CHAR) > -1)
        {
            throw new IllegalArgumentException("Invalid connection id!");
        }

        Session session = sessionFactory.createSession(server.getParameters());
        String username = server.getParameters().get(SessionParameter.USER);

        return new CMISConnectionImpl(this, connectionId, session, server, username, false);
    }

    @Override
    public CMISConnection getLocalConnection()
    {
        CMISConnection connection = getUserConnections(LOCAL_CONNECTION_ID);
        if (connection != null)
        {
            return connection;
        }

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(SessionParameter.BINDING_TYPE, BindingType.LOCAL.value());
        // ToDo: add factory

        CMISServer server = createServerDefinition("local", parameters);

        Session session = sessionFactory.createSession(server.getParameters());

        connection = new CMISConnectionImpl(this, LOCAL_CONNECTION_ID, session, server, null, true);

        lock.writeLock().lock();
        try
        {
            userConnections.put(connection.getId(), connection);
        } finally
        {
            lock.writeLock().unlock();
        }

        return connection;
    }

    public Collection<CMISConnection> getUserConnections()
    {
        String currentUser = authenticationService.getCurrentUserName();
        if (currentUser == null)
        {
            throw new IllegalStateException("No current user!");
        }

        lock.writeLock().lock();
        try
        {
            List<CMISConnection> result = new ArrayList<CMISConnection>();

            for (CMISConnection conn : userConnections.values())
            {
                int idx = conn.getId().indexOf(RESERVED_ID_CHAR);
                if (idx > -1)
                {
                    if (currentUser.equals(conn.getId().substring(idx + 1)))
                    {
                        result.add(conn);
                    }
                }
            }

            return Collections.unmodifiableList(result);
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    public CMISConnection getUserConnections(String connectionId)
    {
        String currentUser = authenticationService.getCurrentUserName();
        if (currentUser == null)
        {
            throw new IllegalStateException("No current user!");
        }

        lock.writeLock().lock();
        try
        {
            String userConnectionId = createUserConnectionId(currentUser, connectionId);
            return userConnections.get(userConnectionId);
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    public Collection<CMISConnection> getSharedConnections()
    {
        lock.writeLock().lock();
        try
        {
            return Collections.unmodifiableCollection(sharedConnections.values());
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    public CMISConnection getSharedConnection(String connectionId)
    {
        lock.writeLock().lock();
        try
        {
            return sharedConnections.get(connectionId);
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    public void removeConnection(CMISConnection connection)
    {
        if (connection == null || connection.getId() == null)
        {
            return;
        }

        lock.writeLock().lock();
        try
        {
            if (connection.isLocal())
            {
                userConnections.remove(LOCAL_CONNECTION_ID);
            } else
            {
                int idx = connection.getId().indexOf(RESERVED_ID_CHAR);
                if (idx == -1)
                {
                    sharedConnections.remove(connection.getId());
                } else
                {
                    userConnections.remove(connection.getId());
                }
            }
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Collection<CMISServer> getServerDefinitions()
    {
        return servers == null ? null : servers.values();
    }

    @Override
    public CMISServer getServerDefinition(String serverName)
    {
        return servers == null ? null : servers.get(serverName);
    }

    @Override
    public CMISServer createServerDefinition(String serverName, Map<String, String> parameters)
    {
        return new CMISServerImpl(serverName, null, parameters);
    }

    @Override
    public CMISServer createServerDefinition(CMISServer server, String username, String password)
    {
        if (server == null)
        {
            throw new IllegalArgumentException("Server must be set!");
        }

        Map<String, String> parameters = new HashMap<String, String>(server.getParameters());
        parameters.put(SessionParameter.USER, username);
        parameters.put(SessionParameter.PASSWORD, password);

        return new CMISServerImpl(server.getName(), server.getDescription(), parameters);
    }

    @Override
    public CMISServer createServerDefinition(CMISServer server, String username, String password, String repositoryId)
    {
        if (server == null)
        {
            throw new IllegalArgumentException("Server must be set!");
        }

        Map<String, String> parameters = new HashMap<String, String>(server.getParameters());
        parameters.put(SessionParameter.USER, username);
        parameters.put(SessionParameter.PASSWORD, password);
        parameters.put(SessionParameter.REPOSITORY_ID, repositoryId);

        return new CMISServerImpl(server.getName(), server.getDescription(), parameters);
    }

    @Override
    public List<Repository> getRepositories(CMISServer server)
    {
        if (server == null)
        {
            throw new IllegalArgumentException("Server must be set!");
        }

        return sessionFactory.getRepositories(server.getParameters());
    }
}
