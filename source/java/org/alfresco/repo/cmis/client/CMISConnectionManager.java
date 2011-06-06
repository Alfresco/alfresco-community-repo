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

import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Repository;

/**
 * Manages all CMIS client connections.
 */
public interface CMISConnectionManager
{
    // --- connections ---

    /**
     * Creates a new default connection that is only visible to the current
     * user.
     */
    CMISConnection createDefaultConnection(CMISServer server);

    /**
     * Creates a new connection that is only visible to the current user.
     */
    CMISConnection createUserConnection(CMISServer server, String connectionId);

    /**
     * Creates a new connection that is visible to all users.
     */
    CMISConnection createSharedConnection(CMISServer server, String connectionId);

    /**
     * Gets or creates a connection to the local server or a default server.
     */
    CMISConnection getConnection();

    /**
     * Returns a specific connection or <code>null</code> if the connection id
     * is unknown.
     */
    CMISConnection getConnection(String connectionId);

    /**
     * Returns all user connections.
     */
    List<CMISConnection> getUserConnections();

    /**
     * Returns all shared connections.
     */
    List<CMISConnection> getSharedConnections();

    // --- servers ---

    /**
     * Returns all configured server definitions.
     */
    List<CMISServer> getServerDefinitions();

    /**
     * Gets a server definitions by name.
     */
    CMISServer getServerDefinition(String serverName);

    /**
     * Creates a new server definition.
     */
    CMISServer createServerDefinition(String serverName, Map<String, String> parameters);

    /**
     * Creates a new server definition from a template.
     */
    CMISServer createServerDefinition(CMISServer server, String username, String password);

    /**
     * Creates a new server definition from a template.
     */
    CMISServer createServerDefinition(CMISServer server, String username, String password, String repositoryId);

    // --- repositories ---

    /**
     * Returns all repositories available at this server.
     */
    List<Repository> getRepositories(CMISServer server);
}
