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
import org.springframework.extensions.config.ConfigService;

/**
 * Connection manager base class.
 */
public abstract class AbstractCMISConnectionManagerImpl extends CMISHelper implements CMISConnectionManager
{
    public static final String SERVER_NAME = "name";
    public static final String SERVER_DESCRIPTION = "description";
    public static final String DEFAULT_CONNECTION_ID = "default";
    public static final char RESERVED_ID_CHAR = '$';

    protected ConfigService configService;
    protected AuthenticationService authenticationService;

    protected final SessionFactory sessionFactory = SessionFactoryImpl.newInstance();

    protected LinkedHashMap<String, CMISConnection> sharedConnections;
    protected LinkedHashMap<String, CMISConnection> userConnections;
    protected int userConnectionsCapacity = 1000;
    protected int sharedConnectionsCapacity = 100;

    protected Map<String, CMISServer> servers;

    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // --- set up ---

    public void setConfigService(ConfigService configService)
    {
        this.configService = configService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public void setUserConnectionsCapacity(int userConnectionsCapacity)
    {
        this.userConnectionsCapacity = userConnectionsCapacity;
    }

    public void setSharedConnectionsCapacity(int sharedConnectionsCapacity)
    {
        this.sharedConnectionsCapacity = sharedConnectionsCapacity;
    }

    public void init()
    {
        lock.writeLock().lock();
        try
        {
            // create shared connection LRU cache
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

            // create user connection LRU cache
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

            // get predefined server definitions
            CMISServersConfigElement cmisServersConfig = (CMISServersConfigElement) configService.getConfig("CMIS")
                    .getConfigElement("cmis-servers");
            if (cmisServersConfig != null && cmisServersConfig.getServerDefinitions() != null)
            {
                servers = cmisServersConfig.getServerDefinitions();
            } else
            {
                servers = new HashMap<String, CMISServer>();
            }
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    // --- connections ---

    @Override
    public CMISConnection createUserConnection(CMISServer server, String connectionId)
    {
        if (connectionId == null || connectionId.length() == 0 || connectionId.indexOf(RESERVED_ID_CHAR) > -1)
        {
            throw new IllegalArgumentException("Invalid connection id!");
        }

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

            connection = createConnection(server, userConnectionId, false);

            userConnections.put(userConnectionId, connection);
        } finally
        {
            lock.writeLock().unlock();
        }

        return connection;
    }

    protected String createUserConnectionId(String username, String connectionId)
    {
        return connectionId + RESERVED_ID_CHAR + username;
    }

    @Override
    public CMISConnection createSharedConnection(CMISServer server, String connectionId)
    {
        if (connectionId == null || connectionId.length() == 0 || connectionId.indexOf(RESERVED_ID_CHAR) > -1
                || DEFAULT_CONNECTION_ID.equals(connectionId))
        {
            throw new IllegalArgumentException("Invalid connection id!");
        }

        CMISConnection connection;

        lock.writeLock().lock();
        try
        {
            if (sharedConnections.containsKey(connectionId))
            {
                throw new IllegalStateException("Connection id is already in use!");
            }

            connection = createConnection(server, connectionId, true);

            sharedConnections.put(connection.getInternalId(), connection);
        } finally
        {
            lock.writeLock().unlock();
        }

        return connection;
    }

    protected CMISConnection createConnection(CMISServer server, String connectionId, boolean isShared)
    {
        Session session = createSession(server.getParameters());
        String username = server.getParameters().get(SessionParameter.USER);

        return new CMISConnectionImpl(this, connectionId, session, server, username, false, isShared);
    }

    public abstract CMISConnection getConnection();

    public CMISConnection getConnection(String connectionId)
    {
        lock.writeLock().lock();
        try
        {
            CMISConnection connection = sharedConnections.get(connectionId);
            if (connection != null)
            {
                return connection;
            }

            String currentUser = authenticationService.getCurrentUserName();
            if (currentUser == null)
            {
                return null;
            }

            String userConnectionId = createUserConnectionId(currentUser, connectionId);
            return userConnections.get(userConnectionId);
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    public List<CMISConnection> getUserConnections()
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
                int idx = conn.getInternalId().indexOf(RESERVED_ID_CHAR);
                if (idx > -1)
                {
                    if (currentUser.equals(conn.getInternalId().substring(idx + 1)))
                    {
                        result.add(conn);
                    }
                }
            }

            Collections.sort(result);
            return Collections.unmodifiableList(result);
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    public List<CMISConnection> getSharedConnections()
    {
        lock.writeLock().lock();
        try
        {
            List<CMISConnection> result = new ArrayList<CMISConnection>(sharedConnections.values());

            Collections.sort(result);
            return Collections.unmodifiableList(result);
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    public void removeConnection(CMISConnection connection)
    {
        if (connection == null || connection.getInternalId() == null)
        {
            return;
        }

        lock.writeLock().lock();
        try
        {
            if (connection.isShared())
            {
                sharedConnections.remove(connection.getInternalId());
            } else
            {
                userConnections.remove(connection.getInternalId());
            }
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    protected Session createSession(Map<String, String> parameters)
    {
        if (parameters.containsKey(SessionParameter.REPOSITORY_ID))
        {
            return sessionFactory.createSession(new HashMap<String, String>(parameters));
        } else
        {
            return sessionFactory.getRepositories(new HashMap<String, String>(parameters)).get(0).createSession();
        }
    }

    // --- servers ---

    public List<CMISServer> getServerDefinitions()
    {
        return servers == null ? null : Collections.unmodifiableList(new ArrayList<CMISServer>(servers.values()));
    }

    public CMISServer getServerDefinition(String serverName)
    {
        return servers == null ? null : servers.get(serverName);
    }

    public CMISServer createServerDefinition(String serverName, Map<String, String> parameters)
    {
        return new CMISServerImpl(serverName, null, parameters);
    }

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

    protected CMISServer createServerDefinition(Map<String, String> parameters)
    {
        if (parameters == null)
        {
            throw new IllegalArgumentException("Parameters must be set!");
        }

        String name = parameters.get(SERVER_NAME);
        parameters.remove(SERVER_NAME);

        String description = parameters.get(SERVER_DESCRIPTION);
        parameters.remove(SERVER_DESCRIPTION);

        if (name != null)
        {
            return new CMISServerImpl(name, description, parameters);
        }

        return null;
    }

    public List<Repository> getRepositories(CMISServer server)
    {
        if (server == null)
        {
            throw new IllegalArgumentException("Server must be set!");
        }

        return sessionFactory.getRepositories(new HashMap<String, String>(server.getParameters()));
    }
}
