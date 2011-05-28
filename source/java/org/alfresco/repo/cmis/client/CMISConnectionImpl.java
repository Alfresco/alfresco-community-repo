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

import org.apache.chemistry.opencmis.client.api.Session;

public class CMISConnectionImpl implements CMISConnection
{
    private AbstractCMISConnectionManagerImpl connectionManager;

    private String id;
    private Session session;
    private CMISServer server;
    private String username;
    private boolean isLocal;

    public CMISConnectionImpl(AbstractCMISConnectionManagerImpl connectionManager, String id, Session session,
            CMISServer server, String username, boolean isLocal)
    {
        if (connectionManager == null)
        {
            throw new IllegalArgumentException("Connection Manager must be set!");
        }

        if (id == null)
        {
            throw new IllegalArgumentException("Id must be set!");
        }

        if (session == null)
        {
            throw new IllegalArgumentException("Session must be set!");
        }

        this.connectionManager = connectionManager;
        this.id = id;
        this.session = session;
        this.server = server;
        this.username = username;
        this.isLocal = isLocal;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public Session getSession()
    {
        return session;
    }

    @Override
    public CMISServer getServer()
    {
        return server;
    }

    @Override
    public String getUserName()
    {
        return username;
    }

    @Override
    public boolean isLocal()
    {
        return isLocal;
    }

    @Override
    public void close()
    {
        connectionManager.removeConnection(this);
        session = null;
    }
}
